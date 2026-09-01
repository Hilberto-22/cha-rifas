package br.com.charifa.config;

import br.com.charifa.domain.AdminUser;
import br.com.charifa.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserInitializer implements ApplicationRunner {
    private final AdminUserRepository repository;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public AdminUserInitializer(AdminUserRepository repository, PasswordEncoder encoder,
            @Value("${app.admin.username}") String username, @Value("${app.admin.password}") String password) {
        this.repository = repository; this.encoder = encoder; this.username = username; this.password = password;
    }

    @Override @Transactional
    public void run(ApplicationArguments args) {
        repository.findByUsername(username).ifPresentOrElse(user -> {
            if (!encoder.matches(password, user.getPasswordHash())) user.changePassword(encoder.encode(password));
        }, () -> repository.save(new AdminUser(username, encoder.encode(password))));
    }
}
