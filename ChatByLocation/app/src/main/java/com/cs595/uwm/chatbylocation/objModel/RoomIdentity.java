package com.cs595.uwm.chatbylocation.objModel;


public class RoomIdentity {

    private String name;
    private String longg;
    private String lat;
    private String ownerID;
    private int rad;
    private String password;

    public RoomIdentity() {

    }

    public RoomIdentity(String name, String longg, String lat, String ownerID, int rad, String password) {
        this.name = name;
        this.longg = longg;
        this.lat = lat;
        this.ownerID = ownerID;
        this.rad = rad;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLongg() {
        return longg;
    }

    public void setLongg(String longg) {
        this.longg = longg;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public int getRad() {
        return rad;
    }

    public void setRad(int rad) {
        this.rad = rad;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }
}
