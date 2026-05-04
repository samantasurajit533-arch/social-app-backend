package com.social.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  Integer id;

   @ManyToOne
   @JsonIgnore
    private  User user;

    private String image;
    private String captions;
    private LocalDateTime timeStamp;
}
