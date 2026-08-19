package com.llacsaa.timesheet.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Usuario autenticado de la petición actual — ver {@link AuthContext}. */
@Data
@AllArgsConstructor
public class CurrentUser {
    private Long code;
    private String email;
    private String role;
}
