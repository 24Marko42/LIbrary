package org.example;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
class Visitors {
    private String name;
    private String surname;
    private String phone;
    @SerializedName("favoriteBooks")
    private List<Book> favoriteBooks;
    private boolean subscribed;
}