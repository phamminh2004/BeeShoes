package fpoly.mds.beeshoes.model;

import java.util.HashMap;

public class Customer {
    private String id;
    private String name;
    private String address;
    private String phone;

    public Customer() {
    }

    public Customer(String id, String name,  String phone,String address) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public HashMap<String, Object> converHashMap() {
        HashMap<String, Object> customer = new HashMap<>();
        customer.put("id", id);
        customer.put("name", name);
        customer.put("phone", phone);
        customer.put("address", address);
        return customer;
    }
}
