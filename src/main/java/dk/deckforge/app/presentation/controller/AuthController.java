package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.dto.AuthResponse;
import dk.deckforge.app.application.dto.RegisterRequest;
import dk.deckforge.app.application.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(RegisterRequest request, Model model) {

        AuthResponse response = authService.register(request);

        return "redirect:/login";
    }

}
