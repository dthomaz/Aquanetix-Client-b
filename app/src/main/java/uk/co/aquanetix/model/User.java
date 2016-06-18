package uk.co.aquanetix.model;

public class User {
    
    private final int userId;
    private final String username;
    private final String pincode;
    private final boolean hideFromClient;
    private final boolean bagsFlag;
    private final String role;
    private final String unit_system;
    
    public User(int userId, String username, String pincode, boolean hideFromClient, boolean bagsFlag, String role, String unit_system) {
        this.userId = userId;
        this.username = username;
        this.pincode = pincode;
        this.hideFromClient = hideFromClient;
        this.bagsFlag = bagsFlag;
        this.role = role;
        this.unit_system = unit_system;
    }
    
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPincode() { return pincode; }
    public boolean isHideFromClient() { return hideFromClient; }
    public boolean isBagsFlag() { return bagsFlag; }
    public String getRole() { return role; }
    public String getUnitSystem() {return unit_system;}
}
