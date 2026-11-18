package com.example.backend.Canciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancionRepository extends JpaRepository<Cancion, Long> {
    @Query("SELECT new com.example.backend.Canciones.CancionDTO(" +
            "c.id, c.titulo, c.autor, c.archivo_mp3, c.urlImagen) " +
            "FROM PlaylistCancion pc JOIN pc.cancion c " +
            "WHERE pc.playlist.idPlaylist = :idPlaylist")
    List<CancionDTO> findCancionesByPlaylist(@Param("idPlaylist") int idPlaylist);
}