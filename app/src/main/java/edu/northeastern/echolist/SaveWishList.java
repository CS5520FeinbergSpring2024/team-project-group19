package edu.northeastern.echolist;

import java.io.Serializable;
import java.util.List;

public class SaveWishList implements Serializable {
    private List<WishListItem> wishList;
    public List<WishListItem> getWishList() {
        return wishList;
    }
    public void setWishList(List<WishListItem> wishList) {
        this.wishList = wishList;
    }
}
