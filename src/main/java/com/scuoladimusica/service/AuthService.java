package com.scuoladimusica.service;

import com.scuoladimusica.model.dto.request.LoginRequest;
import com.scuoladimusica.model.dto.request.SignupRequest;
import com.scuoladimusica.model.dto.response.JwtResponse;
import com.scuoladimusica.model.dto.response.MessageResponse;
import com.scuoladimusica.model.entity.ERole;
import com.scuoladimusica.model.entity.Role;
import com.scuoladimusica.model.entity.User;
import com.scuoladimusica.repository.RoleRepository;
import com.scuoladimusica.repository.UserRepository;
import com.scuoladimusica.security.jwt.JwtUtils;
import com.scuoladimusica.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), roles);
    }

    public MessageResponse registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.username())) {
            return new MessageResponse("Errore: Username già in uso!");
        }

        if (userRepository.existsByEmail(signupRequest.email())) {
            return new MessageResponse("Errore: Email già in uso!");
        }

        User user = new User(signupRequest.username(), signupRequest.email(),
                encoder.encode(signupRequest.password()));

        Set<String> strRoles = signupRequest.role();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                    .orElseThrow(() -> new RuntimeException("Errore: Ruolo non trovato."));
            roles.add(studentRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin" -> {
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Errore: Ruolo non trovato."));
                        roles.add(adminRole);
                    }
                    case "teacher" -> {
                        Role teacherRole = roleRepository.findByName(ERole.ROLE_TEACHER)
                                .orElseThrow(() -> new RuntimeException("Errore: Ruolo non trovato."));
                        roles.add(teacherRole);
                    }
                    default -> {
                        Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                                .orElseThrow(() -> new RuntimeException("Errore: Ruolo non trovato."));
                        roles.add(studentRole);
                    }
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return new MessageResponse("Utente registrato con successo!");
    }
}
