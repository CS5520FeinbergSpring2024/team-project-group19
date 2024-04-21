package edu.northeastern.echolist;

/*
New class to identify gift favorite status, independent of Gift
(which does not track favorite, as they are tracked at the user level)
and independent of activities and views
 */
public class GiftItem extends Gift {
    private boolean isFavorite;

    public GiftItem() {

    }

    public GiftItem(Gift gift, boolean isFavorite) {
        super(gift.getGiftId(), gift.getName(), gift.getDescription(), gift.getIsTrending(), gift.getImage());
        this.isFavorite = isFavorite;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}