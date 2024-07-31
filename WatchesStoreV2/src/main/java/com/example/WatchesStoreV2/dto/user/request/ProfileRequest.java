package com.example.WatchesStoreV2.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {
    private String phone;

    private String firstname;

    private String lastname;

    private String avatarImg;

    private String backgroundImg;
}
