package edu.northeastern.echolist;

import java.util.List;

public class Event {
    private String eventId;
    private String title;
    private String location;
    private String date;
    private String userId;
    private String category;
    private String visibility;
    private List<String> friends;

    public Event() {}

    public Event(String eventId, String userId, String title, String location, String date,
                 String category,String visibility, List<String> friends) {
        this.eventId = eventId;
        this.userId = userId;
        this.title = title;
        this.location = location;
        this.date = date;
        this.category = category;
        this.visibility = visibility;
        this.friends = friends;
    }

    public String getEventId() {
        return this.eventId;
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

    public String getDate() {
        return this.date;
    }

    public String getCategory() {
        return this.category;
    }

    public String getVisibility() {
        return visibility;
    }
}
