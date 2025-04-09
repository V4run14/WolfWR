package WolfWR.models;

public class Supplier {

    private String name;
    private String email;
    private String location;
    private String phoneNumber;

    public Supplier(String name, String email, String location, String phoneNumber) {
        super();
        setName(name);
        setEmail(email);
        setLocation(location);
        setPhoneNumber(phoneNumber);
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    private void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    private void setLocation(String location) {
        this.location = location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    private void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
