package shop.francesinha.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import shop.francesinha.backend.repo.IUserRepository;

import java.util.Set;

@Component
public class AdminInitializer {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @EventListener(ApplicationReadyEvent.class)
    public void ensureAdminExists() {
        if (userRepository.countByRolesContains("ADMIN") == 0) {
            userRepository.save("admin", passwordEncoder.encode("admin"), Set.of("ADMIN"));
            logger.debug("Admin user created with username 'admin'");
        }
    }
}
