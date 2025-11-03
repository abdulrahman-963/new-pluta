package com.pluta.camera.configs;


//@Component
public class AuditorAwareImpl {/*implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        // Extract user ID from authentication principal
        // This is a placeholder - adjust based on your security implementation
        try {
            String userId = authentication.getName();
            return Optional.of(UUID.fromString(userId));
        } catch (IllegalArgumentException e) {
            // If the authentication name is not a UUID, return empty
            return Optional.empty();
        }
    }*/
}