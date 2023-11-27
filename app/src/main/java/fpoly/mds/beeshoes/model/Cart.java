package fpoly.mds.beeshoes.model;

import java.util.HashMap;

public class Cart {
    private String id;
    private String img;
    private String name;
    private int price;
    private String color;
    private int size;
    private int quantity;

    public Cart() {
    }

    public Cart(String id, String img, String name, int price, String color, int size, int quantity) {
        this.id = id;
        this.img = img;
        this.name = name;
        this.price = price;
        this.color = color;
        this.size = size;
        this.quantity = quantity;
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public HashMap<String, Object> convertHashMap() {
        HashMap<String, Object> cart = new HashMap<>();
        cart.put("id", id);
        cart.put("img", img);
        cart.put("name", name);
        cart.put("price", price);
        cart.put("color", color);
        cart.put("size", size);
        cart.put("quantity", quantity);
        return cart;
    }
}
