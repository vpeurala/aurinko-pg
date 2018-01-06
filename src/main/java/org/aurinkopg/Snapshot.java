package org.aurinkopg;

public class Snapshot {
    private final String name;

    public Snapshot(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
