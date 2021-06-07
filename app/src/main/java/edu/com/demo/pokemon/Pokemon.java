package edu.com.demo.pokemon;

public class Pokemon {
    private final String name;
    private final String url;

    Pokemon(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
