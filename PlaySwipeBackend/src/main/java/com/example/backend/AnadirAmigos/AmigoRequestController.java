package com.example.backend.AnadirAmigos;

import com.example.backend.Registro.Usuario;
import com.example.backend.UsuarioRepository;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/api/users/{userId}/friends")
@CrossOrigin(origins = "*")
public class AmigoRequestController {

    private final AmigoRequestService service;

    public AmigoRequestController(AmigoRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String,String>> send(
            @PathVariable("userId") String userId,
            @RequestBody MandarAmigoRequest dto
    ) {
        Map<String,String> r = new HashMap<>();
        try {
            service.sendRequest(userId, dto.getToUserId());
            r.put("mensaje","Solicitud enviada");
            return ResponseEntity.ok(r);
        } catch (EntityNotFoundException | IllegalArgumentException | IllegalStateException ex) {
            r.put("mensaje", ex.getMessage());
            return ResponseEntity.status(400).body(r);
        } catch (Exception ex) {
            ex.printStackTrace();
            r.put("mensaje","Error interno");
            return ResponseEntity.status(500).body(r);
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<List<AmigoDTO>> pending(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(service.getPending(userId));
    }

    @PutMapping("/requests/{requestId}")
    public ResponseEntity<Map<String,String>> respond(
            @PathVariable("userId") String userId,
            @PathVariable("requestId") Long requestId,
            @RequestBody RespuestaAmigoRequest dto
    ) {
        Map<String,String> r = new HashMap<>();
        try {
            service.respond(requestId, dto.isAccept());
            r.put("mensaje", dto.isAccept() ? "Amistad aceptada" : "Solicitud rechazada");
            return ResponseEntity.ok(r);
        } catch (EntityNotFoundException ex) {
            r.put("mensaje", ex.getMessage());
            return ResponseEntity.status(404).body(r);
        } catch (Exception ex) {
            ex.printStackTrace();
            r.put("mensaje","Error interno");
            return ResponseEntity.status(500).body(r);
        }
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDto>> friends(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(service.getFriends(userId));
    }

    @GetMapping("/cantidad-amigos")
    public ResponseEntity<Integer>contarAmigos(@PathVariable("userId") String userId) {
        int cantidad = service.contarAmigosDeUsuario(userId);
        return ResponseEntity.ok(cantidad);
    }

}