package com.agrosphere.service.impl;
import com.agrosphere.dto.request.LoginRequest;
import com.agrosphere.dto.request.RegisterRequest;
import com.agrosphere.dto.response.AuthResponse;
import com.agrosphere.entity.Tenant;
import com.agrosphere.entity.User;
import com.agrosphere.enums.Role;
import com.agrosphere.repository.TenantRepository;
import com.agrosphere.repository.UserRepository;
import com.agrosphere.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public AuthResponse registerFarmer(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) throw new RuntimeException("Email already registered");
        Tenant tenant = tenantRepository.save(Tenant.builder()
            .name(req.getName()+"'s Farm").email(req.getEmail())
            .phoneNumber(req.getPhoneNumber()).district(req.getDistrict()).state(req.getState()).build());
        User user = userRepository.save(User.builder()
            .name(req.getName()).email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .phoneNumber(req.getPhoneNumber()).role(Role.FARMER).tenant(tenant).build());
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse registerConsumer(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) throw new RuntimeException("Email already registered");
        User user = userRepository.save(User.builder()
            .name(req.getName()).email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .phoneNumber(req.getPhoneNumber()).role(Role.CONSUMER).build());
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        if (!user.getIsActive()) throw new RuntimeException("Account is deactivated. Contact admin.");
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
        Long tenantId = user.getTenant() != null ? user.getTenant().getId() : null;
        String token = jwtUtil.generateToken(ud, tenantId, user.getRole().name());
        return AuthResponse.builder()
            .token(token).userId(user.getId()).name(user.getName())
            .email(user.getEmail()).role(user.getRole().name())
            .tenantId(tenantId).profileImage(user.getProfileImage()).build();
    }
}
