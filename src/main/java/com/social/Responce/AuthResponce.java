package com.social.Responce;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // This generates the constructor with parameters
@NoArgsConstructor
public class AuthResponce {

    private String  token;
    private String message;


}
