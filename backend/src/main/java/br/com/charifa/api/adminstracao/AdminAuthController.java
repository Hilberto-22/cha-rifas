package br.com.charifa.api.adminstracao;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.charifa.domain.AdminLoginRequest;
import br.com.charifa.domain.AdminLoginResponse;
import br.com.charifa.service.JwtService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AdminAuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager; 
        this.jwtService = jwtService;
    }
    
    @PostMapping("/login")
    public AdminLoginResponse login(@Valid @RequestBody AdminLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        return jwtService.issue(authentication.getName());
    }
}
