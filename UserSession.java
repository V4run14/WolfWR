package WolfWR;

public class UserSession {
    private final String name;
    private final String jobTitle;
    private final int storeId;

    public UserSession(String name, String jobTitle, int storeId) {
        this.name = name;
        this.jobTitle = jobTitle;
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public int getStoreId() {
        return storeId;
    }
}
