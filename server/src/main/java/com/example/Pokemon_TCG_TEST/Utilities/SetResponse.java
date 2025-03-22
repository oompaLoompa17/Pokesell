package com.example.Pokemon_TCG_TEST.Utilities;

import java.util.List;

public class SetResponse { // acts as the API response for fetching the pokemon card sets

    private List<SetDetails> data;  // List of Set Details from API

    // Getter and Setter
    public List<SetDetails> getData() {
        return data;
    }

    public void setData(List<SetDetails> data) {
        this.data = data;
    }

    // Nested Class for Set Details
    public static class SetDetails {
        private String id;   // the ID of the Pokemon Set (e.g. ex4)
        private String name; // the name of the set (e.g. Team Rocket)

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}