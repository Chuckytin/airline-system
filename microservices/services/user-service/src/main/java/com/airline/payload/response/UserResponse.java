package com.airline.payload.response;

import com.airline.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private Boolean active;
    private LocalDateTime lastLogin;

}
