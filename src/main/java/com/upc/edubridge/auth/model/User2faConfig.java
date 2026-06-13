package com.upc.edubridge.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "user_2fa_config")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User2faConfig {
    @Id
    private String email;
    private boolean enabled;
    private String secret;
}
