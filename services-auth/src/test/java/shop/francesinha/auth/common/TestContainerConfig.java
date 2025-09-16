package shop.francesinha.auth.common;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;

@TestConfiguration
public class TestContainerConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public MySQLContainer<?> mySQLContainer() {
        MySQLContainer<?> container = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");

        container.start();
        return container;
    }

    @Bean
    public DataSource dataSource(MySQLContainer<?> mysql) {
        return DataSourceBuilder.create()
                .url(mysql.getJdbcUrl())
                .username(mysql.getUsername())
                .password(mysql.getPassword())
                .build();
    }
}
