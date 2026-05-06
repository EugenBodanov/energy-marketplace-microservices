package com.energy.marketplace.user;

import com.energy.marketplace.user.adapter.out.persistence.UserPersistenceAdapter;
import com.energy.marketplace.user.domain.model.User;
import com.energy.marketplace.user.domain.valueObject.Email;
import com.energy.marketplace.user.domain.valueObject.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ExtendWith(UserApplicationTests.DockerRequiredCondition.class)
class UserApplicationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserPersistenceAdapter userPersistenceAdapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void startsContextWithFlywayMigrationAndPersistsUsers() {
        assertThat(countRows(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success = true"
        )).isEqualTo(1);
        assertThat(countRows(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'users'"
        )).isEqualTo(1);
        assertThat(countRows(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'password_hash'"
        )).isEqualTo(1);

        User savedUser = userPersistenceAdapter.save(User.registerNew(
                "Alice Prosumer",
                new Email("alice@example.com"),
                "hashed-password",
                UserRole.PROSUMER
        ));

        assertThat(savedUser.getId()).isNotNull();
        assertThat(userPersistenceAdapter.existsByEmail(new Email("alice@example.com"))).isTrue();

        Optional<User> loadedById = userPersistenceAdapter.loadById(savedUser.getId());
        Optional<User> loadedByEmail = userPersistenceAdapter.loadByEmail(new Email("alice@example.com"));

        assertThat(loadedById).isPresent();
        assertThat(loadedByEmail).isPresent();
        assertThat(loadedById.get().getEmail()).isEqualTo(new Email("alice@example.com"));
        assertThat(loadedById.get().getRole()).isEqualTo(UserRole.PROSUMER);
        assertThat(loadedByEmail.get().getId()).isEqualTo(savedUser.getId());

        var row = jdbcTemplate.queryForMap(
                "SELECT role, status FROM users WHERE id = ?",
                savedUser.getId().value()
        );

        assertThat(row)
                .containsEntry("role", "PROSUMER")
                .containsEntry("status", "ACTIVE");
    }

    private Integer countRows(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    static class DockerRequiredCondition implements ExecutionCondition {

        private static final Logger log = LoggerFactory.getLogger(DockerRequiredCondition.class);

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            try {
                if (DockerClientFactory.instance().isDockerAvailable()) {
                    return ConditionEvaluationResult.enabled("Docker is available");
                }
            } catch (Throwable ex) {
                return skipTest(ex);
            }

            return skipTest(null);
        }

        private ConditionEvaluationResult skipTest(Throwable ex) {
            String message = "Docker is not available. Skipping PostgreSQL integration test.";

            if (ex == null) {
                log.warn(message);
            } else {
                log.warn("{} Cause: {}", message, ex.toString());
            }

            return ConditionEvaluationResult.disabled(message);
        }
    }
}
