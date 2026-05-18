package com.social.controller;
import com.social.Config.JwtProvider;
import com.social.Responce.AuthResponce;
import com.social.Service.CustomerUserDetailsService;
import com.social.Service.UserService;
import com.social.Service.EmailService; // Added import
import com.social.models.User;
import com.social.models.VerificationOTP; // Added import
import com.social.repository.UserRepository;
import com.social.repository.VerificationOtpRepository; // Added import
import com.social.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerUserDetailsService customerUserDetails;
    private final JwtProvider jwtProvider;

    // Injected new OTP dependencies
    private final EmailService emailService;
    private final VerificationOtpRepository otpRepository;

    // STEP 1: Request OTP email delivery
    @PostMapping("/signup/request")
    public ResponseEntity<String> requestRegistration(@RequestBody User user) {
        try {
            // Check if user already exists in main user table
            User isExist = userRepository.findByEmail(user.getEmail());
            if (isExist != null) {
                return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
            }

            // Generate a secure 6-digit random OTP string
            String otp = String.format("%06d", new Random().nextInt(999999));

            // Clear any old registration attempts for this email address
            otpRepository.deleteByEmail(user.getEmail());

            // Save user details temporarily in verification table
            VerificationOTP tempUser = new VerificationOTP();
            tempUser.setEmail(user.getEmail());
            tempUser.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt immediately
            tempUser.setFirstName(user.getFirstName());
            tempUser.setLastName(user.getLastName());
            tempUser.setOtp(otp);
            tempUser.setExpiryTime(LocalDateTime.now().plusMinutes(5)); // 5 minute lifespan

            otpRepository.save(tempUser);

            // Trigger email transmission
            emailService.sendOtpEmail(user.getEmail(), otp);

            return new ResponseEntity<>("Verification OTP code successfully sent to your inbox.", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to process request: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // STEP 2: Validate OTP and complete registration
    @PostMapping("/signup/verify")
    public ResponseEntity<?> verifyAndRegister(@RequestParam String email, @RequestParam String otp) {
        try {
            Optional<VerificationOTP> otpData = otpRepository.findByEmailAndOtp(email, otp);

            // Check if OTP matches and hasn't expired yet
            if (otpData.isEmpty() || otpData.get().getExpiryTime().isBefore(LocalDateTime.now())) {
                return new ResponseEntity<>("The OTP code provided is invalid or has expired.", HttpStatus.BAD_REQUEST);
            }

            VerificationOTP data = otpData.get();

            // Transfer data to permanent main User table
            User newUser = new User();
            newUser.setEmail(data.getEmail());
            newUser.setPassword(data.getPassword()); // Already encrypted
            newUser.setFirstName(data.getFirstName());
            newUser.setLastName(data.getLastName());

            User savedUser = userRepository.save(newUser);
            otpRepository.delete(data); // Clear temporary workspace record

            // Authenticate and issue clean JWT for immediate auto-login
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    savedUser.getEmail(), savedUser.getPassword());

            String token = jwtProvider.generateToken(authentication);

            return new ResponseEntity<>(new AuthResponce(token, "Register success"), HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {

        try {

            Authentication authentication =
                    authenticate(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            String token = jwtProvider.generateToken(authentication);

            return ResponseEntity.ok(
                    java.util.Map.of(
                            "jwt", token,
                            "message", "Login Success"
                    )
            );

        } catch (BadCredentialsException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            java.util.Map.of(
                                    "error", "Invalid email or password"
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            java.util.Map.of(
                                    "error", e.getMessage()
                            )
                    );
        }
    }

    private Authentication authenticate(String email, String password) {

        UserDetails userDetails =
                customerUserDetails.loadUserByUsername(email);

        if (userDetails == null) {
            throw new BadCredentialsException("User not found");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid Password");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}