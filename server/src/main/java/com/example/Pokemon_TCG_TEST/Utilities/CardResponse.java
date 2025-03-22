package com.example.Pokemon_TCG_TEST.Utilities;

import com.example.Pokemon_TCG_TEST.Model.Card;

import java.util.List;

public class CardResponse { //acts as response from the API when getting a list of cards

    private List<Card> data; // Cards from the API response

    private int totalCount; //total number of cards that match query

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    // Getters and Setters
    public List<Card> getData() { return data; }
    public void setData(List<Card> data) { this.data = data; }
}


//example of the JSON response we will be parsing

//{
//        "data": [
//        {
//        "id": "xy7-54",
//        "name": "Charizard",
//        "types": ["Fire"],
//        "rarity": "Rare Holo",
//        "set": { "id": "xy7", "name": "Ancient Origins" }
//        },
//        {
//        "id": "sm3-20",
//        "name": "Pikachu",
//        "types": ["Electric"],
//        "rarity": "Common",
//        "set": { "id": "sm3", "name": "Burning Shadows" }
//        }
//        ],
//        "totalCount": 100
//        }