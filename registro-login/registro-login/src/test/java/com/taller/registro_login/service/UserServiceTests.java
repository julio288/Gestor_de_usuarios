package com.taller.registro_login.service;

import com.taller.registro_login.model.User;
import com.taller.registro_login.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserService userService = new UserService(userRepository, passwordEncoder, "admin2027");

    @Test
    void publicRegistrationAlwaysAssignsUserRoleAndEncodesPassword() {
        User user = user("  Regular@Example.Test  ", "plain-password");
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.register(user);

        assertThat(saved.getEmail()).isEqualTo("regular@example.test");
        assertThat(saved.getRole()).isEqualTo(User.ROLE_USER);
        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        verify(userRepository).existsByEmail("regular@example.test");
    }

    @Test
    void adminRegistrationAssignsAdminRoleAndEncodesPassword() {
        User user = user("admin@example.test", "plain-password");
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.registerAdmin(user, "admin2027");

        assertThat(saved.getRole()).isEqualTo(User.ROLE_ADMIN);
        assertThat(saved.getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void publicAdminRegistrationRequiresTheConfiguredCode() {
        User user = user("admin@example.test", "plain-password");

        assertThatThrownBy(() -> userService.registerAdmin(user, "incorrecto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El código de administrador no es válido.");
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void adminRegistrationRejectsRequestsWhenNoCodeIsConfigured() {
        UserService serviceWithoutCode = new UserService(userRepository, passwordEncoder, "");
        User user = user("admin@example.test", "plain-password");

        assertThatThrownBy(() -> serviceWithoutCode.registerAdmin(user, "any-code"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El código de administrador no es válido.");
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void updateProfileChangesOnlyContactDetails() {
        User existing = user("before@example.test", "encoded-password");
        existing.setRole(User.ROLE_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User updated = userService.updateProfile(1L, " After@Example.Test ", "Nuevo", "Nombre");

        assertThat(updated.getEmail()).isEqualTo("after@example.test");
        assertThat(updated.getFirstName()).isEqualTo("Nuevo");
        assertThat(updated.getLastName()).isEqualTo("Nombre");
        assertThat(updated.getRole()).isEqualTo(User.ROLE_ADMIN);
        assertThat(updated.getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void refusesDeletingTheCurrentAdmin() {
        User admin = user("admin@example.test", "encoded-password");
        admin.setId(1L);
        admin.setRole(User.ROLE_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.deleteUser(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("propia cuenta");
        verify(userRepository, never()).delete(admin);
    }

    @Test
    void refusesDeletingTheLastAdmin() {
        User admin = user("other-admin@example.test", "encoded-password");
        admin.setId(2L);
        admin.setRole(User.ROLE_ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(User.ROLE_ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.deleteUser(2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("último administrador");
        verify(userRepository, never()).delete(admin);
    }

    private User user(String email, String password) {
        return new User(email, password, "Nombre", "Apellido");
    }
}