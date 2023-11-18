package fpoly.mds.beeshoes.model;

import java.util.HashMap;

public class User {
    private String email;
    private String role;

    public User() {
    }

    public User(String email, String role) {
        this.email = email;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public HashMap<String, Object> convertHashMap() {
        HashMap<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", role);
        return user;
    }
}
