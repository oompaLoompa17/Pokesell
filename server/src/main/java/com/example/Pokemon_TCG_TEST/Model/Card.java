package com.example.Pokemon_TCG_TEST.Model;

import java.util.List;
import java.util.Map;

import com.example.Pokemon_TCG_TEST.Utilities.SetResponse.SetDetails;

public class Card {

    private String id;             // Card ID (e.g. "base1-1")
    private String name;           // Card name (e.g. Charizard)
    private List<String> types;    // List of types (e.g., Fire, Water)
    private String rarity;         // Card rarity (e.g. Rare Holo)
    private SetDetails set;        // Use nested SetDetails from SetResponse
    private Images images;         // Card images
    private Tcgplayer tcgplayer;   // TCGPlayer price information
    private Cardmarket cardmarket; // Cardmarket price information

    // Getters and Setters
    public String getId() {return id;}
    public void setId(String id) { this.id = id;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public List<String> getTypes() {return types;}
    public void setTypes(List<String> types) {this.types = types;}
    public String getRarity() {return rarity;}
    public void setRarity(String rarity) {this.rarity = rarity;}
    public SetDetails getSet() {return set;}
    public void setSet(SetDetails set) {this.set = set;}
    public Images getImages() {return images;}
    public void setImages(Images images) {this.images = images;}
    public Tcgplayer getTcgplayer() {return tcgplayer;}
    public void setTcgplayer(Tcgplayer tcgplayer) {this.tcgplayer = tcgplayer;}
    public Cardmarket getCardmarket() {return cardmarket;}
    public void setCardmarket(Cardmarket cardmarket) {this.cardmarket = cardmarket;}

    // Nested Class for Images
    public static class Images { //the images provided for each pokemon card in the API
        private String small;
        private String large;

        public String getSmall() {return small;}
        public void setSmall(String small) {this.small = small;}
        public String getLarge() {return large;}
        public void setLarge(String large) {this.large = large;}
    }

    public static class Tcgplayer {
        private Map<String, Prices> prices; // Map to handle multiple pricing categories (e.g., holofoil, reverseHolofoil)
        // so the key of this map is (e.g. holofoil, reverse) rarity/type and pricing details is the value
        private String url; //webpage link to the card's page in TCGPlayer

        public Map<String, Prices> getPrices() {
            return prices != null ? prices : Map.of(); // if prices are null, return an empty List (N/A)
        }
        public void setPrices(Map<String, Prices> prices) {this.prices = prices;}
        public String getUrl() {
            return url != null ? url : "N/A"; // if url is missing, show N/A
        }
        public void setUrl(String url) {this.url = url;}
    }

    public static class Prices {
        private Double low; //lowest price for this card type
        private Double mid; //average price
        private Double high; //highest price
        private Double market; //market price

        public Double getLow() {
            return low != null ? low : 0.0; // Default to 0.0 if price is null
        }
        public void setLow(Double low) {this.low = low;}
        public Double getMid() {return mid != null ? mid : 0.0;}
        public void setMid(Double mid) {this.mid = mid;}
        public Double getHigh() {return high != null ? high : 0.0;}
        public void setHigh(Double high) {this.high = high;}
        public Double getMarket() {return market != null ? market : 0.0;}
        public void setMarket(Double market) {this.market = market;}
    }

    // Nested Cardmarket Class
    public static class Cardmarket {
        private Map<String, Double> prices; // similar to TCG but the key in this case is the condition of the card (e.g. Near Mint)
        private String url; //url to the card on CardMarket website

        public Map<String, Double> getPrices() {return prices;}
        public void setPrices(Map<String, Double> prices) {this.prices = prices;}
        public String getUrl() {return url;}
        public void setUrl(String url) {this.url = url;}
    }
}