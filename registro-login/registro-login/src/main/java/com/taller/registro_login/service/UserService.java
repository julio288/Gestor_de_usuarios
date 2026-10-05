package com.taller.registro_login.service;

import com.taller.registro_login.model.User;
import com.taller.registro_login.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminRegistrationCode;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${APP_ADMIN_REGISTRATION_CODE:}") String adminRegistrationCode) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminRegistrationCode = adminRegistrationCode;
    }

    @Transactional
    public User register(User user) {
        return register(user, User.ROLE_USER);
    }

    @Transactional
    User registerSeededAdmin(User user) {
        return register(user, User.ROLE_ADMIN);
    }

    @Transactional
    public User registerAdmin(User user, String suppliedCode) {
        if (adminRegistrationCode.isBlank() || !Objects.equals(adminRegistrationCode, suppliedCode)) {
            throw new IllegalArgumentException("El código de administrador no es válido.");
        }
        return register(user, User.ROLE_ADMIN);
    }

    private User register(User user, String role) {
        String normalizedEmail = normalizeEmail(user.getEmail());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado.");
        }
        user.setEmail(normalizedEmail);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario solicitado no existe."));
    }

    @Transactional
    public User updateProfile(Long id, String email, String firstName, String lastName) {
        User user = findById(id);
        String normalizedEmail = normalizeEmail(email);
        if (!Objects.equals(user.getEmail(), normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado.");
        }
        user.setEmail(normalizedEmail);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id, Long actorId) {
        User user = findById(id);
        if (Objects.equals(user.getId(), actorId)) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
        }
        if (User.ROLE_ADMIN.equals(user.getRole())
                && userRepository.countByRole(User.ROLE_ADMIN) <= 1) {
            throw new IllegalArgumentException("No se puede eliminar al último administrador.");
        }
        userRepository.delete(user);
    }
}