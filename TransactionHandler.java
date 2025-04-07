//Still pending testing

package WolfWR;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class TransactionHandler {

    private static final String jdbcURL = "jdbc:mariadb://classdb2.csc.ncsu.edu:3306/vvarath";
    private static final String user = "vvarath";
    private static final String password = "dbmsproj2025";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserSession session = Reports.login(scanner); // Reusing login method from Reports.java
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
                    System.out.println("Return logic to be implemented.");
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void handleBuy(Scanner scanner, UserSession session) {
        try (Connection conn = DriverManager.getConnection(jdbcURL, user, password)) {
            System.out.print("Enter date (YYYY-MM-DD): ");
            String date = scanner.nextLine();

            System.out.print("Enter number of items: ");
            int itemCount = scanner.nextInt();
            int[][] items = new int[itemCount][2];

            for (int i = 0; i < itemCount; i++) {
                System.out.print("Enter Product ID and Quantity (space-separated): ");
                items[i][0] = scanner.nextInt(); // ProductID
                items[i][1] = scanner.nextInt(); // Quantity
            }

            System.out.print("Enter Customer ID: ");
            int customerId = scanner.nextInt();
            scanner.nextLine();

            int staffId = getStaffId(session.getName(), conn);
            int newTransactionId = getNextTransactionId(conn);

            double totalPrice = 0.0;

            for (int[] item : items) {
                int productId = item[0];
                int qty = item[1];
                double price = getDiscountedPrice(productId, date, conn);
                totalPrice += price * qty;

                insertIntoBills(newTransactionId, productId, price, qty, conn);
                updateInventory(session.getStoreId(), productId, qty, conn);
            }

            insertIntoTransactionRecords(newTransactionId, staffId, customerId, date, totalPrice, conn);

            System.out.printf("Transaction complete. Total Price: $%.2f\n", totalPrice);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        return 1; // If table is empty
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

        String discountSql = "SELECT DiscountPercent, ValidTillDate FROM Discount WHERE ProductID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(discountSql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String validTill = rs.getString("ValidTillDate");
                if (validTill != null && !LocalDate.parse(date).isAfter(LocalDate.parse(validTill))) {
                    discount = rs.getDouble("DiscountPercent");
                }
            }
        }

        return marketPrice * (1 - discount / 100);
    }

    private static void insertIntoTransactionRecords(int transId, int staffId, int custId,
                                                     String date, double total, Connection conn) throws SQLException {
        String sql = "INSERT INTO TransactionRecords (TransactionID, StaffID, CustomerID, Date, TotalPrice, Type) VALUES (?, ?, ?, ?, ?, 'Buy')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transId);
            stmt.setInt(2, staffId);
            stmt.setInt(3, custId);
            stmt.setString(4, date);
            stmt.setDouble(5, total);
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
}
