package com.example.backend.Canciones;

import com.example.backend.Registro.RegistroRespuesta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/canciones")
public class CancionController {

    @Autowired
    private CancionService cancionService;

    @GetMapping
    public List<CancionRequest> obtenerCanciones(
            @RequestParam int inicio,
            @RequestParam(defaultValue = "5") int cantidad) {
        return cancionService.obtenerCanciones(inicio, cantidad);
    }


    @PostMapping("/nueva")
    public ResponseEntity<RegistroRespuesta> registrarCancion(
            @RequestBody CancionRequest cancionRequest) {

        Cancion nueva = new Cancion();
        nueva.setTitulo (cancionRequest.getTitulo());
        nueva.setAutor (cancionRequest.getAutor());
        nueva.setGenero (cancionRequest.getGenero());
        nueva.setArchivo_mp3 (cancionRequest.getArchivo_mp3());
        nueva.setUrlImagen (cancionRequest.getUrlImagen());

        cancionService.registrarCancion(nueva);
        return ResponseEntity.ok(
                new RegistroRespuesta("Canción registrada correctamente"));
    }
}
