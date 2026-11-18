package com.example.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.example.backend.Registro.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByIdUsuario(String idUsuario);
}
