package shop.francesinha.backend.security;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import shop.francesinha.backend.common.TestContainerConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainerConfig.class)
public class AuthControllerWithRolesIT extends AbstractAuthControllerWithRolesNotRunnableTest {
    //this test will run AbstractAuthControllerWithRolesNotRunnableTest with JpaUserRepository
}
