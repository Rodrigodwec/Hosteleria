package com.hosteleriapractica.backendpractica.config;

import com.hosteleriapractica.backendpractica.model.Rol;
import com.hosteleriapractica.backendpractica.model.Usuario;
import com.hosteleriapractica.backendpractica.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = Usuario.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .nombre("Administrador")
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build();
            usuarioRepository.save(admin);
        }
    }
}