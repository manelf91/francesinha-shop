package shop.francesinha.backend.security;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import shop.francesinha.backend.controller.AuthController;
import shop.francesinha.backend.controller.UserController;

@WebMvcTest({AuthController.class, UserController.class})
@ComponentScan(basePackages = "shop.francesinha.backend.security, shop.francesinha.backend.repo, shop.francesinha.backend.service")
public class AuthControllerWithRolesTest extends AbstractAuthControllerWithRolesNotRunnableTest {
    //this test will run AbstractAuthControllerWithRolesNotRunnableTest with InMemoryUserRepository
}
