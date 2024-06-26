package edu.northeastern.echolist;

import java.io.Serializable;

public class WishListItem {
    private String id;
    private String name;
    private Boolean purchased;

    public WishListItem() {}

    public WishListItem(String id, String name, Boolean purchased) {
        this.id = id;
        this.name = name;
        this.purchased = purchased;
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

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }
}
