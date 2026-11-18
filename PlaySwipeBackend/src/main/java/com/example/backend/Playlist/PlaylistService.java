package com.example.backend.Playlist;

import com.example.backend.Canciones.Cancion;
import com.example.backend.Canciones.CancionRepository;
import com.example.backend.Canciones.CancionRequest;
import com.example.backend.Registro.Usuario;
import com.example.backend.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PlaylistService {

    private final CancionRepository cancionRepo;
    private final PlaylistCancionRepository pcRepo;
    private final PlaylistRepository playlistRepo;
    private final UsuarioRepository usuarioRepo;

    public PlaylistService(CancionRepository cancionRepo,
                           PlaylistCancionRepository pcRepo,
                           PlaylistRepository playlistRepo,
                           UsuarioRepository usuarioRepo) {
        this.cancionRepo = cancionRepo;
        this.pcRepo = pcRepo;
        this.playlistRepo = playlistRepo;
        this.usuarioRepo = usuarioRepo;
    }

    private Playlist getOrCreate(String userId) {
        List<Playlist> playlists = playlistRepo.findAllByUsuario_IdUsuario(userId);

        if (!playlists.isEmpty()) {
            return playlists.get(0);
        }

        Usuario usuario = usuarioRepo.findByIdUsuario(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Playlist nueva = new Playlist();
        nueva.setUsuario(usuario);
        return playlistRepo.save(nueva);
    }

    public void addSongToUserPlaylist(String userId, Long songId) {
        Playlist playlist = getOrCreate(userId);
        if (pcRepo.existsByPlaylist_IdPlaylistAndCancion_Id(playlist.getIdPlaylist(), songId)) {
            return;
        }
        Cancion c = cancionRepo.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("Canción no encontrada"));
        pcRepo.save(new PlaylistCancion(playlist, c));
    }

    public List<CancionRequest> getUserPlaylist(String userId) {
        Playlist playlist = getOrCreate(userId);
        return pcRepo.findByPlaylist_IdPlaylist(playlist.getIdPlaylist()).stream()
                .map(pc -> {
                    Cancion c = pc.getCancion();
                    return new CancionRequest(
                            c.getId(), c.getTitulo(), c.getAutor(),
                            c.getGenero(), c.getArchivo_mp3(), c.getUrlImagen()
                    );
                })
                .collect(Collectors.toList());
    }

    public PlaylistInfo getPlaylistInfo(String userId) {
        Playlist playlist = getOrCreate(userId);
        return new PlaylistInfo(
                playlist.getIdPlaylist().toString(),
                playlist.getUsuario().getIdUsuario(),
                playlist.getNombre(),
                playlist.getTipo(),
                playlist.getFechaCreacion().toString(),
                playlist.getImagenPlaylist()
        );
    }

    public PlaylistInfo renamePlaylist(String userId, String nuevoNombre) {
        Playlist playlist = getOrCreate(userId);
        playlist.setNombre(nuevoNombre);
        playlist = playlistRepo.save(playlist);
        return getPlaylistInfo(userId);
    }

    public PlaylistInfo updatePlaylistImage(String userId, String url) {
        Playlist playlist = getOrCreate(userId);
        playlist.setImagenPlaylist(url);
        playlist = playlistRepo.save(playlist);
        return getPlaylistInfo(userId);
    }

    public void removeSongFromUserPlaylist(String userId, Long songId) {
        Playlist playlist = getOrCreate(userId);
        pcRepo.deleteByIds(playlist.getIdPlaylist(), songId);
    }

    public int contarCancionesFavoritas(String userId) {
        return pcRepo.contarCancionesPorUsuario(userId);
    }

    public Optional<Playlist> getPlaylistByUserId(String userId) {
        return playlistRepo.findFirstByUsuario_IdUsuario(userId);
    }
}
