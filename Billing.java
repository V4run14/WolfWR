/**
 * This program provides a menu-driven interface for billing operations and discounts
 * on merchandise. It interacts with a MariaDB database to perform staff authentication 
 * and a series of SQL operations including generating supplier bills, handling discounts, 
 * and calculating transaction totals.
 *
 * Functional Overview:
 * - Staff login: Prompts for email and password, then verifies staff credentials.
 * - Access Control: Only "Billing Staff" and "Administrator" are allowed to perform menu operations.
 * - Menu Operations: Operations include generating supplier bills, store-wise bills,
 *   rewarding platinum customers, handling discounts, calculating transaction totals,
 *   and viewing items on sale.
 */
package WolfWR;
import java.sql.*;
import java.util.Scanner;

public class Billing {
	//DB Credentials
    private static final String jdbcURL = "jdbc:mariadb://classdb2.csc.ncsu.edu:3306/vvarath";
    private static final String user = "vvarath";
    private static final String password = "dbmsproj2025";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter Email: ");
            String email = scanner.nextLine();

            System.out.print("Enter Password: ");
            String inputPassword = scanner.nextLine();

            Connection connection = null;

            try {
                connection = DriverManager.getConnection(jdbcURL, user, password);
                String designation = authenticateUser(email, inputPassword, connection);
                if (designation == null) {
                    System.out.println("Access denied.");
                    return;
                }

                System.out.println("Welcome, " + designation);
              //Access Control Condition for Staff
                if (!designation.equalsIgnoreCase("Billing Staff") && !designation.equalsIgnoreCase("Administrator")) {
                    System.out.println("Access for Billing Staff and Administrator only. Exiting");
                    return;
                }

                while (true) {
                	//Menu Operations
                    System.out.println("\n Choose an operation:");
                    System.out.println("1. Generate All Supplier Bills");
                    System.out.println("2. Generate Store-wise Supplier Bills");
                    System.out.println("3. Reward Platinum Customers");
                    System.out.println("4. Calculate Total Price for Transactions");
                    System.out.println("5. Generate Bill for Specific Supplier");
                    System.out.println("6. Generate Bill for Supplier for Specific Store");
                    System.out.println("7. Calculate Total Price for Specific Transactions");
                    System.out.println("8. Items on Sale");
                    System.out.println("9. Manage Discounts");
                    System.out.println("10. Exit");

                    System.out.print("Enter your choice (1-10): ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1 : generateSupplierBills(connection); break;
                        case 2 : generateStoreWiseSupplierBills(connection); break;
                        case 3 : rewardPlatinumCustomers(connection); break;
                        case 4 : calculateTotalTransactionPrice(connection); break;
                        case 5 : {
                            System.out.print("Enter Supplier ID: ");
                            int supplierId = scanner.nextInt();
                            scanner.nextLine();
                            generateBillForSpecificSupplier(connection, supplierId);
                        } break;
                        case 6 : {
                            System.out.print("Enter Store ID: ");
                            int storeID = scanner.nextInt();
                            scanner.nextLine();
                            generateSupplierBillsForSpecificStore(connection, storeID);
                        } break;
                        case 7 : {
                            System.out.print("Enter Transaction ID: ");
                            int transactionID = scanner.nextInt();
                            scanner.nextLine();
                            calculateTotalTransactionPriceForID(connection, transactionID);
                        } break;
                        case 8 : getItemsOnSale(connection); break;
                        case 9 : manageDiscounts(connection, scanner); break;
                        case 10 : {
                            System.out.println("Exiting");
                            return;
                        }
                        default : System.out.println("Invalid choice. Please try again.");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Database connection failed!");
                e.printStackTrace();
            } finally {
                close(connection);
            }
        }
    }

//Function to authenticate staff members
    private static String authenticateUser(String email, String inputPassword, Connection connection) {
        String designation = null;
        String query = "SELECT JobTitle FROM Staff WHERE Email = ? AND Password = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = connection.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, inputPassword);
            rs = stmt.executeQuery();
            if (rs.next()) {
                designation = rs.getString("JobTitle");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(stmt);
        }
        
        return designation;
    }

//Function to generate bills for suppliers
    private static void generateSupplierBills(Connection connection) {
        System.out.println("\nAll Supplier Bills");
        String query = "SELECT sh.SupplierID, sup.Name AS SupplierName, SUM(m.BuyPrice * sh.Quantity) AS TotalAmountDue " +
                       "FROM Shipments sh " +
                       "JOIN Merchandise m ON sh.ProductID = m.ProductID " +
                       "JOIN Supplier sup ON sh.SupplierID = sup.SupplierID " +
                       "GROUP BY sh.SupplierID, sup.Name";

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                System.out.printf("Supplier: %-25s  Amount Due: $%.2f\n",
                                  rs.getString("SupplierName"), rs.getDouble("TotalAmountDue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(stmt);
        }
    }

//Function to generate all supplier bills store-wise
    private static void generateStoreWiseSupplierBills(Connection connection) {
        System.out.println("\nStore-wise Supplier Bills");
        String query = "SELECT s.StoreID, s.Address AS StoreLocation, sup.Name AS SupplierName, " +
                       "SUM(sh.Quantity * m.BuyPrice) AS TotalAmountOwed " +
                       "FROM Store s " +
                       "JOIN Shipments sh ON s.StoreID = sh.Dest_StoreID " +
                       "JOIN Merchandise m ON sh.ProductID = m.ProductID " +
                       "JOIN Supplier sup ON m.SupplierID = sup.SupplierID " +
                       "GROUP BY s.StoreID, s.Address, sup.Name";
        
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.printf("Store: %-30s Supplier: %-25s  Amount Owed: $%.2f\n",
                        rs.getString("StoreLocation"),
                        rs.getString("SupplierName"),
                        rs.getDouble("TotalAmountOwed"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(stmt);
        }
    }
//Function to generate rewards for Platinum Members
    private static void rewardPlatinumCustomers(Connection connection) {
        System.out.println("\nPlatinum Customers Reward");
        String query = "SELECT c.CustomerID, CONCAT(c.Fname, ' ', c.Lname) AS CustomerName, " +
                       "SUM(t.TotalPrice) AS TotalSpent, SUM(t.TotalPrice) * 0.05 AS RewardAmount " +
                       "FROM TransactionRecords t " +
                       "JOIN ClubMember c ON t.CustomerID = c.CustomerID " +
                       "WHERE c.MembershipLevel = 'Platinum' AND YEAR(t.Date) = YEAR(CURDATE()) " +
                       "GROUP BY c.CustomerID HAVING TotalSpent > 0";

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.printf("Customer: %-30s  Reward: $%.2f\n",
                        rs.getString("CustomerName"),
                        rs.getDouble("RewardAmount"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(stmt);
        }
    }
//Function to Manage Discounts on Merchandise
    private static void manageDiscounts(Connection connection, Scanner scanner) {
        while (true) {
            System.out.println("\n--- Discount Management ---");
            System.out.println("1. Add Discount");
            System.out.println("2. Modify Discount");
            System.out.println("3. Delete Discount");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice (1-4): ");
            int option = scanner.nextInt();
            scanner.nextLine(); 

            switch (option) {
                case 1 : addDiscount(connection, scanner);
                case 2 : modifyDiscount(connection, scanner);
                case 3 : deleteDiscount(connection, scanner);
                case 4 : { return; }
                default : System.out.println("Invalid choice. Try again.");
            }
        }
    }
    //Function to add new Discount on Merchandise
    private static void addDiscount(Connection connection, Scanner scanner) {
        PreparedStatement pstmt = null;
        try {
            System.out.print("Enter Product ID: ");
            int productId = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter Discount Percent: ");
            int discountPercent = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter Valid From Date (YYYY-MM-DD): ");
            String fromDate = scanner.nextLine();

            System.out.print("Enter Valid Till Date (YYYY-MM-DD): ");
            String toDate = scanner.nextLine();

            String query = "INSERT INTO Discount (ProductID, DiscountPercent, ValidFromDate, ValidTillDate) VALUES (?, ?, ?, ?)";
            pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, productId);
            pstmt.setInt(2, discountPercent);
            pstmt.setDate(3, Date.valueOf(fromDate));
            pstmt.setDate(4, Date.valueOf(toDate));
            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0 ? "Discount added successfully." : "Failed to add discount.");
        } catch (Exception e) {
            System.out.println("Error while adding discount: " + e.getMessage());
        } finally {
            close(pstmt);
        }
    }

    private static void modifyDiscount(Connection connection, Scanner scanner) {
        PreparedStatement pstmt = null;
        try {
            System.out.print("Enter Product ID of Discount to Modify: ");
            int productId = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter New Discount Percent: ");
            int discountPercent = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter New Valid From Date (YYYY-MM-DD): ");
            String fromDate = scanner.nextLine();

            System.out.print("Enter New Valid Till Date (YYYY-MM-DD): ");
            String newToDate = scanner.nextLine();

            String query = "UPDATE Discount SET DiscountPercent = ?, ValidFromDate = ?, ValidTillDate = ? WHERE ProductID = ?";
            pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, discountPercent);
            pstmt.setDate(2, Date.valueOf(fromDate));
            pstmt.setDate(3, Date.valueOf(newToDate));
            pstmt.setInt(4, productId);

            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0 ? "Discount modified successfully." : "No matching discount found to modify.");
        } catch (Exception e) {
            System.out.println("Error while modifying discount: " + e.getMessage());
        } finally {
            close(pstmt);
        }
    }

    private static void deleteDiscount(Connection connection, Scanner scanner) {
        PreparedStatement pstmt = null;
        try {
            System.out.print("Enter Product ID: ");
            int productId = scanner.nextInt();
            scanner.nextLine();

            String query = "DELETE FROM Discount WHERE ProductID = ?";
            pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, productId);

            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0 ? "Discount deleted successfully." : "No matching discount found.");
        } catch (Exception e) {
            System.out.println("Error while deleting discount: " + e.getMessage());
        } finally {
            close(pstmt);
        }
    }


//Function to calculate the total price for all transactions
    private static void calculateTotalTransactionPrice(Connection connection) {
        System.out.println("\nTotal Transaction Price");
        String query = "SELECT tr.TransactionID, tr.Date, tr.CustomerID, c.Fname, c.Lname, " +
                       "SUM(b.Quantity * (b.Price * (1 - COALESCE(d.DiscountPercent, 0) / 100))) AS FinalTotalPrice " +
                       "FROM TransactionRecords tr " +
                       "JOIN Bills b ON tr.TransactionID = b.TransactionID " +
                       "JOIN ClubMember c ON tr.CustomerID = c.CustomerID " +
                       "LEFT JOIN Discount d ON b.ProductID = d.ProductID AND d.ValidTillDate >= CURDATE() " +
                       "WHERE tr.Type = 'Buy' " +
                       "GROUP BY tr.TransactionID, tr.Date, tr.CustomerID, c.Fname, c.Lname";
        
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.printf("Transaction ID: %-10d  Customer: %-20s  Date: %-12s  Final Total: $%.2f\n",
                        rs.getInt("TransactionID"),
                        rs.getString("Fname") + " " + rs.getString("Lname"),
                        rs.getDate("Date").toString(),
                        rs.getDouble("FinalTotalPrice"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(stmt);
        }
    }
    //Function to generate supplier bill for specific store, takes store ID as input
    private static void generateSupplierBillsForSpecificStore(Connection connection, int storeID) {
        System.out.println("\nSupplier Bills for Store ID: " + storeID);
        String query = "SELECT s.StoreID, s.Address AS StoreLocation, sup.Name AS SupplierName, " +
                       "SUM(sh.Quantity * m.BuyPrice) AS TotalAmountOwed " +
                       "FROM Store s " +
                       "JOIN Shipments sh ON s.StoreID = sh.Dest_StoreID " +
                       "JOIN Merchandise m ON sh.ProductID = m.ProductID " +
                       "JOIN Supplier sup ON m.SupplierID = sup.SupplierID " +
                       "WHERE s.StoreID = ? " +
                       "GROUP BY s.StoreID, s.Address, sup.Name";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, storeID);
            rs = pstmt.executeQuery();

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                System.out.printf("Store: %-30s Supplier: %-25s  Amount Owed: $%.2f\n",
                                  rs.getString("StoreLocation"),
                                  rs.getString("SupplierName"),
                                  rs.getDouble("TotalAmountOwed"));
            }
            if (!hasResults) {
                System.out.println("No data found for Store ID: " + storeID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(pstmt);
        }
    }

//Function to calculate total amount for a specific transaction, takes transaction ID as input
    private static void calculateTotalTransactionPriceForID(Connection connection, int transactionID) {
        System.out.println("\nTotal Transaction Price for Transaction ID: " + transactionID);
        String query = "SELECT tr.TransactionID, tr.Date, tr.CustomerID, c.Fname, c.Lname, " +
                       "SUM(b.Quantity * (b.Price * (1 - COALESCE(d.DiscountPercent, 0) / 100))) AS FinalTotalPrice " +
                       "FROM TransactionRecords tr " +
                       "JOIN Bills b ON tr.TransactionID = b.TransactionID " +
                       "JOIN ClubMember c ON tr.CustomerID = c.CustomerID " +
                       "LEFT JOIN Discount d ON b.ProductID = d.ProductID AND d.ValidTillDate >= CURDATE() " +
                       "WHERE tr.Type = 'Buy' AND tr.TransactionID = ? " +
                       "GROUP BY tr.TransactionID, tr.Date, tr.CustomerID, c.Fname, c.Lname";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, transactionID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.printf("Transaction ID: %-10d  Customer: %-20s  Date: %-12s  Final Total: $%.2f\n",
                                  rs.getInt("TransactionID"),
                                  rs.getString("Fname") + " " + rs.getString("Lname"),
                                  rs.getDate("Date").toString(),
                                  rs.getDouble("FinalTotalPrice"));
            } else {
                System.out.println("No transaction found with ID: " + transactionID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(pstmt);
        }
    }


    //Function to display all items on sale
    private static void getItemsOnSale(Connection connection) {
        System.out.println("\nItems Currently on Sale:");
        String query = "SELECT m.ProductID, m.Name, " +
                       "m.MarketPrice AS OriginalPrice, " +
                       "CASE " +
                       "WHEN d.DiscountPercent IS NOT NULL AND d.ValidTillDate >= CURDATE() THEN " +
                       "m.MarketPrice * (1 - d.DiscountPercent / 100) " +
                       "ELSE NULL " +
                       "END AS FinalDiscountPrice " +
                       "FROM Merchandise m " +
                       "LEFT JOIN Discount d ON m.ProductID = d.ProductID";

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                int productID = rs.getInt("ProductID");
                String name = rs.getString("Name");
                double originalPrice = rs.getDouble("OriginalPrice");
                double discountedPrice = rs.getDouble("FinalDiscountPrice");

                if (!rs.wasNull()) {
                    System.out.printf("Product ID: %-5d  Name: %-25s  Original: $%.2f  Discounted: $%.2f\n",
                                      productID, name, originalPrice, discountedPrice);
                }
            }

            if (!hasResults) {
                System.out.println("No items found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(stmt);
        }
    }


//Function to generate bill for a specific supplier, takes Supplier ID as input.
    private static void generateBillForSpecificSupplier(Connection connection, int supplierId) {
        System.out.println("\nSupplier Bill for ID: " + supplierId + " ---");
        String query = "SELECT sup.Name AS SupplierName, SUM(sh.Quantity * m.BuyPrice) AS AmountDue " +
                       "FROM Shipments sh " +
                       "JOIN Merchandise m ON sh.ProductID = m.ProductID " +
                       "JOIN Supplier sup ON m.SupplierID = sup.SupplierID " +
                       "WHERE sup.SupplierID = ? " +
                       "GROUP BY sup.Name";

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, supplierId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.printf("Supplier: %-25s  Amount Due: $%.2f\n",
                                  rs.getString("SupplierName"),
                                  rs.getDouble("AmountDue"));
            } else {
                System.out.println("No records found for this Supplier ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(stmt);
        }
    }


    static void close(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (Throwable whatever) {}
        }
    }

    static void close(Statement st) {
        if (st != null) {
            try { st.close(); } catch (Throwable whatever) {}
        }
    }

    static void close(ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (Throwable whatever) {}
        }
    
    }

}
