package edu.northeastern.echolist;

public class Gift {
    private String giftId;
    private String name;
    private String description;
    private boolean isTrending;
    private String image;

    public Gift() {}

    public Gift(String giftId, String name, String description, boolean isTrending, String image) {
        this.giftId = giftId;
        this.name = name;
        this.description = description;
        this.isTrending = isTrending;
        this.image = image;
    }

    public String getGiftId() {
        return this.giftId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsTrending() {
        return this.isTrending;
    }

    public String getImage() {
        return this.image;
    }

}
