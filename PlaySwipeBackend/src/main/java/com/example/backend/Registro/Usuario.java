package com.example.backend.Registro;

import com.example.backend.UsuarioRepository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;


@SpringBootApplication

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Column(name = "imagen_perfil")
    private String urlImagenPerfil;
    @Id
    @Column(name = "id_usuario")
    private String idUsuario;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "nombre_usuario", nullable = false, unique = true)
    private String nombreUsuario;

    @Column(name = "nombre_apellidos", nullable = false)
    private String nombreApellidos;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "estado", nullable = false)
    private String estado = "Activo";

    @Column(name = "password", nullable = false)
    private String password;
}

@Service
class RegistroService {
    @Autowired private UsuarioRepository repo;
    boolean emailExists(String email){ return repo.findByEmail(email).isPresent(); }
    boolean usernameExists(String u){ return repo.findByNombreUsuario(u).isPresent(); }

    Usuario guardar( String email, String nombreUsuario, String nombreApellidos, String password, String urlImagenPerfil) {
        Usuario u = new Usuario();
        u.setIdUsuario(generarIdUsuario());
        u.setEmail(email);
        u.setNombreUsuario(nombreUsuario);
        u.setNombreApellidos(nombreApellidos);
        u.setFechaRegistro(LocalDate.now());
        u.setPassword(password);
        u.setUrlImagenPerfil(urlImagenPerfil);
        return repo.save(u);
    }
    private static String generarIdUsuario() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int index = (int) (caracteres.length() * Math.random());
            id.append(caracteres.charAt(index));
        }
        return id.toString();
    }
}

record RegistroRequest( String email, String password, String nombreUsuario,
                        String nombreApellidos,
                        String urlImagenPerfil
) {}

@RestController
@RequestMapping("/api/registro")
@CrossOrigin(origins = "*")
class RegistroController {

    @Autowired private RegistroService servicio;

    @PostMapping
    public ResponseEntity<Map<String,String>> registrarFull(@RequestBody RegistroRequest dto) {

        System.out.println("Registro recibido:");
        System.out.println("Email: " + dto.email());
        System.out.println("NombreUsuario: " + dto.nombreUsuario());
        System.out.println("NombreApellidos: " + dto.nombreApellidos());

        // Validaciones
        if (servicio.emailExists(dto.email())) {
            return ResponseEntity
                    .badRequest().body(Map.of("mensaje", "Email ya registrado"));
        }
        if (servicio.usernameExists(dto.nombreUsuario())) {
            return ResponseEntity
                    .badRequest().body(Map.of("mensaje", "Nombre de usuario ya registrado"));
        }

        servicio.guardar(
                dto.email(),
                dto.nombreUsuario(),
                dto.nombreApellidos(),
                dto.password(),
                dto.urlImagenPerfil());

        return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado correctamente"));
    }
}
