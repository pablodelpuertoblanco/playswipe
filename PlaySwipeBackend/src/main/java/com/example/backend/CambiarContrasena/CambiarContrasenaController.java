package com.example.backend.CambiarContrasena;

import com.example.backend.Registro.Usuario;
import com.example.backend.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
    @RequestMapping("/api/cambiar-contrasena")
    @CrossOrigin(origins = "*")
    public class CambiarContrasenaController {

        @Autowired
        private UsuarioRepository repo;

        @PostMapping
        public Map<String, String> cambiarContrasena(@RequestBody Map<String, String> datos) {
            String nombreUsuario = datos.get("nombreUsuario");
            String actual = datos.get("contrasenaActual");
            String nueva = datos.get("nuevaContrasena");

            Map<String, String> respuesta = new HashMap<>();

            Optional<Usuario> optionalUsuario = repo.findByNombreUsuario(nombreUsuario);

            if (optionalUsuario.isPresent() && optionalUsuario.get().getPassword().equals(actual)) {
                Usuario usuario = optionalUsuario.get();
                usuario.setPassword(nueva);
                repo.save(usuario);
                respuesta.put("estado", "ok");
                respuesta.put("mensaje", "Contraseña cambiada correctamente");
            } else {
                respuesta.put("estado", "error");
                respuesta.put("mensaje", "La contraseña actual no es válida");
            }

            return respuesta;
        }
    }

