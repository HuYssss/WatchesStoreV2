package com.example.WatchesStoreV2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "User")
public class User {
    @Id
    private ObjectId id;

    private String email;

    private String phone;

    private String username;

    private String password;

    private String firstname;

    private String lastname;

    private String avatarImg;

    private String backgroundImg;

    private String roles;

    private String state;

    private String token;
}
