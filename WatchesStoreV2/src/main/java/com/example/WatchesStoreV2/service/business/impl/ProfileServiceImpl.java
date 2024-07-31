package com.example.WatchesStoreV2.service.business.impl;

import com.example.WatchesStoreV2.dto.address.response.AddressResponse;
import com.example.WatchesStoreV2.dto.user.request.ProfileRequest;
import com.example.WatchesStoreV2.dto.user.response.ProfileResponse;
import com.example.WatchesStoreV2.entity.User;
import com.example.WatchesStoreV2.mapper.UserMapper;
import com.example.WatchesStoreV2.service.business.ProfileService;
import com.example.WatchesStoreV2.service.component.AddressService;
import com.example.WatchesStoreV2.service.component.UserComponentService;
import com.mongodb.MongoException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {
    @Autowired
    private UserComponentService userComponentService;

    @Autowired
    private AddressService addressService;

    @Override
    public ProfileResponse getProfileUser(ObjectId userId) {
        try {
            User user = this.userComponentService.findUser(userId);

            List<AddressResponse> addresses = this.addressService.getUserAddresses(userId);

            ProfileResponse profileResponse = UserMapper.convertProfileUser(user, addresses);

            if ((user.getRoles().contains("ROLE_ADMIN")))
                profileResponse.setAdmin(true);

            return profileResponse;
        } catch (MongoException e) {
            e.printStackTrace();
            throw new MongoException("Can't convert profile user");
        }
    }

    @Override
    public User updateProfile(ProfileRequest profileRequest, ObjectId userId) {
        try {
            User user = this.userComponentService.findUser(userId);

            if (profileRequest.getPhone() != null)              user.setPhone(profileRequest.getPhone());
            if (profileRequest.getFirstname() != null)          user.setFirstname(profileRequest.getFirstname());
            if (profileRequest.getLastname() != null)           user.setLastname(profileRequest.getLastname());
            if (profileRequest.getAvatarImg() != null)          user.setAvatarImg(profileRequest.getAvatarImg());
            if (profileRequest.getBackgroundImg() != null)      user.setBackgroundImg(profileRequest.getBackgroundImg());

            this.userComponentService.saveUser(user);
            return user;
        } catch (MongoException e) {
            e.printStackTrace();
            throw new MongoException("Can't update profile user");
        }
    }
}
