package com.example.rafacartes;

public class Profile {
    private String id;
    private String name;
    private int color; // couleur ARGB en int

    public Profile() {}

    public Profile(String id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
}
