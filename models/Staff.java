package WolfWR.models;

import java.util.Date;

public class Staff {

    private int storeID;
    private String name;
    private int age;
    private String address;
    private String phoneNumber;
    private String jobTitle;
    private String email;
    private String password;
    private Date timeOfEmployment;



    public Staff(int storeID, String name, int age, String address, String phoneNumber, String jobTitle, String email,
                 String password, Date timeOfEmployment) {
        super();
        setStoreID(storeID);
        setName(name);
        setAge(age);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setJobTitle(jobTitle);
        setEmail(email);
        setPassword(password);
        setTimeOfEmployment(timeOfEmployment);

    }

    public int getStoreID() {
        return storeID;
    }
    private void setStoreID(int storeID) {
        this.storeID = storeID;
    }
    public String getName() {
        return name;
    }
    private void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    private void setAge(int age) {
        this.age = age;
    }
    public String getAddress() {
        return address;
    }
    private void setAddress(String address) {
        this.address = address;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    private void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getJobTitle() {
        return jobTitle;
    }
    private void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    public String getEmail() {
        return email;
    }
    private void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    private void setPassword(String password) {
        this.password = password;
    }
    public Date getTimeOfEmployment() {
        return timeOfEmployment;
    }
    private void setTimeOfEmployment(Date timeOfEmployment) {
        this.timeOfEmployment = timeOfEmployment;
    }

}
