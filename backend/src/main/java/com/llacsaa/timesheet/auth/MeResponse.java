package com.llacsaa.timesheet.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

/** No incluye el token — el JWT solo viaja como cookie httpOnly, nunca en el body. */
@Data
@AllArgsConstructor
public class MeResponse {
    private Long code;
    private String email;
    private String name;
    private String role;
    private String roleName;
}
