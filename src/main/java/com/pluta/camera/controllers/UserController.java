package com.pluta.camera.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

//@RestController
@RequestMapping("/api")
public class UserController {


    @GetMapping("/user-info")
    @PreAuthorize("hasRole('dashboard-view')")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new HashMap<>();

        // Get standard claims
        userInfo.put("subject", jwt.getSubject());
        userInfo.put("email", jwt.getClaim("email"));
        userInfo.put("name", jwt.getClaim("name"));
        userInfo.put("preferred_username", jwt.getClaim("preferred_username"));

        // Get custom claims
        userInfo.put("custom_claim", jwt.getClaim("custom_claim"));

        // Get all claims
        userInfo.put("all_claims", jwt.getClaims());

        return userInfo;
    }
}