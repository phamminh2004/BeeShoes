package fpoly.mds.beeshoes.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Bill {
    private String id;
    private String userId;
    private String nameCustomer;
    private String address;
    private String phone;
    private int price;
    private Date date;
    private int status;

    public Bill() {
    }

    public Bill(String id, String userId, String nameCustomer, String address, String phone, int price, Date date, int status) {
        this.id = id;
        this.userId = userId;
        this.nameCustomer = nameCustomer;
        this.address = address;
        this.phone = phone;
        this.price = price;
        this.date = date;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNameCustomer() {
        return nameCustomer;
    }

    public void setNameCustomer(String nameCustomer) {
        this.nameCustomer = nameCustomer;
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public HashMap<String, Object> convertHashMap() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        HashMap<String, Object> bill = new HashMap<>();
        bill.put("id", id);
        bill.put("userId", userId);
        bill.put("nameCustomer", nameCustomer);
        bill.put("address", address);
        bill.put("phone", phone);
        bill.put("price", price);
        bill.put("date", sdf.format(date));
        bill.put("status", status);
        return bill;
    }

}
