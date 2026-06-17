package com.tfg.streaming.users_service.config;

import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Comprobamos si la base de datos esta vacia de administradores
        if (userRepository.findByEmail("admin@vodeo.com").isEmpty()) {
            User admin = new User();
            admin.setUsername("Administrador Supremo");
            admin.setEmail("admin@vodeo.com");
            admin.setPassword(passwordEncoder.encode("asd"));
            admin.setRole(User.Role.ADMIN);

            userRepository.save(admin);
            System.out.println("[VODEO] Usuario ADMIN creado automáticamente: admin@vodeo.com / asd");
        }
    }
}