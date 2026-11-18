package com.example.backend.Canciones;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cancion")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
public class Cancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cancion")
    private Long id;

    @Column
    private String titulo;

    @Column
    private String autor;

    @Column
    private String genero;

    @Column(name = "archivo_mp3")
    private String archivo_mp3;

    @Column(name = "urlImagen")
    private String urlImagen;

}
