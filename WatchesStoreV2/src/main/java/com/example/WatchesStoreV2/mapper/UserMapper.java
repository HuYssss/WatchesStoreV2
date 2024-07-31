package com.example.WatchesStoreV2.mapper;

import com.example.WatchesStoreV2.dto.address.response.AddressResponse;
import com.example.WatchesStoreV2.dto.auth.request.RegisterDto;
import com.example.WatchesStoreV2.dto.user.response.ProfileResponse;
import com.example.WatchesStoreV2.entity.User;
import org.bson.types.ObjectId;

import java.util.List;

public class UserMapper {
    public static User registerUser(RegisterDto registerDto) {
        return new User(
            new ObjectId(),
                registerDto.getEmail(),
                registerDto.getPhone(),
                registerDto.getUsername(),
                registerDto.getPassword(),
                "unknow",
                "unknow",
                "unknow",
                "unknow",
                "ROLE_USER",
                "active",
                ""
        );
    }

    public static ProfileResponse convertProfileUser(User user, List<AddressResponse> addresses) {
        return new ProfileResponse(
                user.getId().toHexString(),
                user.getEmail(),
                user.getPhone(),
                user.getUsername(),
                user.getFirstname(),
                user.getLastname(),
                user.getAvatarImg(),
                user.getBackgroundImg(),
                addresses,
                user.getState(),
                false
        );
    }
}
