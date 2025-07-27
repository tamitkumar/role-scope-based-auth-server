package com.tech.brain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User information for registration")
public class UserInfo {

    @Schema(example = "amit")
    private String name;

    @Schema(example = "amit@xyz.com")
    private String email;

    @Schema(example = "securePassword")
    private String password;

    @Schema(
            description = "Role name to be assigned",
            name = "roles",
            example = "ADMIN",
            allowableValues = {"ADMIN", "CLIENT", "SERVICE", "MANAGER", "HR", "USER"}
    )
    private RoleName roles;
}
