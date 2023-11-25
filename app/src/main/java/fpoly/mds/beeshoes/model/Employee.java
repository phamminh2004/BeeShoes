package fpoly.mds.beeshoes.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Employee {
    private String id;
    private String img;
    private String name;
    private Date birthday;
    private String sex;
    private String phone;
    private String address;
    private String role;

    public Employee() {
    }

    public Employee(String id, String img, String name, Date birthday, String sex, String phone, String address, String role) {
        this.id = id;
        this.img = img;
        this.name = name;
        this.birthday = birthday;
        this.sex = sex;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public HashMap<String, Object> convertHashMap() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        HashMap<String, Object> employee = new HashMap<>();
        employee.put("id", id);
        employee.put("img", img);
        employee.put("name", name);
        employee.put("birthday", sdf.format(birthday));
        employee.put("sex", sex);
        employee.put("phone", phone);
        employee.put("address", address);
        employee.put("role", role);
        return employee;
    }
}
