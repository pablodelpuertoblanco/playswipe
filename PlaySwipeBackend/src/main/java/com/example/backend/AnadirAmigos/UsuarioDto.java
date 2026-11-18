package com.example.backend.AnadirAmigos;

import com.example.backend.Registro.Usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioDto {
    private String idUsuario;
    private String nombreUsuario;
    private String urlImagenPerfil;

    public UsuarioDto(Usuario u) {
        this.idUsuario = u.getIdUsuario();
        this.nombreUsuario = u.getNombreUsuario();
        this.urlImagenPerfil = u.getUrlImagenPerfil();
    }

}
