package shop.francesinha.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.service.UserService;

@Component
public class AdminInitializer {
    @Autowired
    private UserService userService;

    private final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @EventListener(ApplicationReadyEvent.class)
    public void ensureAdminExists() {
        if (userService.countByRole("ADMIN") == 0) {
            User defaultAdmin = userService.getDefaultAdminUser();
            userService.saveUser(defaultAdmin);
            logger.debug("Admin user created with username 'admin'");
        }
    }
}
