package WolfWR;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import WolfWR.models.*;

public class Queries {

    public UserSession loginAndGetSession(String email, String password, Connection con) {
        String query = "SELECT Name, JobTitle, StoreID FROM Staff WHERE Email=? AND Password=?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("Name");
                String jobTitle = rs.getString("JobTitle");
                int storeId = rs.getInt("StoreID");
                return new UserSession(name, jobTitle, storeId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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
    public void singleInsertClubMemb(ClubMember cm, Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement(
                "INSERT INTO ClubMember (MembershipLevel, Address, Email, PhoneNumber, Fname, Lname, SignupDate, DueDate, ActiveStatus, LastPaid, StoreID, SignupStaffID) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)")) {

            pstmt.setString(1, cm.getMembershipLevel());
            pstmt.setString(2, cm.getAddress());
            pstmt.setString(3, cm.getEmail());
            pstmt.setString(4, cm.getPhoneNumber());
            pstmt.setString(5, cm.getFirstName());
            pstmt.setString(6, cm.getLastName());
            pstmt.setDate(7, new java.sql.Date(cm.getSignUpDate().getTime()));
            pstmt.setDate(8, new java.sql.Date(cm.getDueDate().getTime()));
            pstmt.setInt(9, cm.getActivityStatus());
            pstmt.setDate(10, new java.sql.Date(cm.getLastPaid().getTime()));
            pstmt.setInt(11, cm.getStoreID());
            pstmt.setInt(12, cm.getSignupStaffID());

            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteClubMembById(int customerId, Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM ClubMember WHERE CustomerID = ?")) {
            pstmt.setInt(1, customerId);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteStoreById(int storeId, Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM Store WHERE StoreID = ?")) {
            pstmt.setInt(1, storeId);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteStaffById(int staffId, Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM Staff WHERE StaffID = ?")) {
            pstmt.setInt(1, staffId);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteSupplierById(int supplierId, Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM Supplier WHERE SupplierID = ?")) {
            pstmt.setInt(1, supplierId);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSupplierNameById(int supplierId, String newName, Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("UPDATE Supplier SET Name = ? WHERE SupplierID = ?")) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, supplierId);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStoreBranchById(int storeId, String newBranch, Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("UPDATE Store SET Branch = ? WHERE StoreID = ?")) {
            pstmt.setString(1, newBranch);
            pstmt.setInt(2, storeId);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateClubMemberLevelById(int customerId, String newLevel, Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("UPDATE ClubMember SET MembershipLevel = ? WHERE CustomerID = ?")) {
            pstmt.setString(1, newLevel);
            pstmt.setInt(2, customerId);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStaffStoreIdById(int staffId, int newStoreId, Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("UPDATE Staff SET StoreID = ? WHERE StaffID = ?")) {
            pstmt.setInt(1, newStoreId);
            pstmt.setInt(2, staffId);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelClubMemberMembership(int customerId, Connection con) {
        try (PreparedStatement pstmt = con.prepareStatement("UPDATE ClubMember SET ActiveStatus = 0 WHERE CustomerID = ?")) {
            pstmt.setInt(1, customerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
