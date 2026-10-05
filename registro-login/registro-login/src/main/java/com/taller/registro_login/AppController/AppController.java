package com.taller.registro_login.AppController;

import com.taller.registro_login.dto.UserEditForm;
import com.taller.registro_login.dto.RegistrationForm;
import com.taller.registro_login.model.User;
import com.taller.registro_login.security.CustomUserDetails;
import com.taller.registro_login.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
public class AppController {

    private static final String PROFILE_VIEW = "profile";
    private static final String MESSAGE_ATTRIBUTE = "message";

    private final UserService userService;

    public AppController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String viewHomePage() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("fullName", principal.getFullName());
        return "dashboard";
    }

    @GetMapping("/admin")
    public String adminPanel(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("fullName", principal.getFullName());
        return "admin";
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("profileForm", UserEditForm.from(userService.findById(principal.getUserId())));
        return PROFILE_VIEW;
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails principal,
                                @Valid @ModelAttribute("profileForm") UserEditForm profileForm,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return PROFILE_VIEW;
        }
        try {
            userService.updateProfile(principal.getUserId(), profileForm.getEmail(),
                    profileForm.getFirstName(), profileForm.getLastName());
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            return PROFILE_VIEW;
        }
        redirectAttributes.addFlashAttribute(MESSAGE_ATTRIBUTE, "Tu perfil se actualizó correctamente.");
        return "redirect:/profile";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "registro";
    }

    @PostMapping("/process_register")
    public String processRegister(@Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm,
                                BindingResult result,
                                Model model) {
        if (!Objects.equals(registrationForm.getPassword(), registrationForm.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch", "Las contraseñas no coinciden.");
        }
        if (result.hasErrors()) {
            return "registro";
        }
        try {
            User user = registrationForm.toUser();
            if (User.ROLE_USER.equals(registrationForm.getAccountType())) {
                userService.register(user);
            } else if (User.ROLE_ADMIN.equals(registrationForm.getAccountType())) {
                userService.registerAdmin(user, registrationForm.getAdminCode());
            } else {
                throw new IllegalArgumentException("Selecciona un tipo de cuenta válido.");
            }
            return "registro_exitoso";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "registro";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/users")
    public String listUsers(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
        model.addAttribute("listUsers", userService.findAll());
        model.addAttribute("actorEmail", principal.getUsername());
        return "usuarios";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("userId", id);
        model.addAttribute("userForm", UserEditForm.from(user));
        return "usuario_editar";
    }

    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id,
                           @Valid @ModelAttribute("userForm") UserEditForm userForm,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("userId", id);
            return "usuario_editar";
        }
        try {
            userService.updateProfile(id, userForm.getEmail(), userForm.getFirstName(), userForm.getLastName());
        } catch (IllegalArgumentException exception) {
            model.addAttribute("userId", id);
            model.addAttribute("formError", exception.getMessage());
            return "usuario_editar";
        }
        redirectAttributes.addFlashAttribute(MESSAGE_ATTRIBUTE, "Los datos del usuario se actualizaron.");
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails principal,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id, principal.getUserId());
            redirectAttributes.addFlashAttribute(MESSAGE_ATTRIBUTE, "El usuario se eliminó.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/users";
    }
}