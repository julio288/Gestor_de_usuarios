package com.taller.registro_login.AppController;

import com.taller.registro_login.model.User;
import com.taller.registro_login.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void registersAValidatedUser() throws Exception {
        when(userService.register(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/process_register").with(csrf())
                        .param("email", "  New.User@Example.test  ")
                        .param("password", "secure-pass-123")
                        .param("confirmPassword", "secure-pass-123")
                        .param("firstName", "  Ana  ")
                        .param("lastName", "  López  "))
                .andExpect(status().isOk())
                .andExpect(view().name("registro_exitoso"));

        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userService).register(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("new.user@example.test");
        assertThat(userCaptor.getValue().getFirstName()).isEqualTo("Ana");
        assertThat(userCaptor.getValue().getLastName()).isEqualTo("López");
    }

    @Test
    void registersAnAdministratorOnlyThroughTheAdminCodeFlow() throws Exception {
        when(userService.registerAdmin(any(User.class), org.mockito.ArgumentMatchers.eq("admin2027")))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/process_register").with(csrf())
                        .param("email", "admin@example.test")
                        .param("password", "secure-pass-123")
                        .param("confirmPassword", "secure-pass-123")
                        .param("firstName", "Admin")
                        .param("lastName", "Test")
                        .param("accountType", User.ROLE_ADMIN)
                        .param("adminCode", "admin2027"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro_exitoso"));

        verify(userService).registerAdmin(any(User.class), org.mockito.ArgumentMatchers.eq("admin2027"));
        verify(userService, never()).register(any(User.class));
    }

    @Test
    void rejectsDifferentPasswordsWithoutCallingRegistrationService() throws Exception {
        mockMvc.perform(post("/process_register").with(csrf())
                        .param("email", "new.user@example.test")
                        .param("password", "secure-pass-123")
                        .param("confirmPassword", "different-pass-456")
                        .param("firstName", "Ana")
                        .param("lastName", "López"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "confirmPassword"));

        verify(userService, never()).register(any(User.class));
    }

    @Test
    void redisplaysFormWhenEmailIsAlreadyRegistered() throws Exception {
        when(userService.register(any(User.class)))
                .thenThrow(new IllegalArgumentException("El correo electrónico ya se encuentra registrado."));

        MvcResult result = mockMvc.perform(post("/process_register").with(csrf())
                        .param("email", "existing@example.test")
                        .param("password", "secure-pass-123")
                        .param("confirmPassword", "secure-pass-123")
                        .param("firstName", "Ana")
                        .param("lastName", "López"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"))
                .andReturn();

        assertThat(result.getModelAndView().getModel()).containsKey("error");
    }
}