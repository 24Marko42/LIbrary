package org.example;

import lombok.Data;

@Data
public class Book {
    private String name;
    private String author;
    private int publishingYear;
    private String isbn;
    private String publisher;
}