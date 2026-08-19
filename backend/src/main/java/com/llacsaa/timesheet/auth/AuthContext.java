package com.llacsaa.timesheet.auth;

/**
 * Usuario autenticado de la petición HTTP en curso, poblado por
 * {@link AuthFilter} y leído por los servicios en lugar del antiguo
 * PilotContext.CURRENT_USER_CODE fijo de las Fases 1-4. ThreadLocal porque
 * cada request de Servlet corre en su propio hilo sincrónico en este
 * proyecto (sin WebFlux/hilos reactivos que invaliden ese supuesto).
 */
public final class AuthContext {

    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    static void set(CurrentUser user) {
        CURRENT.set(user);
    }

    static void clear() {
        CURRENT.remove();
    }

    public static CurrentUser current() {
        CurrentUser user = CURRENT.get();
        if (user == null) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto actual");
        }
        return user;
    }

    public static long currentUserCode() {
        return current().getCode();
    }

    public static boolean isAuthorizer() {
        return "AUT".equals(current().getRole());
    }
}
