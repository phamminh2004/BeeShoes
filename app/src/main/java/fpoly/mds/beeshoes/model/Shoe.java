package fpoly.mds.beeshoes.model;

import java.util.HashMap;

public class Shoe {
    private String id;
    private String img;
    private String name;
    private String shoeType;
    private int price;
    private String color;
    private int size;

    public Shoe() {
    }

    public Shoe(String id, String img, String name, String shoeType, int price, String color, int size) {
        this.id = id;
        this.img = img;
        this.name = name;
        this.shoeType = shoeType;
        this.price = price;
        this.color = color;
        this.size = size;
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

    public String getShoeType() {
        return shoeType;
    }

    public void setShoeType(String shoeType) {
        this.shoeType = shoeType;
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

    public HashMap<String, Object> convertHashMap() {
        HashMap<String, Object> shoe = new HashMap<>();
        shoe.put("id", id);
        shoe.put("img", img);
        shoe.put("name", name);
        shoe.put("shoeType", shoeType);
        shoe.put("price", price);
        shoe.put("color", color);
        shoe.put("size", size);
        return shoe;
    }
}
