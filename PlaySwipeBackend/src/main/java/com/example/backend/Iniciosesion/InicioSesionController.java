package com.example.backend.Iniciosesion;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.UsuarioRepository;
import com.example.backend.Registro.Usuario;

//CONTROLADOR LOGIN CLÁSICO (usuario + password)
@RestController
@RequestMapping("login")
@CrossOrigin
public class InicioSesionController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<Map<String, String>> loginConUsuarioPassword(
            @RequestBody Map<String, String> body) {

        String nombreUsuario = body.get("nombreUsuario");
        String password = body.get("password");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getPassword().equals(password)) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Contraseña incorrecta"));
        }

        // Login exitoso
        return ResponseEntity.ok(Map.of(
                "mensaje", "Inicio de sesión exitoso",
                "nombre", usuario.getNombreUsuario(),
                "id_usuario", usuario.getIdUsuario().toString(),
                "urlImagenPerfil", usuario.getUrlImagenPerfil() != null ? usuario.getUrlImagenPerfil() : ""
        ));
    }
}
