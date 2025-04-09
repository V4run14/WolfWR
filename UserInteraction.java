package WolfWR;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import WolfWR.models.*;

public class UserInteraction {
    public static void main(String[] args) {
        String jdbcURL = "jdbc:mariadb://classdb2.csc.ncsu.edu:3306/btsima";
        Connection con = null;
        String user = "btsima";
        String passwd = "mypwis54321";

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            con = DriverManager.getConnection(jdbcURL, user, passwd);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        Queries query = new Queries();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter Email: ");
        String loginemail = scanner.nextLine();
        System.out.println("Enter Password: ");
        String loginpassword = scanner.nextLine();
        String title = query.auth(loginemail, loginpassword, con);

        String[] menuOptions = {
                "Insert Store",
                "Insert ClubMember",
                "Insert Staff",
                "Insert Supplier",
                "Delete Club Member",
                "Delete Store",
                "Delete Staff",
                "Delete Supplier",
                "Update Supplier Name",
                "Update Store Branch",
                "Update ClubMember membership level",
                "Update StoreId for staff",
                "Cancel ClubMember Membership",
                "Logout"
        };

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        while (true) {
            System.out.println("\nMenu----------");
            for (int i = 0; i < menuOptions.length; i++) {
                System.out.println((i + 1) + ". " + menuOptions[i]);
            }
            System.out.print("\nEnter your choice (1-" + menuOptions.length + "): ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (choice < 1 || choice > menuOptions.length) {
                System.out.println("Invalid choice. Please try again.");
                continue;
            }

            String selectedOption = menuOptions[choice - 1];
            if (selectedOption.equals("Logout")) {
                System.out.println("Logging out...");
                break;
            }

            switch (selectedOption) {
                case "Insert Supplier":
                    if (title.equals("Administrator") || title.equals("Warehouse Operator") || title.equals("Manager")) {
                        System.out.println("Enter Supplier Name: ");
                        String name = scanner.nextLine();
                        System.out.println("Enter Supplier Email: ");
                        String email = scanner.nextLine();
                        System.out.println("Enter Supplier Location: ");
                        String location = scanner.nextLine();
                        System.out.println("Enter Supplier PhoneNumber: ");
                        String phoneNumber = scanner.nextLine();
                        Supplier s = new Supplier(name, email, location, phoneNumber);
                        query.singleInsertSupplier(s, con);
                    }
                    break;
                case "Insert Store":
                    if (title.equals("Administrator") ) {
                        System.out.println("Enter Store Branch: ");
                        String branch = scanner.nextLine();
                        System.out.println("Enter Store Address: ");
                        String address = scanner.nextLine();
                        System.out.println("Enter Store Phone Number (xxx-xxx-xxxx): ");
                        String phone = scanner.nextLine();
                        Store store = new Store(branch, phone, address);
                        query.singleInsertStore(store, con);
                    }
                    break;
                case "Insert ClubMember":
                    if (title.equals("Registration Office Operator") || title.equals("Administrator")) {
                        try {
                            System.out.println("Enter Membership Level: ");
                            String membShipLevel = scanner.nextLine();
                            System.out.println("Enter Address: ");
                            String address = scanner.nextLine();
                            System.out.println("Enter Email: ");
                            String email = scanner.nextLine();
                            System.out.println("Enter Phone Number (xxx-xxx-xxxx): ");
                            String phone = scanner.nextLine();
                            System.out.println("Enter First Name: ");
                            String firstName = scanner.nextLine();
                            System.out.println("Enter Last Name: ");
                            String lastName = scanner.nextLine();
                            System.out.println("Enter Signup Date (yyyy-MM-dd): ");
                            Date signUpDate = formatter.parse(scanner.nextLine());
                            System.out.println("Enter Due Date (yyyy-MM-dd): ");
                            Date dueDate = formatter.parse(scanner.nextLine());
                            System.out.println("Enter Membership Active Status (0 or 1): ");
                            int status = Integer.parseInt(scanner.nextLine());
                            System.out.println("Enter Last Paid (yyyy-MM-dd): ");
                            Date lastPaid = formatter.parse(scanner.nextLine());
                            System.out.println("Enter Store ID: ");
                            int storeId = Integer.parseInt(scanner.nextLine());
                            System.out.println("Enter Staff ID: ");
                            int staffId = Integer.parseInt(scanner.nextLine());
                            ClubMember cmb = new ClubMember(membShipLevel, address, email, phone, firstName, lastName, signUpDate, dueDate, status, lastPaid, storeId, staffId);
                            query.singleInsertClubMemb(cmb, con);
                        } catch (ParseException | NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "Insert Staff":
                    if (title.equals("Administrator")) {
                        try {
                            System.out.println("Enter Name: ");
                            String name = scanner.nextLine();
                            System.out.println("Enter Address: ");
                            String address = scanner.nextLine();
                            System.out.println("Enter Job Title: ");
                            String job = scanner.nextLine();
                            System.out.println("Enter Email: ");
                            String email = scanner.nextLine();
                            System.out.println("Enter Password: ");
                            String pass = scanner.nextLine();
                            System.out.println("Enter Date of Employment (yyyy-MM-dd): ");
                            Date empDate = formatter.parse(scanner.nextLine());
                            System.out.println("Enter Store ID: ");
                            int storeId = Integer.parseInt(scanner.nextLine());
                            System.out.println("Enter Age: ");
                            int age = Integer.parseInt(scanner.nextLine());
                            System.out.println("Enter Phone Number (xxx-xxx-xxxx): ");
                            String phone = scanner.nextLine();
                            Staff s = new Staff(storeId, name, age, address, phone, job, email, pass, empDate);
                            query.singleInsertStaff(s, email, pass, con);
                        } catch (ParseException | NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "Delete Supplier":
                    if (title.equals("Administrator") || title.equals("Warehouse Operator") || title.equals("Manager")) {
                        System.out.println("Enter Supplier Name: ");
                        String name = scanner.nextLine();
                        query.deleteSupplierByName(name, con);
                    }
                    break;
                case "Delete Store":
                    if (title.equals("Administrator") ) {
                        System.out.println("Enter Branch Name: ");
                        String branch = scanner.nextLine();
                        query.deleteStoreByBranch(branch, con);
                    }
                    break;
                case "Delete Club Member":
                    if (title.equals("Registration Operator") || title.equals("Administrator")) {
                        System.out.println("Enter First Name: ");
                        String first = scanner.nextLine();
                        System.out.println("Enter Last Name: ");
                        String last = scanner.nextLine();
                        query.deleteClubMemb(first, last, con);
                    }
                    break;
                case "Delete Staff":
                    if (title.equals("Administrator")) {
                        System.out.println("Enter Staff Name: ");
                        String name = scanner.nextLine();
                        query.deleteStaff(name, con);
                    }
                    break;
                case "Update Supplier Name":
                    if (title.equals("Administrator") || title.equals("Warehouse Operator") || title.equals("Manager")) {
                        System.out.println("Enter Old Supplier Name: ");
                        String oldName = scanner.nextLine();
                        System.out.println("Enter New Supplier Name: ");
                        String newName = scanner.nextLine();
                        query.updateSupplierName(oldName, newName, con);
                    }
                    break;
                case "Update Store Branch":
                    if (title.equals("Administrator") || title.equals("Manager")) {
                        System.out.println("Enter Old Branch Name: ");
                        String oldBranch = scanner.nextLine();
                        System.out.println("Enter New Branch Name: ");
                        String newBranch = scanner.nextLine();
                        query.updateStoreBranch(oldBranch, newBranch, con);
                    }
                    break;
                case "Update ClubMember membership level":
                    if (title.equals("Registration Operator") || title.equals("Administrator")) {
                        System.out.println("Enter First Name: ");
                        String first = scanner.nextLine();
                        System.out.println("Enter Last Name: ");
                        String last = scanner.nextLine();
                        System.out.println("Enter New Membership Level: ");
                        String level = scanner.nextLine();
                        query.updateClubMemberLevel(first, last, level, con);
                    }
                    break;
                case "Update StoreId for staff":
                    if (title.equals("Administrator")) {
                        System.out.println("Enter Staff Name: ");
                        String staffName = scanner.nextLine();
                        System.out.println("Enter New Store ID: ");
                        int newStoreId = Integer.parseInt(scanner.nextLine());
                        query.updateStaffStoreId(staffName, newStoreId, con);
                    }
                    break;
                case "Cancel ClubMember Membership":
                    if (title.equals("Registration Office Operator") || title.equals("Administrator")) {
                        System.out.println("Enter Customer ID to cancel membership: ");
                        int customerId = Integer.parseInt(scanner.nextLine());
                        query.cancelClubMemberMembership(customerId, con); // ✅ Method call
                    }
                    break;
            }
        }

        scanner.close();
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}