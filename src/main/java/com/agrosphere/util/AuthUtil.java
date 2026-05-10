package com.agrosphere.util;
import com.agrosphere.entity.User;
import com.agrosphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class AuthUtil {
    private final UserRepository userRepository;
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
    }
    public Long getCurrentTenantId() {
        User u = getCurrentUser();
        if (u.getTenant()==null) throw new RuntimeException("No tenant for this user");
        return u.getTenant().getId();
    }
    public Long getCurrentUserId() { return getCurrentUser().getId(); }
}
