package edu.northeastern.echolist;

public class Event {
    private String eventId;
    private String title;
    private String location;
    private String date;
    private String userId;
    private String category;
    private String visibility;


    public Event() {}

    public Event(String eventId, String userId, String title, String location, String date, String category,String visibility) {
        this.eventId = eventId;
        this.userId = userId;
        this.title = title;
        this.location = location;
        this.date = date;
        this.category = category;
        this.visibility = visibility;
    }


    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
