package com.example.backend.Canciones;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CancionRequest {

    private Long id;
    private String titulo;
    private String autor;
    private String genero;
    private String archivo_mp3;
    private String urlImagen;
}
