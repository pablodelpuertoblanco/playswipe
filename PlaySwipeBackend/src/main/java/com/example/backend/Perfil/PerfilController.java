package com.example.backend.Perfil;

import com.example.backend.AnadirAmigos.UsuarioDto;
import com.example.backend.Registro.Usuario;
import com.example.backend.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.backend.Playlist.PlaylistService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/perfil")
@CrossOrigin(origins = "*")
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PlaylistService playlistService;

    //Guardar o actualizar URL imagen perfil
    @PostMapping("/imagenUrl")
    public ResponseEntity<Map<String, String>> guardarUrlImagen(
            @RequestParam String id_usuario,
            @RequestParam String urlImagen
    ) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id_usuario);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setUrlImagenPerfil(urlImagen);
            usuarioRepository.save(usuario);
            return ResponseEntity.ok(Map.of("mensaje", "Imagen URL guardada"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "Usuario no encontrado"));
        }
    }

    @GetMapping("/imagen/{id_usuario}")
    public ResponseEntity<Map<String, String>> obtenerUrlImagen(@PathVariable String id_usuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id_usuario);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getUrlImagenPerfil() != null) {
                return ResponseEntity.ok(Map.of("urlImagen", usuario.getUrlImagenPerfil()));
            }
        }
        return ResponseEntity.status(404).body(Map.of("mensaje", "Imagen no encontrada o usuario no existe"));
    }

    @GetMapping("/favoritas/{id_usuario}")
    public ResponseEntity<Integer> obtenerCantidadFavoritas(@PathVariable String id_usuario) {
        int cantidad = playlistService.contarCancionesFavoritas(id_usuario);
        return ResponseEntity.ok(cantidad);
    }
    @GetMapping("/usuario/{id_usuario}")
    public ResponseEntity<UsuarioDto> obtenerUsuarioPorId(@PathVariable String id_usuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id_usuario);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            UsuarioDto dto = new UsuarioDto(usuario);
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}