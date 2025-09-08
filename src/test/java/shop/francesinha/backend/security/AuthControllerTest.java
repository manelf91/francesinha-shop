package shop.francesinha.backend.security;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import shop.francesinha.backend.controller.AuthController;

@WebMvcTest(AuthController.class)
@ComponentScan(basePackages = "shop.francesinha.backend.security, shop.francesinha.backend.repo, shop.francesinha.backend.config")
public class AuthControllerTest extends AbstractAuthControllerNotRunnableTest {
    //this test will run AbstractAuthControllerNotRunnableTest with InMemoryUserRepository
}
