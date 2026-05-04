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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String firstName;
    private String lastName;
    private String email;


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String gender;

    private String profileImage;
    private String coverPhoto;


    @Enumerated(EnumType.STRING)
    private Mood currentMood;


    @ElementCollection
    private List<Integer> followers = new ArrayList<>();

    @ElementCollection
    private List<Integer> followings = new ArrayList<>();

    @ManyToMany
    private List<Post> savedPost = new ArrayList<>();
}
