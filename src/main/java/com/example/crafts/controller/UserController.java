package com.example.crafts.controller;

import com.example.crafts.dto.LoginRequest;
import com.example.crafts.dto.RegisterRequest;
import com.example.crafts.dto.UpdateProfileRequest;
import com.example.crafts.entity.User;
import com.example.crafts.security.JwtService;
import com.example.crafts.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // 🔥 Do NOT encode here

        String result = userService.registerUser(user);

        System.out.println("✅ Registered User: " + user.getEmail());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail());

        if (user == null) {
            System.out.println("❌ User not found: " + request.getEmail());
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        boolean match = passwordEncoder.matches(request.getPassword(), user.getPassword());

        System.out.println("🔐 Login attempt: " + request.getEmail());
        System.out.println("🔒 Raw password: " + request.getPassword());
        System.out.println("🔒 Encoded password in DB: " + user.getPassword());
        System.out.println("🔍 Match result: " + match);

        if (match) {
            String token = jwtService.generateToken(user.getEmail(), "USER");
            return ResponseEntity.ok(token);
        }

        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(HttpServletRequest request) {
        String email = jwtService.extractEmailFromRequest(request);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(HttpServletRequest request, @RequestBody UpdateProfileRequest updateRequest) {
        String email = jwtService.extractEmailFromRequest(request);
        User updatedUser = new User();
        updatedUser.setName(updateRequest.getName());
        updatedUser.setPassword(updateRequest.getPassword()); // 🔥 raw password passed
        userService.updateProfile(email, updatedUser);
        return ResponseEntity.ok("Profile updated");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = jwtService.extractToken(request);
        jwtService.invalidateToken(token);
        return ResponseEntity.ok("Logged out");
    }
}
