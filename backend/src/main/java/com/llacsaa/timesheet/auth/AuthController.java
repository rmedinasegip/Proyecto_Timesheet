package com.llacsaa.timesheet.auth;

import com.llacsaa.timesheet.common.NameResolver;
import com.llacsaa.timesheet.master.TinsUser;
import com.llacsaa.timesheet.master.TinsUserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

import static com.llacsaa.timesheet.common.PilotContext.CODECOMPANY;
import static com.llacsaa.timesheet.common.PilotContext.CODEINSTANCE;

/**
 * Login/logout/me. POST /api/auth/login es la única ruta pública de
 * /api/** (ver AuthFilter) — el resto exige la cookie httpOnly
 * "auth_token" firmada por {@link JwtService}. La respuesta de login nunca
 * incluye el token en el body, solo {email, name, role}.
 */
@RestController
public class AuthController {

    private final TinsUserRepository userRepository;
    private final NameResolver nameResolver;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(TinsUserRepository userRepository, NameResolver nameResolver,
                           BCryptPasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.nameResolver = nameResolver;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/api/auth/login")
    public MeResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
        TinsUser user = userRepository.findByIdCodeinstanceAndIdCodecompanyAndEmail(CODEINSTANCE, CODECOMPANY, request.getEmail())
                .filter(u -> u.getPasswordhash() != null && passwordEncoder.matches(request.getPassword(), u.getPasswordhash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        String token = jwtService.sign(user.getId().getCode(), user.getEmail(), user.getRole());
        setCookie(response, token, jwtService.maxAgeSeconds());
        return toMeResponse(user);
    }

    @PostMapping("/api/auth/logout")
    public void logout(HttpServletResponse response) {
        setCookie(response, "", 0);
    }

    @GetMapping("/api/auth/me")
    public MeResponse me() {
        CurrentUser current = AuthContext.current();
        TinsUser user = userRepository.findByIdCodeinstanceAndIdCodecompanyAndIdCode(CODEINSTANCE, CODECOMPANY, current.getCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return toMeResponse(user);
    }

    private MeResponse toMeResponse(TinsUser user) {
        return new MeResponse(
                user.getId().getCode(),
                user.getEmail(),
                nameResolver.userName(user.getId().getCode()),
                user.getRole(),
                nameResolver.catalogItemName(user.getRolecat(), user.getRole())
        );
    }

    private void setCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(JwtService.AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false) // dev/piloto sobre http; poner true al desplegar con https real
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
