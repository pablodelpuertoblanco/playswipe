package com.example.backend.Buscador;

import com.example.backend.Canciones.CancionRequest;   // ← el DTO con “id”
import com.example.backend.Canciones.CancionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buscador")
@CrossOrigin(origins = "*")
public class Buscador {

    private final CancionService cancionService;

    public Buscador(CancionService cancionService) {
        this.cancionService = cancionService;
    }

    @GetMapping("/canciones")
    public List<CancionRequest> obtenerCanciones(
            @RequestParam int inicio,
            @RequestParam(defaultValue = "5") int cantidad) {

        return cancionService.obtenerCanciones(inicio, cantidad);
    }
}
