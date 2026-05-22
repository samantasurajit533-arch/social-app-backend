package com.social.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String firstName;
    private String lastName;
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String gender;
    private String profileImage;
    private String coverPhoto;

    // ✅ FIX: Explicitly name all collection tables
    @ElementCollection
    @CollectionTable(name = "user_followers",
            joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "followers")
    private List<Integer> followers = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_followings",
            joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "followings")
    private List<Integer> followings = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "users_saved_post",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "saved_post_id"))
    private List<Post> savedPost = new ArrayList<>();
}