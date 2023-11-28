package fpoly.mds.beeshoes.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Bill {
    private String id;
    private int price;
    private String nameCustomer;
    private String phone;
    private String address;
    private Date date;
    private int status;

    public Bill() {
    }

    public Bill(String id, int price, String nameCustomer, String phone, String address, Date date, int status) {
        this.id = id;
        this.price = price;
        this.nameCustomer = nameCustomer;
        this.phone = phone;
        this.address = address;
        this.date = date;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getNameCustomer() {
        return nameCustomer;
    }

    public void setNameCustomer(String nameCustomer) {
        this.nameCustomer = nameCustomer;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
        bill.put("price", price);
        bill.put("nameCustomer", nameCustomer);
        bill.put("phone", phone);
        bill.put("address", address);
        bill.put("date", sdf.format(date));
        bill.put("status", status);
        return bill;
    }

}
