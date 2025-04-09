package WolfWR.models;

public class Store {

    String branch;
    String phoneNumber;
    String address;


    public Store(String branch, String phoneNumber, String address) {
        super();
        setBranch(branch);
        setPhoneNumber(phoneNumber);
        setAddress(address);
    }

    public String getBranch() {
        return branch;
    }

    private void setBranch(String branch) {
        this.branch = branch;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    private void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    private void setAddress(String address) {
        this.address = address;
    }


}
