package com.social.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/admin")
public class DatabaseInitController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/init-tables")
    public String initTables() {
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS user_followers (" +
                            "user_id INT NOT NULL, " +
                            "followers INT, " +
                            "FOREIGN KEY (user_id) REFERENCES users(id))");

            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS user_followings (" +
                            "user_id INT NOT NULL, " +
                            "followings INT, " +
                            "FOREIGN KEY (user_id) REFERENCES users(id))");

            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS users_saved_post (" +
                            "user_id INT NOT NULL, " +
                            "saved_post_id INT NOT NULL, " +
                            "FOREIGN KEY (user_id) REFERENCES users(id), " +
                            "FOREIGN KEY (saved_post_id) REFERENCES post(id))");

            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS post_liked (" +
                            "post_id INT NOT NULL, " +
                            "liked INT, " +
                            "FOREIGN KEY (post_id) REFERENCES post(id))");

            return "All tables created successfully!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

