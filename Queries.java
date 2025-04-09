package WolfWR;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import WolfWR.models.*;
/**
 * This class contains all the queries necessary for insert, delete and update
 * for suppliers, stores, clubmembers and staff
 */
public class Queries {

    /**
     * Insert one supplier into the database
     * @param s the supplier object to insert into the database
     * @param con the connection used in the database
     */
    public void singleInsertSupplier(Supplier s, Connection con) {

        PreparedStatement pstmt = null;

        try {

            pstmt  = con.prepareStatement("INSERT INTO Supplier (Name, Email, Location, PhoneNumber) Values(?,?,?,?)");

            pstmt.setString(1, s.getName());
            pstmt.setString(2, s.getEmail());
            pstmt.setString(3, s.getLocation());
            pstmt.setString(4, s.getPhoneNumber());

            pstmt.execute();
        }  catch (SQLException e) {
            throw new IllegalArgumentException("Error was found proccessing your request" + e);
        }
    }

    /**
     * Insert a single store into the database
     * @param s the store object to insert
     * @param con the connection used in to connect to the database
     */
    public void singleInsertStore(Store s, Connection con) {

        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement("INSERT INTO Store (Branch, PhoneNumber, Address) VALUES(?,?,?)");

            pstmt.setString(1, s.getBranch());
            pstmt.setString(2, s.getPhoneNumber());
            pstmt.setString(3, s.getAddress());

            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Insert a single club member into the database
     * @param cm the club member to insert
     * @param con the connection used for the insert
     */
    public void singleInsertClubMemb(ClubMember cm, Connection con) {
        PreparedStatement pstmt = null;

        try {

            pstmt = con.prepareStatement("INSERT INTO ClubMember (MembershipLevel, Address, Email, PhoneNumber, Fname, Lname, SignupDate, DueDate, ActiveStatus, LastPaid, StoreID, SignupStaffID) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");

            pstmt.setString(1, cm.getMembershipLevel());
            pstmt.setString(2, cm.getAddress());
            pstmt.setString(3, cm.getEmail());
            pstmt.setString(4, cm.getPhoneNumber());
            pstmt.setString(5, cm.getFirstName());
            pstmt.setString(6, cm.getLastName());
            java.sql.Date signUp = new java.sql.Date(cm.getSignUpDate().getTime());
            java.sql.Date dueDate = new java.sql.Date(cm.getDueDate().getTime());
            pstmt.setDate(7, signUp);
            pstmt.setDate(8, dueDate);
            java.sql.Date lastPaid = new java.sql.Date(cm.getLastPaid().getTime());
            pstmt.setInt(9, cm.getActivityStatus());
            pstmt.setDate(10, lastPaid);
            pstmt.setInt(11, cm.getStoreID());
            pstmt.setInt(12, cm.getSignupStaffID());

            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    /**
     * Insert a single staff member into the database
     * @param st the staff to insert
     * @param email the email of the staff to insert
     * @param pw the password of the staff to insert
     * @param con the connection to the database
     */
    public void singleInsertStaff(Staff st, String email, String pw, Connection con) {
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement("INSERT INTO Staff (StoreID, Name, Age, Address, PhoneNumber, JobTitle, Email, Password, TimeofEmployment) VALUES(?,?,?,?,?,?,?,?,?)");

            pstmt.setInt(1, st.getStoreID());
            pstmt.setString(2, st.getName());
            pstmt.setInt(3, st.getAge());
            pstmt.setString(4, st.getAddress());
            pstmt.setString(5, st.getPhoneNumber());
            pstmt.setString(6, st.getJobTitle());
            pstmt.setString(7, email);
            pstmt.setString(8, pw);
            java.sql.Date newDate = new java.sql.Date(st.getTimeOfEmployment().getTime());
            pstmt.setDate(9, newDate);

            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Delete a single supplier by the given name
     * @param name the name of the supplier to delete
     * @param con the connection to db
     */
    public void deleteSupplierByName(String name, Connection con) {

        PreparedStatement pstmt = null;
        try {
            pstmt = con.prepareStatement("DELETE FROM Supplier WHERE Name=?");
            pstmt.setString(1, name);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Deletes a store by the given branch
     * @param branch the branch of the store to delete from database
     * @param con the connection to the database
     */
    public void deleteStoreByBranch(String branch, Connection con) {
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement("DELETE FROM Store WHERE Branch = ?");
            pstmt.setString(1, branch);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Deletes a club member with the given first and last name
     * @param firstName the first name of the club member to delete
     * @param lastName the last name of the club member to delete
     * @param con the connection to the database
     */
    public void deleteClubMemb(String firstName, String lastName, Connection con) {
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement("DELETE FROM ClubMember WHERE Fname = ? AND Lname = ?");
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    /**
     * Deletes the staff with the given name from the database
     * @param name the name of the staff to delete
     * @param con the connection to use to the database
     */
    public void deleteStaff(String name, Connection con) {
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement("DELETE FROM Staff WHERE Name = ?");
            pstmt.setString(1, name);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Authenticates the staff member based on email and password
     * @param email the email of the user
     * @param password the password of the user
     * @param con the connection
     * @return returns the job title of the staff member
     */
    public String auth(String email, String password, Connection con) {

        String query = "SELECT JobTitle FROM Staff WHERE Email=? AND Password=?";
        String jobTitle = "";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                jobTitle = rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jobTitle;
    }
    /**
     * updates the name of the supplier using old name
     * @param oldName the old name of the supplier
     * @param newName the new name of the supplier
     * @param con the connection to the database
     */
    public void updateSupplierName(String oldName, String newName, Connection con) {
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement("UPDATE Supplier SET Name=? WHERE Name=?");

            pstmt.setString(1, newName);
            pstmt.setString(2, oldName);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }



    }
    /**
     * update the branch name of the given branch with the new one
     * @param oldBranch the name of the old branch
     * @param newBranch the name of the new branch to update with
     * @param con the connection to use
     */
    public void updateStoreBranch(String oldBranch, String newBranch, Connection con) {
        PreparedStatement pstmt = null;

        try {

            pstmt = con.prepareStatement("UPDATE Store SET Branch=? WHERE Branch=?");
            pstmt.setString(1, newBranch);
            pstmt.setString(2, oldBranch);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * update the membership level of the club member with the given first and last name
     * @param firstName first name of the club member to update
     * @param lastName last name of the club member to update
     * @param newMembLevel the new level to update to
     * @param con the connection to use
     */
    public void updateClubMemberLevel(String firstName, String lastName, String newMembLevel, Connection con) {
        PreparedStatement pstmt = null;

        try {

            pstmt = con.prepareStatement("UPDATE ClubMember SET MembershipLevel=? WHERE Fname=? AND Lname=?");
            pstmt.setString(1, newMembLevel);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    /**
     * Update the store id of a staff
     * @param name the name of the staff to update
     * @param newStoreId the store id to update to
     */
    public void updateStaffStoreId(String name, int newStoreId, Connection con) {
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement("UPDATE Staff SET StoreID=? WHERE Name=?");

            pstmt.setInt(1, newStoreId);
            pstmt.setString(2, name);

            pstmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancels (deactivates) a ClubMember by setting ActiveStatus to 0.
     * @param customerId the ID of the ClubMember to deactivate
     * @param con the database connection
     */
    public void cancelClubMemberMembership(int customerId, Connection con) {
        PreparedStatement pstmt = null;
        try {
            pstmt = con.prepareStatement("UPDATE ClubMember SET ActiveStatus = 0 WHERE CustomerID = ?");
            pstmt.setInt(1, customerId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("ClubMember with CustomerID " + customerId + " has been deactivated.");
            } else {
                System.out.println("No ClubMember found with CustomerID " + customerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}


