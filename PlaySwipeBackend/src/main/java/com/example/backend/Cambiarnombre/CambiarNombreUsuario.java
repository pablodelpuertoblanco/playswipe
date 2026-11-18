package com.example.backend.Cambiarnombre;

import com.example.backend.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import com.example.backend.Registro.Usuario;

@RestController
@RequestMapping("/api/cambiar-nombre")
public class CambiarNombreUsuario{

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public CambiarNombreUsuario(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> cambiarNombreUsuario(@RequestBody Map<String, String> request) {
        String nombreUsuarioActual = request.get("nombreUsuarioActual");
        String nuevoNombreUsuario = request.get("nuevoNombreUsuario");

        if (nombreUsuarioActual == null || nuevoNombreUsuario == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Faltan datos requeridos"));
        }

        Optional<Usuario> usuarioOptional = usuarioRepository.findByNombreUsuario(nombreUsuarioActual);

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            usuario.setNombreUsuario(nuevoNombreUsuario);
            usuarioRepository.save(usuario);
            return ResponseEntity.ok(Map.of("mensaje", "Nombre de usuario actualizado correctamente"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "Usuario no encontrado"));
        }
    }
}
