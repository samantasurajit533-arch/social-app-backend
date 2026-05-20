package com.social.Service;

import com.social.models.User;
import com.social.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    // Final variable ensures thread safety and mandatory initialization
    private final UserRepository userRepository;

    // Constructor injection prevents NullPointerExceptions during initialization
    public CustomerUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Looks up user via database
        User user = userRepository.findByEmail(username);

        // Validates database response
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        // Prepares roles/permissions list
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Returns Spring Security compatible user object
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
