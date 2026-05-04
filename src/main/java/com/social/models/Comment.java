package com.social.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  Integer id;

    private String content;

    @ManyToOne
    @JsonIgnore
    private User user;

    @ManyToMany
    @JsonIgnore
    private List<User> liked=new ArrayList<>();

    @OneToMany
    @JsonIgnore
    private List<Comment>comments=new ArrayList<>();

    private LocalDateTime createdAt;


}
