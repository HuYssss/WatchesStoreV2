package com.example.WatchesStoreV2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "Product")
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    private ObjectId id;

    private String productName;

    private List<String> img;

    private double price;

    private String brand;

    private String origin;

    private String thickness;

    private String size;

    private String wireMaterial;

    private String shellMaterial;

    private String style;

    private String feature;

    private String shape;

    private String condition;

    private String weight;

    private String genderUser;

    private String description;

    private String color;

    private ObjectId category;

    private int amount;

    private double discount;

    private int waterproof;

    private String state;
}
