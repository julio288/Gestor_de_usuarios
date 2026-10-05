package com.taller.registro_login.config;

import com.taller.registro_login.model.User;
import com.taller.registro_login.repository.UserRepository;
import com.taller.registro_login.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserAccessTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void dashboardRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void regularUserCanOpenDashboard() throws Exception {
        User regularUser = new User("dashboard-user@example.test", "encoded", "Usuario", "Prueba");

        mockMvc.perform(get("/dashboard").with(user(new CustomUserDetails(regularUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

            @Test
            void regularUserCanUpdateOwnProfile() throws Exception {
            User regularUser = userRepository.save(
                new User("profile-user@example.test", "encoded-password", "Usuario", "Prueba"));

            mockMvc.perform(post("/profile")
                    .with(user(new CustomUserDetails(regularUser)))
                    .with(csrf())
                    .param("email", "profile-updated@example.test")
                    .param("firstName", "Nombre")
                    .param("lastName", "Actualizado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

            User updatedUser = userRepository.findById(regularUser.getId()).orElseThrow();
            assertEquals("profile-updated@example.test", updatedUser.getEmail());
            assertEquals("Nombre", updatedUser.getFirstName());
            assertEquals("Actualizado", updatedUser.getLastName());
            }

    @Test
    void regularUserCannotOpenAdminUserList() throws Exception {
        User regularUser = new User("user@example.test", "encoded", "Usuario", "Prueba");

        mockMvc.perform(get("/users").with(user(new CustomUserDetails(regularUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCannotOpenAdminPanel() throws Exception {
        User regularUser = new User("regular-admin-test@example.test", "encoded", "Usuario", "Prueba");

        mockMvc.perform(get("/admin").with(user(new CustomUserDetails(regularUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanOpenAdminPanel() throws Exception {
        User admin = new User("admin-panel-test@example.test", "encoded", "Admin", "Prueba");
        admin.setRole(User.ROLE_ADMIN);

        mockMvc.perform(get("/admin").with(user(new CustomUserDetails(admin))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }

    @Test
    void administratorCanOpenUserList() throws Exception {
        User admin = new User("admin@example.test", "encoded", "Admin", "Prueba");
        admin.setRole(User.ROLE_ADMIN);

        mockMvc.perform(get("/users").with(user(new CustomUserDetails(admin))))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios"));
    }
}