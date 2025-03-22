package com.example.Pokemon_TCG_TEST.Utilities;

import com.example.Pokemon_TCG_TEST.Model.Card;

public class SingleCardResponse { //to handle API responses when querying for a single card

    private Card data;  // "data" contains a single card object

    public Card getData() {
        return data;
    }

    public void setData(Card data) {
        this.data = data;
    }
}

//e.g. of JSON data from API if querying for a specific card (in this case, querying by id "xy7-54")

//{
//        "data": {
//        "id": "xy7-54",
//        "name": "Charizard",
//        "types": ["Fire"],
//        "rarity": "Rare Holo",
//        "set": {
//        "id": "xy7",
//        "name": "Ancient Origins"
//        },
//        "images": {
//        "small": "https://example.com/small-card-image.jpg",
//        "large": "https://example.com/large-card-image.jpg"
//        },
//        "tcgplayer": {
//        "url": "https://example.com/tcgplayer-card",
//        "prices": {
//        "holofoil": {
//        "low": 5.0,
//        "mid": 10.0,
//        "high": 20.0,
//        "market": 8.5
//        }
//        }
//        },
//        "cardmarket": {
//        "url": "https://example.com/cardmarket-card",
//        "prices": {
//        "averageSellPrice": 7.5,
//        "lowPrice": 4.0,
//        "trendPrice": 8.0
//        }
//        }
//        }
//        }