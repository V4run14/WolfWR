//Still pending testing

package WolfWR;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class BuyOrReturn {

    private static final String jdbcURL = "jdbc:mariadb://classdb2.csc.ncsu.edu:3306/vvarath";
    private static final String user = "vvarath";
    private static final String password = "dbmsproj2025";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserSession session = Reports.login(scanner);
        if (session == null) return;

        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Buy Items");
            System.out.println("2. Return Items");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    handleBuy(scanner, session);
                    break;
                case 2:
                    handleReturn(scanner, session);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void handleBuy(Scanner scanner, UserSession session) {
        String position = session.getJobTitle();
        if (position.equals("Billing Staff") || position.equals("Registration Office Operator") || position.equals("Warehouse Operator") || position.equals("Manager") || position.equals("Assistant Manager")){
            System.out.println("You do not have access to this functionality!");
            return;
        }
        try (Connection conn = DriverManager.getConnection(jdbcURL, user, password)) {
            conn.setAutoCommit(false);

            System.out.print("Do you want to enter your own Transaction ID? (yes/no): ");
            String input = scanner.nextLine().trim().toLowerCase();

            int transactionId;
            if (input.equals("yes")) {
                System.out.print("Enter Transaction ID: ");
                transactionId = scanner.nextInt();
                scanner.nextLine();
            } else {
                transactionId = getNextTransactionId(conn);
                System.out.println("Generated Transaction ID: " + transactionId);
            }

            System.out.print("Enter date (YYYY-MM-DD): ");
            String date = scanner.nextLine();

            System.out.print("Enter number of items: ");
            int itemCount = scanner.nextInt();
            int[][] items = new int[itemCount][2];
            Set<Integer> productIds = new HashSet<>();

            for (int i = 0; i < itemCount; i++) {
                System.out.print("Enter Product ID and Quantity (space-separated): ");
                items[i][0] = scanner.nextInt();
                items[i][1] = scanner.nextInt();
                productIds.add(items[i][0]);
            }

            System.out.print("Enter Customer ID: ");
            int customerId = scanner.nextInt();
            scanner.nextLine();

            if (!checkCustomerExists(conn, customerId)) {
                conn.rollback();
                System.out.println("Error: Customer ID does not exist.");
                return;
            }

            if (!checkProductsExist(conn, productIds)) {
                conn.rollback();
                System.out.println("Error: One or more Product IDs do not exist.");
                return;
            }

            if (!checkInventoryAvailability(conn, session.getStoreId(), items)) {
                conn.rollback();
                System.out.println("Error: Insufficient inventory for one or more items.");
                return;
            }

            int staffId = getStaffId(session.getName(), conn);
            insertIntoTransactionRecords(transactionId, staffId, customerId, date, 0.0, "Buy", conn);

            double totalPrice = 0.0;
            List<String[]> billDetails = new ArrayList<>();

            for (int[] item : items) {
                int productId = item[0];
                int qty = item[1];
                double price = getDiscountedPrice(productId, date, conn);
                totalPrice += price * qty;

                insertIntoBills(transactionId, productId, price, qty, conn);
                updateInventory(session.getStoreId(), productId, qty, conn);

                String productName = getProductName(conn, productId);
                billDetails.add(new String[]{String.valueOf(productId), productName, String.valueOf(qty), String.format("%.2f", price * qty)});
            }

            updateTotalPriceInTransaction(transactionId, totalPrice, conn);
            conn.commit();

            // Print bill
            System.out.println("\n=== Bill Details ===");
            System.out.println("Transaction ID: " + transactionId);
            System.out.println("Date: " + date);
            System.out.println("Staff ID: " + staffId);
            System.out.println("Customer ID: " + customerId);
            System.out.println("+------------+----------------+----------+------------+");
            System.out.printf("| %-10s | %-14s | %-8s | %-10s |\n", "ProductID", "Product Name", "Quantity", "Total($)");
            System.out.println("+------------+----------------+----------+------------+");
            for (String[] row : billDetails) {
                System.out.printf("| %-10s | %-14s | %-8s | %-10s |\n", row[0], row[1], row[2], row[3]);
            }
            System.out.println("+------------+----------------+----------+------------+");
            System.out.printf("Total Bill: $%.2f\n", totalPrice);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean checkCustomerExists(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT 1 FROM ClubMember WHERE CustomerID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            return stmt.executeQuery().next();
        }
    }

    private static boolean checkProductsExist(Connection conn, Set<Integer> productIds) throws SQLException {
        String sql = "SELECT ProductID FROM Merchandise WHERE ProductID IN (" +
                String.join(",", Collections.nCopies(productIds.size(), "?")) + ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int i = 1;
            for (int id : productIds) stmt.setInt(i++, id);
            ResultSet rs = stmt.executeQuery();
            Set<Integer> found = new HashSet<>();
            while (rs.next()) found.add(rs.getInt("ProductID"));
            return found.containsAll(productIds);
        }
    }

    private static boolean checkInventoryAvailability(Connection conn, int storeId, int[][] items) throws SQLException {
        for (int[] item : items) {
            int productId = item[0];
            int qty = item[1];
            String sql = "SELECT Quantity FROM StoreInventory WHERE StoreID = ? AND ProductID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, storeId);
                stmt.setInt(2, productId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next() || rs.getInt("Quantity") < qty) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String getProductName(Connection conn, int productId) throws SQLException {
        String sql = "SELECT Name FROM Merchandise WHERE ProductID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("Name");
        }
        return "Unknown";
    }


    private static int getStaffId(String staffName, Connection conn) throws SQLException {
        String sql = "SELECT StaffID FROM Staff WHERE Name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, staffName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("StaffID");
        }
        throw new SQLException("Staff ID not found for " + staffName);
    }

    private static int getNextTransactionId(Connection conn) throws SQLException {
        String sql = "SELECT MAX(TransactionID) AS MaxID FROM TransactionRecords";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("MaxID") + 1;
        }
        return 1;
    }

    private static double getDiscountedPrice(int productId, String date, Connection conn) throws SQLException {
        double marketPrice = 0;
        double discount = 0;

        String priceSql = "SELECT MarketPrice FROM Merchandise WHERE ProductID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(priceSql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) marketPrice = rs.getDouble("MarketPrice");
        }

        String discountSql = "SELECT DiscountPercent, ValidFromDate, ValidTillDate FROM Discount WHERE ProductID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(discountSql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String validFrom = rs.getString("ValidFromDate");
                String validTill = rs.getString("ValidTillDate");
                if (validFrom!=null && !LocalDate.parse(date).isBefore(LocalDate.parse(validFrom)) && validTill != null && !LocalDate.parse(date).isAfter(LocalDate.parse(validTill))) {
                    discount = rs.getDouble("DiscountPercent");
                }
            }
        }

        return marketPrice * (1 - discount / 100);
    }

    private static void insertIntoTransactionRecords(int transId, int staffId, int custId,
                                                     String date, double total, String type, Connection conn) throws SQLException {
        String sql = "INSERT INTO TransactionRecords (TransactionID, StaffID, CustomerID, Date, TotalPrice, Type) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transId);
            stmt.setInt(2, staffId);
            stmt.setInt(3, custId);
            stmt.setString(4, date);
            stmt.setDouble(5, total);
            stmt.setString(6, type);
            stmt.executeUpdate();
        }
    }

    private static void updateTotalPriceInTransaction(int transactionId, double totalPrice, Connection conn) throws SQLException {
        String sql = "UPDATE TransactionRecords SET TotalPrice = ? WHERE TransactionID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, totalPrice);
            stmt.setInt(2, transactionId);
            stmt.executeUpdate();
        }
    }

    private static void insertIntoBills(int transId, int productId, double price, int qty, Connection conn) throws SQLException {
        String sql = "INSERT INTO Bills (TransactionID, ProductID, Price, Quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transId);
            stmt.setInt(2, productId);
            stmt.setDouble(3, price);
            stmt.setInt(4, qty);
            stmt.executeUpdate();
        }
    }

    private static void updateInventory(int storeId, int productId, int qty, Connection conn) throws SQLException {
        String sql = "UPDATE StoreInventory SET Quantity = Quantity - ? WHERE StoreID = ? AND ProductID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, qty);
            stmt.setInt(2, storeId);
            stmt.setInt(3, productId);
            stmt.executeUpdate();
        }
    }

    private static void handleReturn(Scanner scanner, UserSession session) {
        String position = session.getJobTitle();
        if (position.equals("Billing Staff") || position.equals("Registration Office Operator") || position.equals("Cashier") || position.equals("Manager") || position.equals("Assistant Manager")){
            System.out.println("You do not have access to this functionality!");
            return;
        }
        try (Connection conn = DriverManager.getConnection(jdbcURL, user, password)) {
            conn.setAutoCommit(false);

            System.out.print("Enter the original Transaction ID to return from: ");
            int originalTransactionId = scanner.nextInt();
            scanner.nextLine();

            String typeCheckSql = "SELECT Type FROM TransactionRecords WHERE TransactionID = ?";
            try (PreparedStatement typeStmt = conn.prepareStatement(typeCheckSql)) {
                typeStmt.setInt(1, originalTransactionId);
                ResultSet rs = typeStmt.executeQuery();
                if (rs.next()) {
                    String type = rs.getString("Type");
                    if (!"Buy".equalsIgnoreCase(type)) {
                        System.out.println("Error: Only transactions of type 'Buy' can be returned.");
                        conn.rollback();
                        return;
                    }
                } else {
                    System.out.println("Error: Transaction ID does not exist.");
                    conn.rollback();
                    return;
                }
            }

            // Fetch original transaction and bill details
            Map<Integer, Integer> originalQuantities = new HashMap<>();
            Map<Integer, Double> productPrices = new HashMap<>();
            List<String[]> billDetails = new ArrayList<>();
            int originalCustomerId = -1;
            int staffId = getStaffId(session.getName(), conn);

            String txnSql = "SELECT b.ProductID, b.Quantity, b.Price, m.Name, t.CustomerID FROM Bills b JOIN Merchandise m ON b.ProductID = m.ProductID JOIN TransactionRecords t ON b.TransactionID = t.TransactionID WHERE b.TransactionID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(txnSql)) {
                stmt.setInt(1, originalTransactionId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int productId = rs.getInt("ProductID");
                    int qty = rs.getInt("Quantity");
                    double price = rs.getDouble("Price");
                    String name = rs.getString("Name");
                    originalCustomerId = rs.getInt("CustomerID");

                    originalQuantities.put(productId, qty);
                    productPrices.put(productId, price);
                    billDetails.add(new String[]{String.valueOf(productId), name, String.valueOf(qty), String.format("%.2f", price * qty)});
                }
            }

            if (originalQuantities.isEmpty()) {
                System.out.println("Transaction ID not found or no products found.");
                conn.rollback();
                return;
            }

            System.out.println("\n=== Original Bill ===");
            System.out.println("Customer ID: " + originalCustomerId);
            System.out.println("+------------+----------------+----------+------------+");
            System.out.printf("| %-10s | %-14s | %-8s | %-10s |\n", "ProductID", "Product Name", "Quantity", "Total($)");
            System.out.println("+------------+----------------+----------+------------+");
            for (String[] row : billDetails) {
                System.out.printf("| %-10s | %-14s | %-8s | %-10s |\n", row[0], row[1], row[2], row[3]);
            }
            System.out.println("+------------+----------------+----------+------------+");

            System.out.print("Enter return transaction date (YYYY-MM-DD): ");
            String returnDate = scanner.nextLine().trim();

            System.out.print("Enter number of items to return: ");
            int itemCount = scanner.nextInt();
            int[][] returnItemsArray = new int[itemCount][2];

            for (int i = 0; i < itemCount; i++) {
                System.out.print("Enter Product ID and Quantity to return (space-separated): ");
                returnItemsArray[i][0] = scanner.nextInt();
                returnItemsArray[i][1] = scanner.nextInt();
            }

            // Validate items to return
            Map<Integer, Integer> returnItems = new HashMap<>();
            for (int[] item : returnItemsArray) {
                int pid = item[0];
                int qty = item[1];

                if (!originalQuantities.containsKey(pid)) {
                    System.out.println("Error: Product " + pid + " was not part of the original transaction.");
                    conn.rollback();
                    return;
                }
                if (qty > originalQuantities.get(pid)) {
                    System.out.println("Error: Return quantity exceeds purchased quantity for Product " + pid);
                    conn.rollback();
                    return;
                }
                returnItems.put(pid, qty);
            }

            int newTransactionId = getNextTransactionId(conn);
            double returnTotal = 0.0;

            insertIntoTransactionRecords(newTransactionId, staffId, originalCustomerId, returnDate, 0.0, "Return", conn);

            for (Map.Entry<Integer, Integer> entry : returnItems.entrySet()) {
                int pid = entry.getKey();
                int qty = entry.getValue();
                double price = productPrices.get(pid);
                double total = price * qty;
                returnTotal += total;

                insertIntoBills(newTransactionId, pid, price, qty, conn);
                updateInventoryAdd(session.getStoreId(), pid, qty, conn);
            }

            updateTotalPriceInTransaction(newTransactionId, returnTotal, conn);

            conn.commit();

            System.out.println("\n=== Return Bill ===");
            System.out.println("Transaction ID: " + newTransactionId);
            System.out.println("Customer ID: " + originalCustomerId);
            System.out.println("Date: " + returnDate);
            System.out.println("+------------+----------+--------+------------+");
            System.out.printf("| %-10s | %-8s | %-6s | %-10s |\n", "ProductID", "Quantity", "Price", "Total($)");
            System.out.println("+------------+----------+--------+------------+");

            for (Map.Entry<Integer, Integer> entry : returnItems.entrySet()) {
                int pid = entry.getKey();
                int qty = entry.getValue();
                double price = productPrices.get(pid);
                double total = qty * price;
                System.out.printf("| %-10d | %-8d | %-6.2f | %-10.2f |\n", pid, qty, price, total);
            }
            System.out.println("+------------+----------+--------+------------+");
            System.out.printf("Total Refund: $%.2f\n", returnTotal);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateInventoryAdd(int storeId, int productId, int qty, Connection conn) throws SQLException {
        String updateSql = "UPDATE StoreInventory SET Quantity = Quantity + ? WHERE StoreID = ? AND ProductID = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setInt(1, qty);
            updateStmt.setInt(2, storeId);
            updateStmt.setInt(3, productId);
            int affected = updateStmt.executeUpdate();

            if (affected == 0) {
                String insertSql = "INSERT INTO StoreInventory (StoreID, ProductID, Quantity) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, storeId);
                    insertStmt.setInt(2, productId);
                    insertStmt.setInt(3, qty);
                    insertStmt.executeUpdate();
                }
            }
        }
    }
}
