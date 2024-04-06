package edu.northeastern.echolist;

public class WishListItem {
    private String id;
    private String name;

    public WishListItem() {}

    public WishListItem(String id, String name) {
        this.id = id;
        this.name = name;

    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.name;
    }

    public void setTitle(String name) {
        this.name = name;
    }
}
