package com.cs595.uwm.chatdemo2;

import android.location.Location;

/**
 * Created by Nathan on 3/13/17.
 */

public class ChatRoom {

    public String name;
    public Location location;
    public int radius;
    public String password = null;

    public ChatRoom() {

    }

    public ChatRoom(String name, Location location, int radius, String password) {
        this.name = name;
        this.location = location;
        this.radius = radius;
        this.password = password;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }

    public int getRadius() {
        return radius;
    }
    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
