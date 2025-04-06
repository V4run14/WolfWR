package Proj;
import java.sql.*;
import java.util.Scanner;

public class Reports {

    private static final String jdbcURL = "jdbc:mariadb://classdb2.csc.ncsu.edu:3306/vvarath"; // Using SERVICE_NAME
    private static final String user = "vvarath";
    private static final String password = "dbmsproj2025";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            UserSession session = login(scanner);

            if (session != null) {
                showMenu(scanner, session);
            }
        }
    }

    static void close(Connection connection) {
        if(connection != null) {
            try {
                connection.close();
            } catch(Throwable whatever) {}
        }
    }
    static void close(Statement statement) {
        if(statement != null) {
            try {
                statement.close();
            } catch(Throwable whatever) {}
        }
    }
    static void close(ResultSet result) {
        if(result != null) {
            try {
                result.close();
            } catch(Throwable whatever) {}
        }
    }

    private static UserSession login(Scanner scanner) {
        System.out.print("Enter Staff Email: ");
        String inputEmail = scanner.nextLine();

        System.out.print("Enter Password: ");
        String inputPassword = scanner.nextLine();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(jdbcURL, user, password);
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

    private static void showMenu(Scanner scanner, UserSession session) {
        while (true) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Generate Sales Report (By Day / Month / Year)");
            System.out.println("2. Generate Growth Sales Report");
            System.out.println("3. Generate Merchandise Stock Report");
            System.out.println("4. Generate Customer Growth Report (By Month / Year)");
            System.out.println("5. Generate Customer Activity Report");
            System.out.println("6. Log Out");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();
            System.out.println();

            switch (choice) {
                case "1":
                    generateSalesReport(scanner, session);
                    break;
                case "2":
                    generateGrowthSalesReport(scanner, session);
                    break;
                case "3":
                    generateMerchandiseStockReport(scanner, session);
                    break;
                case "4":
                    generateCustomerGrowthReport(scanner, session);
                    break;
                case "5":
                    generateCustomerActivityReport(scanner, session);
                    break;
                case "6":
                    System.out.println("Logging out...\n");
                    return; // Exits menu and returns to login prompt
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void generateSalesReport(Scanner scanner, UserSession session) {
        String position = session.getJobTitle();
        if (position.equals("Cashier") || position.equals("Registration Office Operator") || position.equals("Warehouse Operator")){
            System.out.println("You do not have access to this report!");
            return;
        }

        System.out.println();
        System.out.println("1. By Day");
        System.out.println("2. By Month");
        System.out.println("3. By Year");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();
        System.out.println();

        String query = "";

        if (choice == 1) {
            query = "SELECT Date, SUM(TotalPrice) AS Total_Sales " +
                    "FROM TransactionRecords " +
                    "WHERE Type = 'Buy' " +
                    "GROUP BY Date " +
                    "ORDER BY Date;";
        } else if (choice == 2) {
            query = "SELECT DATE_FORMAT(Date, '%Y-%m') AS Month, SUM(TotalPrice) AS Total_Sales " +
                    "FROM TransactionRecords " +
                    "WHERE Type = 'Buy' " +
                    "GROUP BY Month " +
                    "ORDER BY Month;";
        } else if (choice == 3) {
            query = "SELECT YEAR(Date) AS Year, SUM(TotalPrice) AS Total_Sales " +
                    "FROM TransactionRecords " +
                    "WHERE Type = 'Buy' " +
                    "GROUP BY Year " +
                    "ORDER BY Year;";
        } else {
            System.out.println("Invalid choice. Please try again.");
            return;
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(jdbcURL, user, password);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            System.out.println("+------------+--------------+");
            if (choice == 1) {
                System.out.printf("| %-10s | %-12s |\n", "Date", "Total_Sales");
            } else if (choice == 2) {
                System.out.printf("| %-10s | %-12s |\n", "Month", "Total_Sales");
            } else {
                System.out.printf("| %-10s | %-12s |\n", "Year", "Total_Sales");
            }
            System.out.println("+------------+--------------+");

            while (resultSet.next()) {
                if (choice == 1) {
                    System.out.printf("| %-10s | %-12.2f |\n",
                            resultSet.getString("Date"),
                            resultSet.getDouble("Total_Sales"));
                } else if (choice == 2) {
                    System.out.printf("| %-10s | %-12.2f |\n",
                            resultSet.getString("Month"),
                            resultSet.getDouble("Total_Sales"));
                } else if (choice == 3) {
                    System.out.printf("| %-10s | %-12.2f |\n",
                            resultSet.getInt("Year"),
                            resultSet.getDouble("Total_Sales"));
                }
            }

            System.out.println("+------------+--------------+");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error retrieving sales report.");
        } finally {
            close(resultSet);
            close(statement);
            close(connection);
        }
    }

    private static void generateGrowthSalesReport(Scanner scanner, UserSession session) {
        String position = session.getJobTitle();
        if (position.equals("Cashier") || position.equals("Registration Office Operator") || position.equals("Warehouse Operator")){
            System.out.println("You do not have access to this report!");
            return;
        }

        System.out.print("Enter start date (YYYY-MM-DD): ");
        String startDate = scanner.next();

        System.out.print("Enter end date (YYYY-MM-DD): ");
        String endDate = scanner.next();

        scanner.nextLine();
        System.out.println();
        int storeId = session.getStoreId();

        String query = "WITH StoreCashiers AS ( " +
                "    SELECT StaffID FROM Staff " +
                "    WHERE StoreID = ? AND JobTitle = 'Cashier' " +
                "), StoreTransactions AS ( " +
                "    SELECT t.TransactionID, t.StaffID, t.Date, t.TotalPrice " +
                "    FROM TransactionRecords t " +
                "    INNER JOIN StoreCashiers sc ON t.StaffID = sc.StaffID " +
                "    WHERE t.Date BETWEEN ? AND ? " +
                ") " +
                "SELECT " +
                "    DATE_FORMAT(Date, '%Y-%m') AS Month, " +
                "    COUNT(TransactionID) AS TotalTransactions, " +
                "    SUM(TotalPrice) AS TotalSales, " +
                "    LAG(SUM(TotalPrice)) OVER (ORDER BY DATE_FORMAT(Date, '%Y-%m')) AS PreviousMonthSales, " +
                "    (SUM(TotalPrice) - LAG(SUM(TotalPrice)) OVER (ORDER BY DATE_FORMAT(Date, '%Y-%m'))) " +
                "        / NULLIF(LAG(SUM(TotalPrice)) OVER (ORDER BY DATE_FORMAT(Date, '%Y-%m')), 0) * 100 AS SalesGrowthPercentage " +
                "FROM StoreTransactions " +
                "GROUP BY Month " +
                "ORDER BY Month;";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(jdbcURL, user, password);
            statement = connection.prepareStatement(query);

            statement.setInt(1, storeId);
            statement.setString(2, startDate);
            statement.setString(3, endDate);

            resultSet = statement.executeQuery();

            System.out.println("+---------+----------------+------------+------------------+----------------------+");
            System.out.printf("| %-7s | %-14s | %-10s | %-16s | %-20s |\n",
                    "Month", "TotalTransactions", "TotalSales", "PreviousMonthSales", "SalesGrowthPercentage");
            System.out.println("+---------+----------------+------------+------------------+----------------------+");

            while (resultSet.next()) {
                System.out.printf("| %-7s | %-14d | %-10.2f | %-16.2f | %-20.2f |\n",
                        resultSet.getString("Month"),
                        resultSet.getInt("TotalTransactions"),
                        resultSet.getDouble("TotalSales"),
                        resultSet.getDouble("PreviousMonthSales"),
                        resultSet.getDouble("SalesGrowthPercentage"));
            }

            System.out.println("+---------+----------------+------------+------------------+----------------------+");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error retrieving sales growth report.");
        } finally {
            close(resultSet);
            close(statement);
            close(connection);
        }
    }

    private static void generateMerchandiseStockReport(Scanner scanner, UserSession session) {

        String position = session.getJobTitle();
        if (position.equals("Cashier") || position.equals("Billing Staff") || position.equals("Registration Office Operatorw")){
            System.out.println("You do not have access to this report!");
            return;
        }

        System.out.println("\nGenerate Merchandise Stock Report:");
        System.out.println("1. Report for a Store");
        System.out.println("2. Report for a Product");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();
        System.out.println();

        String query = "";
        int parameter = 0;

        if (choice == 1) {
            int storeId = session.getStoreId();
            query = "SELECT " +
                    "    si.StoreID, " +
                    "    si.ProductID, " +
                    "    m.Name AS ProductName, " +
                    "    si.Quantity " +
                    "FROM StoreInventory si " +
                    "JOIN Merchandise m ON si.ProductID = m.ProductID " +
                    "WHERE si.StoreID = ?";
            parameter = storeId;

        } else if (choice == 2) {
            System.out.print("Enter Product ID: ");
            int productId = scanner.nextInt();
            query = "SELECT " +
                    "    si.StoreID, " +
                    "    si.ProductID, " +
                    "    m.Name AS ProductName, " +
                    "    si.Quantity " +
                    "FROM StoreInventory si " +
                    "JOIN Merchandise m ON si.ProductID = m.ProductID " +
                    "WHERE si.ProductID = ?";
            parameter = productId;

        } else {
            System.out.println("Invalid choice. Please enter 1 or 2.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(jdbcURL, user, password);
            statement = connection.prepareStatement(query);

            statement.setInt(1, parameter);

            resultSet = statement.executeQuery();

            System.out.println("+---------+-----------+----------------+----------+");
            System.out.printf("| %-7s | %-9s | %-14s | %-8s |\n",
                    "StoreID", "ProductID", "ProductName", "Quantity");
            System.out.println("+---------+-----------+----------------+----------+");


            while (resultSet.next()) {
                System.out.printf("| %-7d | %-9d | %-14s | %-8d |\n",
                        resultSet.getInt("StoreID"),
                        resultSet.getInt("ProductID"),
                        resultSet.getString("ProductName"),
                        resultSet.getInt("Quantity"));
            }

            System.out.println("+---------+-----------+----------------+----------+");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error retrieving merchandise stock report.");
        } finally {
            close(resultSet);
            close(statement);
            close(connection);
        }
    }

    private static void generateCustomerGrowthReport(Scanner scanner, UserSession session) {

        String position = session.getJobTitle();
        if (position.equals("Cashier") || position.equals("Billing Staff") || position.equals("Warehouse Operator")){
            System.out.println("You do not have access to this report!");
            return;
        }

        System.out.println("\nGenerate Customer Growth Report:");
        System.out.println("1. By Month");
        System.out.println("2. By Year");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();
        System.out.println();

        String query = "";

        if (choice == 1) {
            query = "WITH CustomerCount AS (" +
                    "    SELECT DATE_FORMAT(SignupDate, '%Y-%m') AS Month, " +
                    "           COUNT(CustomerID) AS NewCustomers " +
                    "    FROM ClubMember " +
                    "    GROUP BY Month " +
                    ") " +
                    "SELECT Month, " +
                    "       NewCustomers, " +
                    "       SUM(NewCustomers) OVER (ORDER BY Month) AS CumulativeCustomers " +
                    "FROM CustomerCount;";

        } else if (choice == 2) {
            query = "WITH CustomerCount AS (" +
                    "    SELECT YEAR(SignupDate) AS Year, " +
                    "           COUNT(CustomerID) AS NewCustomers " +
                    "    FROM ClubMember " +
                    "    GROUP BY Year " +
                    ") " +
                    "SELECT Year, " +
                    "       NewCustomers, " +
                    "       SUM(NewCustomers) OVER (ORDER BY Year) AS CumulativeCustomers " +
                    "FROM CustomerCount;";

        } else {
            System.out.println("Invalid choice. Please enter 1 or 2.");
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(jdbcURL, user, password);
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();

            if (choice == 1) {
                System.out.println("+---------+-------------+-------------------+");
                System.out.printf("| %-7s | %-11s | %-17s |\n", "Month", "NewCustomers", "CumulativeCustomers");
                System.out.println("+---------+-------------+-------------------+");
            } else {
                System.out.println("+------+-------------+-------------------+");
                System.out.printf("| %-4s | %-11s | %-17s |\n", "Year", "NewCustomers", "CumulativeCustomers");
                System.out.println("+------+-------------+-------------------+");
            }

            while (resultSet.next()) {
                if (choice == 1) {
                    System.out.printf("| %-7s | %-11d | %-17d |\n",
                            resultSet.getString("Month"),
                            resultSet.getInt("NewCustomers"),
                            resultSet.getInt("CumulativeCustomers"));
                } else {
                    System.out.printf("| %-4d | %-11d | %-17d |\n",
                            resultSet.getInt("Year"),
                            resultSet.getInt("NewCustomers"),
                            resultSet.getInt("CumulativeCustomers"));
                }
            }

            if (choice == 1) {
                System.out.println("+---------+-------------+-------------------+");
            } else {
                System.out.println("+------+-------------+-------------------+");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error retrieving customer growth report.");
        } finally {
            close(resultSet);
            close(statement);
            close(connection);
        }
    }

    private static void generateCustomerActivityReport(Scanner scanner, UserSession session) {

        String position = session.getJobTitle();
        if (position.equals("Cashier") || position.equals("Billing Staff") || position.equals("Warehouse Operator")){
            System.out.println("You do not have access to this report!");
            return;
        }

        System.out.println("\nGenerate Customer Activity Report:");

        System.out.print("Enter start date (YYYY-MM-DD): ");
        String startDate = scanner.next();
        System.out.print("Enter end date (YYYY-MM-DD): ");
        String endDate = scanner.next();

        scanner.nextLine();
        System.out.println();

        String query = "SELECT " +
                "    tr.CustomerID, " +
                "    cm.Fname, " +
                "    cm.Lname, " +
                "    SUM(TotalPrice) AS TotalPurchaseAmount " +
                "FROM TransactionRecords tr " +
                "JOIN ClubMember cm ON tr.CustomerID = cm.CustomerID " +
                "WHERE tr.Date BETWEEN ? AND ? " +
                "GROUP BY tr.CustomerID, cm.Fname, cm.Lname " +
                "ORDER BY TotalPurchaseAmount DESC;";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(jdbcURL, user, password);
            statement = connection.prepareStatement(query);

            statement.setString(1, startDate);
            statement.setString(2, endDate);

            resultSet = statement.executeQuery();

            System.out.println("+------------+------------+------------+---------------------+");
            System.out.printf("| %-10s | %-10s | %-10s | %-19s |\n", "CustomerID", "First Name", "Last Name", "Total Purchase ($)");
            System.out.println("+------------+------------+------------+---------------------+");

            while (resultSet.next()) {
                System.out.printf("| %-10d | %-10s | %-10s | %-19.2f |\n",
                        resultSet.getInt("CustomerID"),
                        resultSet.getString("Fname"),
                        resultSet.getString("Lname"),
                        resultSet.getDouble("TotalPurchaseAmount"));
            }

            System.out.println("+------------+------------+------------+---------------------+");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error retrieving customer activity report.");
        } finally {
            close(resultSet);
            close(statement);
            close(connection);
        }
    }
}
