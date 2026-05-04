package com.social.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Chat {
    @Id
    @GeneratedValue(strategy =GenerationType.AUTO)
    private Integer id;

    private  String chat_name;
    private String char_image;

    @ManyToMany
    private List<User>users=new ArrayList<>();

    private LocalDateTime timestamp;
    @OneToMany(mappedBy = "chat")
    @JsonIgnore
    private List<Message> messages = new ArrayList<>();
}