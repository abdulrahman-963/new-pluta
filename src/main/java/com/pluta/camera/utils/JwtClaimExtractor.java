package com.pluta.camera.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;

@Slf4j
public class JwtClaimExtractor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getTenantId() {
        try {
            Jwt jwt = getJwtFromContext();
            if (jwt == null) {
                return null;
            }

            Map<String, Object> tenantClaim = jwt.getClaimAsMap("tenant");
            if (tenantClaim == null || tenantClaim.isEmpty()) {
                return null;
            }

            // Get the first tenant key (e.g., "tenant_a")
            String firstTenantKey = tenantClaim.keySet().iterator().next();
            Map<String, Object> tenantData = (Map<String, Object>) tenantClaim.get(firstTenantKey);

            if (tenantData != null && tenantData.containsKey("id")) {
                Object idValue = tenantData.get("id");
                if (idValue instanceof List) {
                    List<String> idList = (List<String>) idValue;
                    return idList.isEmpty() ? null : idList.get(0);
                } else if (idValue instanceof String) {
                    return (String) idValue;
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting tenant ID from JWT", e);
            return null;
        }
    }

    public static String getBranchId() {
        try {
            Jwt jwt = getJwtFromContext();
            if (jwt == null) {
                return null;
            }

            Map<String, Object> branchClaim = jwt.getClaimAsMap("branch");
            if (branchClaim == null || branchClaim.isEmpty()) {
                return null;
            }

            // Get the first branch key (e.g., "branch_a")
            String firstBranchKey = branchClaim.keySet().iterator().next();
            Map<String, Object> branchData = (Map<String, Object>) branchClaim.get(firstBranchKey);

            if (branchData != null && branchData.containsKey("id")) {
                Object idValue = branchData.get("id");
                if (idValue instanceof List) {
                    List<String> idList = (List<String>) idValue;
                    return idList.isEmpty() ? null : idList.get(0);
                } else if (idValue instanceof String) {
                    return (String) idValue;
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting branch ID from JWT", e);
            return null;
        }
    }

    private static Jwt getJwtFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getToken();
        }
        return null;
    }

    public static String getTenantName() {
        try {
            Jwt jwt = getJwtFromContext();
            if (jwt == null) {
                return null;
            }

            Map<String, Object> tenantClaim = jwt.getClaimAsMap("tenant");
            if (tenantClaim == null || tenantClaim.isEmpty()) {
                return null;
            }

            String firstTenantKey = tenantClaim.keySet().iterator().next();
            Map<String, Object> tenantData = (Map<String, Object>) tenantClaim.get(firstTenantKey);

            if (tenantData != null && tenantData.containsKey("name")) {
                Object nameValue = tenantData.get("name");
                if (nameValue instanceof List) {
                    List<String> nameList = (List<String>) nameValue;
                    return nameList.isEmpty() ? null : nameList.get(0);
                } else if (nameValue instanceof String) {
                    return (String) nameValue;
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting tenant name from JWT", e);
            return null;
        }
    }
}