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
        
        if (usuarioRepository.findByUsername("camarero1").isEmpty()) {
            Usuario camarero = Usuario.builder()
                    .username("camarero1")
                    .password(passwordEncoder.encode("camarero123"))
                    .nombre("Juan Gómez")
                    .rol(Rol.CAMARERO)
                    .activo(true)
                    .build();
            usuarioRepository.save(camarero);
        }
        
        if (usuarioRepository.findByUsername("camarero2").isEmpty()) {
            Usuario camarero2 = Usuario.builder()
                    .username("camarero2")
                    .password(passwordEncoder.encode("camarero123"))
                    .nombre("Ana López")
                    .rol(Rol.CAMARERO)
                    .activo(true)
                    .build();
            usuarioRepository.save(camarero2);
        }
    }
    
    
}