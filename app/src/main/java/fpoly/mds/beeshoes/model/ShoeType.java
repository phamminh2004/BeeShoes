package fpoly.mds.beeshoes.model;

import java.util.HashMap;

public class ShoeType {
    private String id;
    private String img;
    private String name;

    public ShoeType() {
    }

    public ShoeType(String id, String img, String name) {
        this.id = id;
        this.img = img;
        this.name = name;
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

    public HashMap<String, Object> convertHashMap() {
        HashMap<String, Object> shoeType = new HashMap<>();
        shoeType.put("id", id);
        shoeType.put("img", img);
        shoeType.put("name", name);
        return shoeType;
    }
}
