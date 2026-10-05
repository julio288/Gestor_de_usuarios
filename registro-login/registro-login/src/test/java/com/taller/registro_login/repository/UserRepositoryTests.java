package com.taller.registro_login.repository;

import com.taller.registro_login.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    void debeGuardarYBuscarUsuarioPorEmail() {
        User user = new User();
        user.setEmail("estudiante.prueba@correo.com");
        user.setPassword("temporal");           // @DataJpaTest revierte al final
        user.setFirstName("Estudiante");
        user.setLastName("Prueba");

        User saved = userRepository.saveAndFlush(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findByEmail("estudiante.prueba@correo.com")).isPresent();
        assertThat(userRepository.existsByEmail("estudiante.prueba@correo.com")).isTrue();
    }
}