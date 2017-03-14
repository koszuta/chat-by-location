package com.cs595.uwm.chatdemo2;

import android.location.Location;

/**
 * Created by Nathan on 3/13/17.
 */

class ChatRoom {

    private String name;
    private Location location;
    private int radius;

    ChatRoom(String name, Location location, int radius) {
        this.name = name;
        this.location = location;
        this.radius = radius;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    Location getLocation() {
        return location;
    }

    void setLocation(Location location) {
        this.location = location;
    }

    int getRadius() {
        return radius;
    }

    void setRadius(int radius) {
        this.radius = radius;
    }
}
