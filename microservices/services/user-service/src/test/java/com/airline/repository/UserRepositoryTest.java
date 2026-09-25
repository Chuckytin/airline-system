package com.airline.repository;

import com.airline.config.JpaAuditingConfig;
import com.airline.enums.UserRole;
import com.airline.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("john@example.com")
                .password("encodedPassword123")
                .fullName("John Doe")
                .phoneNumber("+34600000000")
                .role(UserRole.ROLE_USER)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should save and find user by ID")
    void shouldSaveAndFindById() {
        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindByEmail() {
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("john@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("notfound@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check exists by email")
    void shouldCheckExistsByEmail() {
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("john@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when checking non-existent email")
    void shouldReturnFalseWhenNotExists() {
        boolean exists = userRepository.existsByEmail("notfound@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should auto-populate createdAt and updatedAt")
    void shouldAutoPopulateAuditFields() {
        User saved = userRepository.save(user);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

}