package com.example.Pokemon_TCG_TEST.Utilities;

import java.util.List;

public class TypeResponse { // straightforward, type-ings in pokemon such as fire, grass etc

    private List<String> data;  // This matches the API's response

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}