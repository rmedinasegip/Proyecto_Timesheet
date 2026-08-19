package com.llacsaa.timesheet.common;

/**
 * Contexto multi-instancia/multi-compañía fijo para este piloto (no hay
 * selector de instancia en ningún mockup de la especificación).
 *
 * El usuario "actor" (usercreate/userlastmodify/userchange) ya NO es fijo
 * desde Fase 5 — viene del JWT del usuario autenticado, ver
 * {@code com.llacsaa.timesheet.auth.AuthContext#currentUserCode()}.
 */
public final class PilotContext {

    public static final String CODEINSTANCE = "LLACSAA";
    public static final String CODECOMPANY = "LLA-EC";

    private PilotContext() {
    }
}
