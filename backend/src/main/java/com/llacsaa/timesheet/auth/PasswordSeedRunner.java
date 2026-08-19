package com.llacsaa.timesheet.auth;

import com.llacsaa.timesheet.master.TinsUser;
import com.llacsaa.timesheet.master.TinsUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Siembra la contraseña de demo del piloto para todo tins_user cuyo
 * passwordhash siga en null (idempotente — no toca usuarios ya sembrados),
 * mismo criterio que seedAdminUser() en el proyecto Next.js hermano: un
 * hash bcrypt no se puede escribir a mano en SQL plano de forma práctica,
 * así que se genera aquí en el primer arranque en vez de en V6__auth.sql.
 *
 * Contraseña de demo para los 10 consultores sembrados en Fase 1: "Llacsaa2026".
 * Documentado también en docs/erd.md — cambiar antes de cualquier uso real.
 */
@Component
public class PasswordSeedRunner implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "Llacsaa2026";

    private final TinsUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordSeedRunner(TinsUserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<TinsUser> pending = userRepository.findByPasswordhashIsNull();
        if (pending.isEmpty()) {
            return;
        }
        String hash = passwordEncoder.encode(DEMO_PASSWORD);
        for (TinsUser user : pending) {
            user.setPasswordhash(hash);
        }
        userRepository.saveAll(pending);
    }
}
