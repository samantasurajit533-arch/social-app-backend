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
        StringBuilder result = new StringBuilder();

        // 1. Fix post_liked — wrong column name, recreate with correct schema
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS post_liked");
            jdbcTemplate.execute(
                    "CREATE TABLE post_liked (" +
                            "post_id INT NOT NULL, " +
                            "liked_id INT NOT NULL, " +
                            "PRIMARY KEY (post_id, liked_id), " +
                            "FOREIGN KEY (post_id) REFERENCES post(id), " +
                            "FOREIGN KEY (liked_id) REFERENCES users(id))");
            result.append("✅ post_liked created\n");
        } catch (Exception e) {
            result.append("❌ post_liked: ").append(e.getMessage()).append("\n");
        }

        // 2. chat_users table
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS chat_users (" +
                            "chat_id INT NOT NULL, " +
                            "users_id INT NOT NULL, " +
                            "PRIMARY KEY (chat_id, users_id), " +
                            "FOREIGN KEY (chat_id) REFERENCES chat(id), " +
                            "FOREIGN KEY (users_id) REFERENCES users(id))");
            result.append("✅ chat_users created\n");
        } catch (Exception e) {
            result.append("❌ chat_users: ").append(e.getMessage()).append("\n");
        }

        // 3. reels_seq table (sequence for reels ID generation)
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS reels_seq (" +
                            "next_val BIGINT NOT NULL, " +
                            "PRIMARY KEY (next_val))");
            jdbcTemplate.execute(
                    "INSERT IGNORE INTO reels_seq VALUES (1)");
            result.append("✅ reels_seq created\n");
        } catch (Exception e) {
            result.append("❌ reels_seq: ").append(e.getMessage()).append("\n");
        }

        // 4. user_followers (already exists but verify)
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS user_followers (" +
                            "user_id INT NOT NULL, " +
                            "followers INT NOT NULL DEFAULT 0, " +
                            "PRIMARY KEY (user_id, followers), " +
                            "FOREIGN KEY (user_id) REFERENCES users(id))");
            result.append("✅ user_followers ok\n");
        } catch (Exception e) {
            result.append("⚠️ user_followers: ").append(e.getMessage()).append("\n");
        }

        // 5. user_followings (already exists but verify)
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS user_followings (" +
                            "user_id INT NOT NULL, " +
                            "followings INT NOT NULL DEFAULT 0, " +
                            "PRIMARY KEY (user_id, followings), " +
                            "FOREIGN KEY (user_id) REFERENCES users(id))");
            result.append("✅ user_followings ok\n");
        } catch (Exception e) {
            result.append("⚠️ user_followings: ").append(e.getMessage()).append("\n");
        }

        // 6. users_saved_post (already exists but verify)
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS users_saved_post (" +
                            "user_id INT NOT NULL, " +
                            "saved_post_id INT NOT NULL, " +
                            "PRIMARY KEY (user_id, saved_post_id), " +
                            "FOREIGN KEY (user_id) REFERENCES users(id), " +
                            "FOREIGN KEY (saved_post_id) REFERENCES post(id))");
            result.append("✅ users_saved_post ok\n");
        } catch (Exception e) {
            result.append("⚠️ users_saved_post: ").append(e.getMessage()).append("\n");
        }

        return result.toString();
    }
}