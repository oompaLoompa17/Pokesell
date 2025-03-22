package com.example.Pokemon_TCG_TEST.Service;

import com.example.Pokemon_TCG_TEST.Model.*;
import com.example.Pokemon_TCG_TEST.Utilities.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class CardService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${pokemon.api.url}")
    private String apiUrl;

    @Value("${pokemon.api.key}")
    private String apiKey;

    // Helper method to create headers with the API key
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey); //adding API key to the headers
        return headers;
    }

    // Search for cards by name
    public List<Card> searchCardsByName(String name) {

        HttpHeaders headers = createHeaders();

        HttpEntity<String> entity = new HttpEntity<>(headers); //represents HTTP request OR response entity
        //containes Headers(key-value)

        String url = apiUrl + "/cards?q=name:" + name; //build the search URL

        // making request to API and then parsing the response into CardResponse which we created
        CardResponse response = restTemplate.exchange(
                url, // url of the API endpoint we are calling
                HttpMethod.GET, //specifying this is a GET request
                entity, //
                CardResponse.class) //specify class type of the API response, Spring then deserializes it from JSON into CardResponse
                .getBody();

        return response != null ? response.getData() : List.of();
        //if response is not null, return the list of cards, else return empty list
        //basically if loop but cleaner code
    }

    // Fetch available sets
    public List<SetResponse.SetDetails> getAvailableSets() {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = apiUrl + "/sets"; //endpoint for sets

        // make request to API and parse the JSON response into TypeResponse
        SetResponse response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                SetResponse.class)
                .getBody();

        return response != null ? response.getData() : List.of();
    }

    // Fetch available types
    public List<String> getAvailableTypes() {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = apiUrl + "/types";

        // Correct the response deserialization
        TypeResponse response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                TypeResponse.class)
                .getBody();

        return response != null ? response.getData() : List.of();
    }

    // Fetch available rarities
    public List<String> getAvailableRarities() {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = apiUrl + "/rarities";

        // Correct the response deserialization
        RarityResponse response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                RarityResponse.class)
                .getBody();

        return response != null ? response.getData() : List.of();
    }

    //searching for cards using multiple filters
    public List<Card> searchCards(String name, String set, String type, String rarity, int page, int pageSize) {

        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        //build the query string with the provided filters
        StringBuilder query = new StringBuilder(apiUrl + "/cards?q=");

        if (name != null && !name.isEmpty())
            query.append("name:\"").append(name).append("\" ");
        // example if name = Charizard, result will be     name:"Charizard" as a query in url

        if (set != null && !set.isEmpty())
            query.append("set.id:").append(set).append(" ");
        //example --> set.id:xyz

        if (type != null && !type.isEmpty())
            query.append("types:").append(type).append(" ");
        //example --> types:Fire

        if (rarity != null && !rarity.isEmpty())
            query.append("rarity:\"").append(rarity).append("\" ");
        //example --> rarity:"Rare Holo"

        //if combine all examples above, query will look like
        // name:"Charizard" set.id:xy7 types:Fire rarity:"Rare Holo"

        // pagination, so that it wont display everything on one page and take long to display
        query.append("&page=").append(page).append("&pageSize=").append(pageSize);

        String url = query.toString().trim();//final url for our API request

        CardResponse response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CardResponse.class)
                .getBody();

        return response != null ? response.getData() : List.of();
    }

    public Card getCardById(String id) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = apiUrl + "/cards/" + id;

        try {
            SingleCardResponse response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    SingleCardResponse.class)
                    .getBody();

            // Return the card if found or null
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            System.err.println("Error fetching card details: " + e.getMessage());
        }

        throw new RuntimeException("Card not found: " + id);
    }

    public List<Card> getCardsByIds(List<String> cardIds) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Card> cards = new ArrayList<>();

        //loop through each card ID and get its details
        for (String id : cardIds) {
            String url = apiUrl + "/cards/" + id;

            try {
                // API request for each card's id
                SingleCardResponse response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, SingleCardResponse.class).getBody();

                if (response != null && response.getData() != null) {
                    cards.add(response.getData()); // Add the card to the list
                }
            } catch (Exception e) {
                System.err.println("Error fetching card with ID " + id + ": " + e.getMessage());
                // Optionally, continue to the next card ID if one fails
            }
        }

        return cards; // Return the list of cards
    }

    // get total number of cards depending on filters used when searching
    public int getTotalCardCount(String name, String set, String type, String rarity) {

        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        //similar to the one above of searching with filters
        StringBuilder query = new StringBuilder(apiUrl + "/cards?q=");
        if (name != null && !name.isEmpty()) query.append("name:\"").append(name).append("\" ");
        if (set != null && !set.isEmpty()) query.append("set.id:").append(set).append(" ");
        if (type != null && !type.isEmpty()) query.append("types:").append(type).append(" ");
        if (rarity != null && !rarity.isEmpty()) query.append("rarity:\"").append(rarity).append("\" ");

        // Fetch the total count
        String url = query.toString().trim() + "&pageSize=1"; // Request 1 card to get total count
        CardResponse response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CardResponse.class)
                .getBody();

        return response != null ? response.getTotalCount() : 0;
        //return total count of cards
    }

    public List<Card> getAllCards() {
        String url = apiUrl + "/cards?pageSize=1000"; // Use a large page size to fetch all cards
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        CardResponse response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CardResponse.class)
                .getBody();

        return response != null ? response.getData() : List.of();
    }
}