package com.example.WatchesStoreV2.controller;

import com.example.WatchesStoreV2.base.ControllerBase;
import com.example.WatchesStoreV2.dto.user.request.ProfileRequest;
import com.example.WatchesStoreV2.service.business.ProfileService;
import com.mongodb.MongoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/profile")
public class ProfileController extends ControllerBase {
    @Autowired
    private ProfileService profileService;

    @GetMapping()
    public ResponseEntity<?> getProfile(Principal principal) {
        try {
            return response(this.profileService.getProfileUser(findIdByUsername(principal.getName()))
                    , HttpStatus.OK);
        } catch (MongoException e) {
            return response(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileRequest profileRequest, Principal principal) {
        if (ObjectUtils.isEmpty(profileRequest))
            return response(null, HttpStatus.NO_CONTENT);

        try {
            return response(this.profileService.updateProfile(profileRequest, findIdByUsername(principal.getName()))
                    , HttpStatus.OK);
        } catch (MongoException e) {
            return response(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
