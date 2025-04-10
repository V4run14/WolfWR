package WolfWR;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.sql.*;
import java.util.Scanner;

public class InventoryManager {

    static final String jdbcURL = "jdbc:mariadb://classdb2.csc.ncsu.edu:3306/vvarath";
    static final String user = "vvarath";
    static final String passwd = "dbmsproj2025";
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            UserSession session = login(scanner);

            if (session != null) {
                showMenu(scanner, session);
            }
        }
    }

    // Authenticates staff and creates a user session if credentials are valid
    public static UserSession login(Scanner scanner) {
        System.out.print("Enter Staff Email: ");
        String inputEmail = scanner.nextLine();

        System.out.print("Enter Password: ");
        String inputPassword = scanner.nextLine();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(jdbcURL, user, passwd);
            // Prepare statement to verify credentials
            preparedStatement = connection.prepareStatement("SELECT * FROM Staff WHERE Email = ? AND Password = ?");

            preparedStatement.setString(1, inputEmail);
            preparedStatement.setString(2, inputPassword);

            result = preparedStatement.executeQuery();

            if (result.next()) {
                String name = result.getString("Name");
                int storeId = result.getInt("StoreID");
                String jobTitle = result.getString("JobTitle");
                System.out.println("Welcome, " + name + " (" + jobTitle + ") - Store ID: " + storeId);
                return new UserSession(name, jobTitle, storeId);
            } else {
                System.out.println("Invalid username or password. Please try again.\n");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            close(result);
            close(preparedStatement);
            close(connection);
        }
    }
    
    // Displays the main menu and routes user actions based on input
    private static void showMenu(Scanner scanner, UserSession session) {
        while (true) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Add Products to Store ");
            System.out.println("2. Transfer Products between Stores");
            System.out.println("3. Delete Expired products");
            System.out.println("4. Show Discounted Goods");
            System.out.println("5. Log Out");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();
            System.out.println();

            switch (choice) {
                case "1":
                	 addProductsToStore(scanner,session);;
                    break;
                case "2":
                	transferProductsBetweenStores(scanner,session);
                   
                    break;
                case "3":
                	deleteExpiredProducts(scanner, session);
                   
                    break;
                case "4":
                	 selectDiscounts(scanner,session);
                   
                    break;
                case "5":
                    System.out.println("Logging out...\n");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    //helper methods 
    private static int getIntInput(Scanner scanner, String message) {
        System.out.print(message);
        return Integer.parseInt(scanner.nextLine().trim());
    }

    private static Integer getNullableInt(Scanner scanner, String message) {
        int val = getIntInput(scanner, message);
        return (val == -1) ? null : val;
    }
    private static void addProductsToStore(Scanner scanner, UserSession session) {
        int storeId = session.getStoreId();
        String position = session.getJobTitle();

        if (position.equals("Cashier") || position.equals("Registration Office Operator")||position.equals("Billing Staff") ){
            System.out.println("You do not have access to this operation!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(jdbcURL, user, passwd)) {
            conn.setAutoCommit(false);

            // Step 1: Find expired or out-of-stock products in inventory
            String expiredOrZeroSQL =
                "SELECT DISTINCT si.ProductID " +
                "FROM StoreInventory si " +
                "JOIN Merchandise m ON si.ProductID = m.ProductID " +
                "WHERE si.StoreID = ? AND (si.Quantity <= 0 OR STR_TO_DATE(m.ExpireDate, '%m-%d-%Y') < CURDATE())";

            PreparedStatement productCheckStmt = conn.prepareStatement(expiredOrZeroSQL);
            productCheckStmt.setInt(1, storeId);
            ResultSet expiredOrOut = productCheckStmt.executeQuery();

            List<Integer> missingShipmentProductIds = new ArrayList<>();

            // Step 2: For each such product, check if a shipment exists
            while (expiredOrOut.next()) {
                int productId = expiredOrOut.getInt("ProductID");

                String shipmentCheckSQL =
                    "SELECT 1 FROM Shipments WHERE ProductID = ? AND Dest_StoreID = ? AND Source_StoreID IS NULL";
                try (PreparedStatement shipmentStmt = conn.prepareStatement(shipmentCheckSQL)) {
                    shipmentStmt.setInt(1, productId);
                    shipmentStmt.setInt(2, storeId);
                    ResultSet rs = shipmentStmt.executeQuery();
                    if (!rs.next()) {
                        missingShipmentProductIds.add(productId);
                    }
                }
            }

            // Step 3: Prompt user to create shipments for missing ones
            for (int productId : missingShipmentProductIds) {
                System.out.println("⚠️ No shipment found for ProductID " + productId);
                System.out.print("Do you want to create a shipment for this product? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();

                if (response.equals("yes")) {
                    createShipment(scanner, session,conn,productId);
                } else {
                    System.out.println("⏭️ Skipping ProductID " + productId);
                }
            }
           
            // Step 4: Get all shipment records that are now eligible to be added
            String shipmentEligibleSQL =
                "SELECT s.ShipmentID, s.SupplierID, s.ProductID, s.Quantity " +
                "FROM Shipments s " +
                "WHERE s.Dest_StoreID = ? AND s.Source_StoreID IS NULL " +
                "AND NOT EXISTS ( " +
                "    SELECT 1 FROM StoreInventory si " +
                "    JOIN Merchandise m ON si.ProductID = m.ProductID " +
                "    WHERE si.StoreID = ? AND si.ProductID = s.ProductID " +
                "    AND si.Quantity > 0 AND (STR_TO_DATE(m.ExpireDate,'%m-%d-%Y') IS NULL OR STR_TO_DATE(m.ExpireDate, '%m-%d-%Y') >= CURDATE())" +
                ")";

            PreparedStatement fetchShipmentStmt = conn.prepareStatement(shipmentEligibleSQL);
            fetchShipmentStmt.setInt(1, storeId);
            fetchShipmentStmt.setInt(2, storeId);

            ResultSet eligibleShipments = fetchShipmentStmt.executeQuery();

            // Step 5: Display them to the user
            System.out.printf("\n📦 Eligible Shipments for StoreID %d:\n", storeId);
            System.out.println("+------------+------------+-----------+----------+");
            System.out.printf("| %-10s | %-10s | %-9s | %-8s |\n", "ShipmentID", "SupplierID", "ProductID", "Quantity");
            System.out.println("+------------+------------+-----------+----------+");

            List<int[]> shipmentList = new ArrayList<>();
            while (eligibleShipments.next()) {
                int shipmentId = eligibleShipments.getInt("ShipmentID");
                int supplierId = eligibleShipments.getInt("SupplierID");
                int productId = eligibleShipments.getInt("ProductID");
                int quantity = eligibleShipments.getInt("Quantity");

                System.out.printf("| %-10d | %-10d | %-9d | %-8d |\n", shipmentId, supplierId, productId, quantity);
                shipmentList.add(new int[]{productId, quantity});
            }
            System.out.println("+------------+------------+-----------+----------+");

            if (shipmentList.isEmpty()) {
                System.out.println("⚠️ No eligible products to add even after checking new shipments.");
                return;
            }

            // Step 6: Insert them into StoreInventory
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO StoreInventory (StoreID, ProductID, Quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE Quantity = VALUES(Quantity)"
            );

            for (int[] item : shipmentList) {
                insertStmt.setInt(1, storeId);
                insertStmt.setInt(2, item[0]);
                insertStmt.setInt(3, item[1]);
                insertStmt.executeUpdate();
            }

            printStoreInventoryFromConnection(conn,session);
         // Step 7: Delete shipment records for added products
            PreparedStatement deleteShipmentsStmt = conn.prepareStatement(
                "DELETE FROM Shipments WHERE Dest_StoreID = ? AND Source_StoreID IS NULL AND ProductID = ?"
            );

            for (int[] item : shipmentList) {
                deleteShipmentsStmt.setInt(1, storeId);
                deleteShipmentsStmt.setInt(2, item[0]); // item[0] = ProductID
                deleteShipmentsStmt.executeUpdate();
            }

            // Step 8: Confirm and commit
            if (getUserConfirmation("Add products to Inventory?")) {
                conn.commit();
                System.out.println("✅ Transaction committed: Products added from shipment.");
            } else {
                conn.rollback();
                System.out.println("❌ Transaction rolled back.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error processing product addition.");
        }
    }


       // Create shipment record in the database when doing merchandise shipment from Supllier to Store
    private static void createShipment(Scanner scanner, UserSession session,Connection conn, Integer defaultProductId) {
        createShipment(scanner, session,conn, null, session.getStoreId(), defaultProductId, null);
    }

  // Overloaded method to create a shipment for transfer when doing Store to Store Transfer
    private static void createShipment(Scanner scanner, UserSession session,Connection conn,
                                       Integer sourceStore, Integer destStore,
                                       Integer defaultProductId, Integer defaultQty) {
        try (
          
        		PreparedStatement pstmt = conn.prepareStatement(
        			    "INSERT INTO Shipments (ShipmentID, SupplierID, ProductID, Quantity, Source_StoreID, Dest_StoreID) " +
        			    "VALUES (?, (SELECT SupplierID FROM Merchandise WHERE ProductID = ?), ?, ?, ?, ?)"
        			);
            
        ) {
            System.out.print("Enter Shipment ID: ");
            int shipmentId = Integer.parseInt(scanner.nextLine().trim());
           
            int productId = defaultProductId != null ? defaultProductId : getIntInput(scanner, "Enter Product ID: ");
            int quantity = defaultQty != null ? defaultQty : getIntInput(scanner, "Enter Quantity: ");

            Integer sourceStoreId = sourceStore != null ? sourceStore : getNullableInt(scanner, "Enter Source Store ID (-1 for NULL): ");
            Integer destStoreId = destStore != null ? destStore : session.getStoreId();

            pstmt.setInt(1, shipmentId);
            pstmt.setInt(2, productId); // for subquery
            pstmt.setInt(3, productId);
            pstmt.setInt(4, quantity);

            if (sourceStoreId == null) pstmt.setNull(5, Types.INTEGER);
            else pstmt.setInt(5, sourceStoreId);

            pstmt.setInt(6, destStoreId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Shipment created successfully.");
            } else {
                System.out.println("❌ Shipment creation failed.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error creating shipment.");
        }
    }


     // Displays current state of the StoreInventory table
    public static void printStoreInventoryFromConnection(Connection conn,UserSession session) {
    	int storeId = session.getStoreId();  // Get the store ID from the current session
        String sql = "SELECT * FROM StoreInventory WHERE StoreID = ? ORDER BY ProductID";

        try (
        		PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
        	stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("\n📦 StoreInventory (Current Session View):");
            System.out.println("StoreID | ProductID | Quantity");
            System.out.println("-------------------------------");
            while (rs.next()) {
                int productId = rs.getInt("ProductID");
                int quantity = rs.getInt("Quantity");
                System.out.printf("%7d | %9d | %8d%n", storeId, productId, quantity);
            }
            System.out.println();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Transfers product from one store to another
    private static void transferProductsBetweenStores(Scanner scanner, UserSession session) {
    	 String position = session.getJobTitle();
         if (position.equals("Cashier") || position.equals("Registration Office Operator")||position.equals("Billing Staff") ){
             System.out.println("You do not have access to this report!");
             return;
         }

        System.out.print("Enter destination Store ID: ");
        int destStore = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter Product ID to transfer: ");
      
        try (
                Connection conn = DriverManager.getConnection(jdbcURL, user, passwd)
            ) {
                conn.setAutoCommit(false);
     // Step 1: Get eligible product IDs at destination (out of stock or expired)
                Set<Integer> eligibleProductIds = new HashSet<>();

             // 1. Products expired/out of stock at destination AND present at source
             String queryExpiredOrZeroButPresent =
                 "SELECT DISTINCT si.ProductID " +
                 "FROM StoreInventory si " +
                 "JOIN Merchandise m ON si.ProductID = m.ProductID " +
                 "WHERE si.StoreID = ? " +
                 "AND (si.Quantity <= 0 OR STR_TO_DATE(m.ExpireDate, '%m-%d-%Y') < CURDATE()) " +
                 "AND EXISTS (SELECT 1 FROM StoreInventory src WHERE src.StoreID = ? AND src.ProductID = si.ProductID AND src.Quantity > 0)";

             try (PreparedStatement stmt = conn.prepareStatement(queryExpiredOrZeroButPresent)) {
                 stmt.setInt(1, destStore);
                 stmt.setInt(2, session.getStoreId());
                 ResultSet rs = stmt.executeQuery();
                 while (rs.next()) {
                     eligibleProductIds.add(rs.getInt("ProductID"));
                 }
             }

             // 2. Products present in source but not at all in destination
             String queryMissingAtDest =
                 "SELECT DISTINCT src.ProductID " +
                 "FROM StoreInventory src " +
                 "WHERE src.StoreID = ? AND src.Quantity > 0 " +
                 "AND NOT EXISTS (SELECT 1 FROM StoreInventory dst WHERE dst.StoreID = ? AND dst.ProductID = src.ProductID)";

             try (PreparedStatement stmt = conn.prepareStatement(queryMissingAtDest)) {
                 stmt.setInt(1, session.getStoreId());
                 stmt.setInt(2, destStore);
                 ResultSet rs = stmt.executeQuery();
                 while (rs.next()) {
                     eligibleProductIds.add(rs.getInt("ProductID"));
                 }
             }

             if (eligibleProductIds.isEmpty()) {
            	    System.out.println("❌ No eligible products available for transfer.");
            	    return;
            	}

            	// Display eligible product IDs
            	System.out.println("Eligible products for transfer:");
            	for (int pid : eligibleProductIds) {
            	    System.out.println(" - Product ID: " + pid);
            	}

     // Step 2: Prompt user for valid product ID
     int productId = -1;
     while (true) {
         System.out.print("Enter Product ID to transfer from the above list: ");
         productId = Integer.parseInt(scanner.nextLine().trim());
         if (eligibleProductIds.contains(productId)) {
             break;
         }
         System.out.println("❗ Invalid choice. Please choose a Product ID from the eligible list.");
     }
     int availableQty = 0;

     String qtyQuery = "SELECT Quantity FROM StoreInventory WHERE StoreID = ? AND ProductID = ?";
     try (PreparedStatement qtyStmt = conn.prepareStatement(qtyQuery)) {
         qtyStmt.setInt(1,session.getStoreId());
         qtyStmt.setInt(2, productId);
         ResultSet qtyRs = qtyStmt.executeQuery();
         if (qtyRs.next()) {
             availableQty = qtyRs.getInt("Quantity");
            // System.out.println("✅ Available quantity in your store: " + availableQty);
         } else {
             System.out.println("⚠️ Product not found in your inventory anymore.");
             return;
         }
     }

     // Step 3: Ask for quantity with context
     int qty = getValidQuantityInput(scanner, availableQty);


        int sourceStore = session.getStoreId();

      
           
            // Check if shipment exists for this transfer
            String shipmentCheck =
                "SELECT 1 FROM Shipments " +
                "WHERE ProductID = ? AND Source_StoreID = ? AND Dest_StoreID = ?";

            try (PreparedStatement shipmentStmt = conn.prepareStatement(shipmentCheck)) {
                shipmentStmt.setInt(1, productId);
                shipmentStmt.setInt(2, sourceStore);
                shipmentStmt.setInt(3, destStore);

                ResultSet shipmentRs = shipmentStmt.executeQuery();
                if (!shipmentRs.next()) {
                    System.out.println("⚠️ No shipment record found for this transfer.");
                    System.out.print("Do you want to create one now? (yes/no): ");
                    String response = scanner.nextLine().trim().toLowerCase();
                    if (response.equals("yes")) {
                        createShipment(scanner, session,conn,sourceStore, destStore, productId, qty);
                    } else {
                        System.out.println("Transfer cancelled due to missing shipment.");
                        return;
                    }
                }
            }

            // Proceed with transfer
            try (
                PreparedStatement deductStmt = conn.prepareStatement(
                    "UPDATE StoreInventory SET Quantity = Quantity - ? " +
                    "WHERE StoreID = ? AND ProductID = ? AND Quantity >= ?"
                );
                PreparedStatement addStmt = conn.prepareStatement(
                    "INSERT INTO StoreInventory (StoreID, ProductID, Quantity) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE Quantity =  VALUES(Quantity)"
                )
            ) {
                deductStmt.setInt(1, qty);
                deductStmt.setInt(2, sourceStore);
                deductStmt.setInt(3, productId);
                deductStmt.setInt(4, qty);
                deductStmt.executeUpdate();

                addStmt.setInt(1, destStore);
                addStmt.setInt(2, productId);
                addStmt.setInt(3, qty);
                addStmt.executeUpdate();
            }
            //: Delete used shipment records
            PreparedStatement deleteShipmentsStmt = conn.prepareStatement(
                "DELETE FROM Shipments WHERE Dest_StoreID = ? AND Source_StoreID = ? AND ProductID = ?"
            );

            
                deleteShipmentsStmt.setInt(1, destStore);
                deleteShipmentsStmt.setInt(2, sourceStore);
                deleteShipmentsStmt.setInt(3, productId);
                deleteShipmentsStmt.executeUpdate();
            
            if (getUserConfirmation("Transfer products")) {
                conn.commit();
                System.out.println("✅ Transaction committed: Transfer successful.");
            } else {
                conn.rollback();
                System.out.println("❌ Transaction rolled back.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error processing transfer.");
        }
    }
     // Deletes all expired products from inventory
    public static void deleteExpiredProducts(Scanner scanner, UserSession session) {
    	 String position = session.getJobTitle();
         if (position.equals("Cashier") || position.equals("Registration Office Operator")||position.equals("Billing Staff") ){
             System.out.println("You do not have access to this report!");
             return;
         }
        Connection conn = null;
        PreparedStatement pstmt = null;
        // SQL to show and delete expired inventory
        String showExpiredSQL =
                "SELECT si.ProductID,  m.ExpireDate, si.StoreID, si.Quantity " +
                "FROM StoreInventory si " +
                "JOIN Merchandise m ON si.ProductID = m.ProductID " +
                "WHERE STR_TO_DATE(m.ExpireDate, '%m-%d-%Y') < CURDATE()";
        String sql = "DELETE FROM StoreInventory WHERE ProductID IN (" +
                     "SELECT ProductID FROM Merchandise m WHERE STR_TO_DATE(m.ExpireDate, '%m-%d-%Y') < CURDATE())";

        try {
            conn = DriverManager.getConnection(jdbcURL, user, passwd);
            conn.setAutoCommit(false);
            try (PreparedStatement showStmt = conn.prepareStatement(showExpiredSQL);
                    ResultSet expiredRs = showStmt.executeQuery()) {

                   boolean found = false;
                   System.out.println("\n🕒 Expired Products *in StoreInventory*:");
                   System.out.println("StoreID | ProductID | Quantity | ExpireDate");
                   System.out.println("---------------------------------------------------------------");
                   while (expiredRs.next()) {
                       found = true;
                       int storeID = expiredRs.getInt("StoreID");
                       int pid = expiredRs.getInt("ProductID");                     
                       int qty = expiredRs.getInt("Quantity");
                       String date = expiredRs.getString("ExpireDate");
                       System.out.printf("%8d | %9d | %8d | %s%n", storeID, pid, qty, date);
                   }

                   if (!found) {
                       System.out.println("✅ No expired products in inventory.");
                       return;
                   }
               }
            pstmt = conn.prepareStatement(sql);
            int rowsDeleted = pstmt.executeUpdate();

            if (getUserConfirmation("Delete expired products")) {
                conn.commit();
                printStoreInventoryFromConnection(conn,session);
                System.out.println("✅ Transaction committed: Deleted " + rowsDeleted + " expired product(s).");
            } else {
                conn.rollback();
                System.out.println("❌ Transaction rolled back: Expired products not deleted.");
            }

        } catch (SQLException e) {
            rollbackSilently(conn);
            e.printStackTrace();
        } finally {
            close(pstmt);
            close(conn);
        }
    }
     // Displays products that currently have discounts > 0%
    public static void selectDiscounts(Scanner scanner, UserSession session) {
    
    	String position = session.getJobTitle();
        if (position.equals("Cashier") || position.equals("Registration Office Operator")||position.equals("Billing Staff") ){
            System.out.println("You do not have access to this report!");
            return;
        }
        try (
            Connection conn = DriverManager.getConnection(jdbcURL, user, passwd);
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Discount WHERE DiscountPercent > 0");
            ResultSet rs = pstmt.executeQuery()
        ) {
            System.out.println("✅ Discounts goods:");
            while (rs.next()) {
                int id = rs.getInt("ProductID");
                int percent = rs.getInt("DiscountPercent");
               // String desc = rs.getString("Description");
                System.out.println("ProductID: " + id + ", Percent: " + percent+"% " );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // === Utility methods ===

    static void rollbackSilently(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                System.out.println("❌ Rolled back due to error.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    // Asks user to confirm an action with yes/no
    static boolean getUserConfirmation(String actionName) {
        Scanner sc = new Scanner(System.in);
        System.out.print("🟡 Confirm commit for \"" + actionName + "\"? (yes/no): ");
        String input = sc.nextLine();
       
    
        return input.trim().equalsIgnoreCase("yes");
 
    }
    //Get Valid Quantity input when doing product transfers
    private static int getValidQuantityInput(Scanner scanner, int maxQty) {
        int qty = 0;
        while (true) {
            System.out.print("Enter quantity to transfer (max " + maxQty + "): ");
            try {
                qty = Integer.parseInt(scanner.nextLine().trim());
                if (qty > 0 && qty <= maxQty) {
                    return qty;
                } else {
                    System.out.println("❌ Please enter a number between 1 and " + maxQty + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
            }
        }
    }

    // Closes a JDBC Connection safely
    static void close(Connection conn) {
        if (conn != null) try { System.out.print("Connection Closed: ");conn.close(); } catch (Throwable ignored) {}
    }

    static void close(Statement st) {
        if (st != null) try { st.close(); } catch (Throwable ignored) {}
    }

    static void close(ResultSet rs) {
        if (rs != null) try { rs.close(); } catch (Throwable ignored) {}
    }
}
