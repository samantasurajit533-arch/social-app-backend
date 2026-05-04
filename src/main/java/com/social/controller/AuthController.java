package com.social.controller;

import com.social.Config.JwtProvider; // Ensure this is imported
import com.social.Responce.AuthResponce;
import com.social.Service.CustomerUserDetailsService;
import com.social.Service.UserService;
import com.social.models.User;
import com.social.repository.UserRepository;
import com.social.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerUserDetailsService customerUserDetails;

    // 1. ADD THIS LINE to inject the JwtProvider bean
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User isExist = userRepository.findByEmail(user.getEmail());
            if (isExist != null) {
                return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    savedUser.getEmail(), savedUser.getPassword());

            // 2. FIX: Use injected 'jwtProvider' instance instead of class name
            String token = jwtProvider.generateToken(authentication);

            return new ResponseEntity<>(new AuthResponce(token, "Register success"), HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/signin")
    public AuthResponce signin(@RequestBody LoginRequest loginRequest){
        Authentication authentication = authenticate(loginRequest.getEmail(), loginRequest.getPassword());

        // 3. FIX: Use injected 'jwtProvider' instance instead of class name
        String token = jwtProvider.generateToken(authentication);

        return new AuthResponce(token, "login success");
    }

    private Authentication authenticate(String email, String password){
        UserDetails userDetails = customerUserDetails.loadUserByUsername(email);
        if(userDetails == null){
            throw new BadCredentialsException("invalid username");
        }
        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new BadCredentialsException("password not match");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
