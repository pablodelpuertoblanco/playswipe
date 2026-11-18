package com.example.backend.Canciones;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CancionDTO {

    private Long id;
    private String titulo;
    private String autor;
    private String archivo_mp3;
    private String url_imagen;
}

