package com.pluta.camera.filters;

import com.pluta.camera.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class TenantFilter implements HandlerInterceptor {

    private static final String BRANCH_CODE_HEADER = "branchId";

    private static final String[] EXCLUDED_PATHS = {
            "/api/swagger-ui",
            "/api/v3/api-docs",
            "/api/swagger-resources",
            "/api/actuator"
    };

    // Paths that skip branch validation but still validate tenant
    private static final String[] BRANCH_EXCLUDED_PATHS = {
            "/api/v1/branches/all"

    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestPath = request.getRequestURI();

        // Skip filter for excluded paths
        if (isExcludedPath(requestPath)) {
            log.debug("Skipping tenant filter for path: {}", requestPath);
            return true;
        }

        String tenantId = null;
        List<String> branchIds = null;

        // Try to get from JWT claims first
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            tenantId = extractTenantIdFromJwt(jwt);
            branchIds = jwt.getClaimAsStringList(BRANCH_CODE_HEADER);
            log.debug("Extracted from JWT - tenantId: {}, branchId: {}", tenantId, branchIds);
        }

        Assert.hasText(tenantId, "tenantId must not be null");

        // Check if this path should skip branch validation
        if (isBranchExcludedPath(requestPath)) {
            log.debug("Skipping branch validation for path: {}", requestPath);
            TenantContext.setTenantInfo(Long.parseLong(tenantId), null);
            return true;
        }

        // Regular branch validation for other paths
        if (Objects.isNull(branchIds) || CollectionUtils.isEmpty(branchIds)) {
            throw new IllegalArgumentException("branchId must not be null");
        }

        String branchId = request.getHeader(BRANCH_CODE_HEADER);

        Assert.hasText(branchId, "branchId must not be null");

        // Validate branchId is in the allowed list from JWT
        if (!branchIds.contains(branchId)) {
            log.warn("Access denied: branchId '{}' not in allowed branches {}", branchId, branchIds);
            throw new AuthorizationDeniedException("Branch not authorized for this user");
        }

        log.debug("Tenant context - tenantId: {}, branchId: {}", tenantId, branchId);
        TenantContext.setTenantInfo(Long.parseLong(tenantId), Long.parseLong(branchId));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }

    private String extractTenantIdFromJwt(Jwt jwt) {
        try {
            var tenantClaim = jwt.getClaimAsMap("tenant");
            if (tenantClaim != null && !tenantClaim.isEmpty()) {
                // Get first tenant key (e.g., "tenant_a")
                String firstKey = tenantClaim.keySet().iterator().next();
                var tenantData = (java.util.Map<String, Object>) tenantClaim.get(firstKey);

                if (tenantData != null && tenantData.containsKey("id")) {
                    Object idValue = tenantData.get("id");
                    if (idValue instanceof java.util.List) {
                        java.util.List<String> idList = (java.util.List<String>) idValue;
                        return idList.isEmpty() ? null : idList.get(0);
                    } else if (idValue instanceof String) {
                        return (String) idValue;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract tenant ID from JWT: {}", e.getMessage());
        }
        return null;
    }


    private boolean isExcludedPath(String requestPath) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (requestPath.startsWith(excludedPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBranchExcludedPath(String requestPath) {
        for (String excludedPath : BRANCH_EXCLUDED_PATHS) {
            if (requestPath.startsWith(excludedPath)) {
                return true;
            }
        }
        return false;
    }
}