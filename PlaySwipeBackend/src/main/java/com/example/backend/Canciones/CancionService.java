package com.example.backend.Canciones;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para la tabla <i>cancion</i>.
 *  • registrarCancion  → inserta registro vía JPA.
 *  • obtenerCanciones  → lista paginada vía JDBC (incluye id).
 */
@Service
public class CancionService {

    private final CancionRepository cancionRepository;
    private final JdbcTemplate jdbcTemplate;

    public CancionService(CancionRepository cancionRepository, JdbcTemplate jdbcTemplate) {
        this.cancionRepository = cancionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /* ---------- INSERT JPA ---------- */
    public void registrarCancion(Cancion cancion) {
        cancionRepository.save(cancion);
    }

    /* ---------- SELECT paginado ---------- */
    public List<CancionRequest> obtenerCanciones(int inicio, int cantidad) {

        final String sql = """
        SELECT id_cancion AS id, titulo, autor, genero, archivo_mp3, url_imagen
        FROM cancion
        LIMIT ?, ?
        """;
        return jdbcTemplate.query(
                sql,
                ps -> {
                    ps.setInt(1, inicio);
                    ps.setInt(2, cantidad);
                },
                (rs, rowNum) -> {
                    String titulo = rs.getString("titulo") != null ? rs.getString("titulo") : "";
                    String autor = rs.getString("autor") != null ? rs.getString("autor") : "";
                    String genero = rs.getString("genero") != null ? rs.getString("genero") : "";
                    String archivo = rs.getString("archivo_mp3") != null ? rs.getString("archivo_mp3") : "";
                    String imagen = rs.getString("url_imagen") != null ? rs.getString("url_imagen") : "";

                    return new CancionRequest(rs.getLong("id"), titulo, autor, genero, archivo, imagen);
                }
        );
    }
}
