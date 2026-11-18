package com.example.backend.Playlist;

import com.example.backend.Canciones.Cancion;
import com.example.backend.Canciones.CancionRequest;
import com.example.backend.Registro.RegistroRespuesta;
import com.example.backend.Registro.Usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "playlist")
class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_playlist")
    private Long idPlaylist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    private String nombre = "Mi playlist";
    private String tipo = "Privada";

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion = LocalDate.now();

    @Column(name = "imagen_playlist")
    private String imagenPlaylist;
}

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "playlist_cancion")
class PlaylistCancion {

    @EmbeddedId
    private PlaylistCancionPK id = new PlaylistCancionPK();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(name = "id_playlist")
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cancionId")
    @JoinColumn(name = "id_cancion")
    private Cancion cancion;

    @Column(name = "fecha_agregado")
    private LocalDate fechaAgregado = LocalDate.now();

    public PlaylistCancion(Playlist playlist, Cancion cancion) {
        this.playlist = playlist;
        this.cancion = cancion;
        this.id.playlistId = playlist.getIdPlaylist();
        this.id.cancionId = cancion.getId();
    }

public Cancion getCancion() { return cancion; }
}

@Getter
@Setter
@NoArgsConstructor
@Embeddable
 class PlaylistCancionPK implements Serializable {
    @Column(name = "id_playlist")
    Long playlistId;
    @Column(name = "id_cancion")
    Long cancionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaylistCancionPK pk)) return false;
        return playlistId.equals(pk.playlistId) && cancionId.equals(pk.cancionId);
    }
    @Override
    public int hashCode() {
        return playlistId.hashCode() + cancionId.hashCode();
    }
}

@Repository
interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Optional<Playlist> findFirstByUsuario_IdUsuario(String idUsuario);
    List<Playlist> findAllByUsuario_IdUsuario(String userId);
    boolean existsByUsuario_IdUsuario(String idUsuario);
}

@Repository
interface PlaylistCancionRepository extends JpaRepository<PlaylistCancion, PlaylistCancionPK> {
    boolean existsByPlaylist_IdPlaylistAndCancion_Id(Long playlistId, Long cancionId);
    List<PlaylistCancion> findByPlaylist_IdPlaylist(Long playlistId);

    @Transactional
    @Modifying
    @Query("DELETE FROM PlaylistCancion pc WHERE pc.id.playlistId = :playlistId AND pc.id.cancionId = :cancionId")
    void deleteByIds(@Param("playlistId") Long playlistId, @Param("cancionId") Long cancionId);
    void deleteByPlaylist_IdPlaylistAndCancion_Id(Long playlistId, Long cancionId);

    @Query("SELECT COUNT(pc) FROM PlaylistCancion pc JOIN pc.playlist p WHERE p.usuario.idUsuario = :idUsuario")
    int contarCancionesPorUsuario(@Param("idUsuario") String idUsuario);
}

@Getter
@Setter
@NoArgsConstructor
class PlaylistInfo {
    private String id_playlist;
    private String id_usuario;
    private String nombre;
    private String tipo;
    private String fecha_creacion;
    private String imagen_playlist;

    public PlaylistInfo(String id_playlist, String id_usuario, String nombre,
                        String tipo, String fecha_creacion, String imagen_playlist) {
        this.id_playlist = id_playlist;
        this.id_usuario = id_usuario;
        this.nombre = nombre;
        this.tipo = tipo;
        this.fecha_creacion = fecha_creacion;
        this.imagen_playlist = imagen_playlist;
    }
}

@RestController
@RequestMapping("/api/users/{userId}/playlist")
@CrossOrigin(origins = "*")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    // Añadir canción a la playlist del usuario
    @PostMapping
    public ResponseEntity<RegistroRespuesta> addSong(
            @PathVariable String userId,
            @RequestBody SongPlaylistRequest req) {
        playlistService.addSongToUserPlaylist(userId, req.getSongId());
        return ResponseEntity.ok(new RegistroRespuesta("Canción añadida correctamente"));
    }

    // Obtener todas las canciones de la playlist
    @GetMapping
    public List<CancionRequest> getPlaylist(@PathVariable String userId) {
        return playlistService.getUserPlaylist(userId);
    }

    // Información de la playlist (nombre, fecha, imagen, etc.)
    @GetMapping("/info")
    public PlaylistInfo getInfo(@PathVariable String userId) {
        return playlistService.getPlaylistInfo(userId);
    }

    // Renombrar la playlist
    @PutMapping
    public ResponseEntity<RegistroRespuesta> rename(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        playlistService.renamePlaylist(userId, body.get("nombre"));
        return ResponseEntity.ok(new RegistroRespuesta("Nombre de la playlist actualizado"));
    }

    // Actualizar la imagen de la playlist
    @PutMapping("/imagen")
    public PlaylistInfo updateImage(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        return playlistService.updatePlaylistImage(userId, body.get("imagenPlaylist"));
    }

    public static class SongPlaylistRequest {
        private Long songId;
        public SongPlaylistRequest() {}
        public SongPlaylistRequest(Long songId) { this.songId = songId; }
        public Long getSongId() { return songId; }
        public void setSongId(Long songId) { this.songId = songId; }
    }

    // Eliminar canción de la playlist del usuario
    @DeleteMapping("/cancion/{songId}")
    public ResponseEntity<RegistroRespuesta> removeSong(
            @PathVariable String userId,
            @PathVariable Long songId) {
        playlistService.removeSongFromUserPlaylist(userId, songId);
        return ResponseEntity.ok(new RegistroRespuesta("Canción eliminada de la playlist"));
    }

    // Nuevo endpoint: obtener la URL de la imagen de la playlist del usuario
    @GetMapping("/imagen")
    public ResponseEntity<Map<String, String>> obtenerImagenPlaylist(@PathVariable String userId) {
        Optional<Playlist> playlistOpt = playlistService.getPlaylistByUserId(userId);
        if (playlistOpt.isPresent()) {
            String url = playlistOpt.get().getImagenPlaylist();
            System.out.println("🎵 Imagen de playlist para usuario " + userId + ": " + url);
            if (url != null && !url.isBlank()) {
                return ResponseEntity.ok(Map.of("urlImagen", url));
            }
        }
        return ResponseEntity.status(404).body(Map.of("mensaje", "Imagen no encontrada"));
    }
}
