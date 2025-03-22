package com.example.Pokemon_TCG_TEST.Utilities;

import java.util.List;

public class RarityResponse { // acts as the response from the API for a list of card rarities

    private List<String> data;  // list of card rarities returned by API

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}

//example of the rarities data

//{
//        "data": [
//        "Common",
//        "Uncommon",
//        "Rare",
//        "Rare Holo",
//        "Ultra Rare",
//        "Secret Rare",
//        "Promo"
//        ]
//        }