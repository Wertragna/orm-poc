package org.essential;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.essential.core.Session;
import org.essential.demo.User;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;


public class PoCDemoTest {
    private static final String INIT_SCRIPT = "init.sql";
    private static PostgreSQLContainer<?> postgres;
    private static DataSource dataSource;

    static {
        postgres = new PostgreSQLContainer("postgres:15-alpine")
                .withDatabaseName("integration-tests-db")
                .withUsername("admin")
                .withPassword("admin");
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        initDataSource();
        runInitSqlScript();

    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    public void setUp() {
    }

    @SneakyThrows
    static String getResourceFileAsString(String fileName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader reader = new BufferedReader(inputStreamReader)) {
                return reader
                        .lines()
                        .collect(Collectors.joining(" "));
            }
        }
    }

    @Test
    void test() {
        try (Session session = new Session(dataSource)) {
            User user = session.find(User.class, 1);
            session.find(User.class, 1);
            session.find(User.class, 2);
            Assertions.assertEquals("Bohdana", user.getFirstName());
            user.setFirstName("Bohdana updated");
        }

        User user;
        try (Session session = new Session(dataSource)) {
            user = session.find(User.class, 1);
        }

        Assertions.assertNotNull(user);
        Assertions.assertEquals("Bohdana updated", user.getFirstName());
    }

    private static void runInitSqlScript() {
        String initSql = getResourceFileAsString(INIT_SCRIPT);
        if (initSql == null) {
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute(initSql);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());
        dataSource = new HikariDataSource(hikariConfig);
    }

}