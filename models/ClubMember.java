package WolfWR.models;

import java.util.Date;

public class ClubMember {

    private String membershipLevel;
    private String address;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private Date signUpDate;
    private Date dueDate;
    private Integer activityStatus;
    private Date lastPaid;
    private Integer storeID;
    private Integer signupStaffID;

    public ClubMember(String membershipLevel, String address, String email, String phoneNumber, String firstName,
                      String lastName, Date signUpDate, Date dueDate, Integer activityStatus, Date lastPaid, Integer storeID, Integer signupStaffID) {
        super();
        setMembershipLevel(membershipLevel);
        setAddress(address);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setFirstName(firstName);
        setLastName(lastName);
        setSignUpDate(signUpDate);
        setDueDate(dueDate);
        setActivityStatus(activityStatus);
        setLastPaid(lastPaid);
        setStoreID(storeID);
        setSignupStaffID(signupStaffID);
    }

    public String getMembershipLevel() {
        return membershipLevel;
    }

    private void setMembershipLevel(String membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    public String getAddress() {
        return address;
    }

    private void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    private void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    private void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    private void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    private void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getSignUpDate() {
        return signUpDate;
    }

    private void setSignUpDate(Date signUpDate) {
        this.signUpDate = signUpDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    private void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getActivityStatus() {
        return activityStatus;
    }

    private void setActivityStatus(Integer activityStatus) {
        this.activityStatus = activityStatus;
    }

    public Date getLastPaid() {
        return lastPaid;
    }

    private void setLastPaid(Date lastPaid) {
        this.lastPaid = lastPaid;
    }

    public Integer getStoreID() {
        return storeID;
    }

    public void setStoreID(Integer storeID) {
        this.storeID = storeID;
    }

    public Integer getSignupStaffID() {
        return signupStaffID;
    }

    public void setSignupStaffID(Integer signupStaffID) {
        this.signupStaffID = signupStaffID;
    }

}
