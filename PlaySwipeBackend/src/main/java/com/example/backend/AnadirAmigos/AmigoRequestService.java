package com.example.backend.AnadirAmigos;

import com.example.backend.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import com.example.backend.AnadirAmigos.AmigoRequest;
import com.example.backend.AnadirAmigos.AmigoRequestRepository;
import com.example.backend.AnadirAmigos.AmigoDTO;
import com.example.backend.AnadirAmigos.UsuarioDto;
import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class AmigoRequestService {

    private final AmigoRequestRepository repo;
    private final UsuarioRepository users;

    public AmigoRequestService(AmigoRequestRepository repo, UsuarioRepository users) {
        this.repo = repo;
        this.users = users;
    }

    public void sendRequest(String fromId, String toId) {
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo.");
        }

        var from = users.findByIdUsuario(fromId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario solicitante no existe"));
        var to = users.findByIdUsuario(toId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario destinatario no existe"));

        boolean yaAmigos = repo.findBySolicitante_IdUsuarioAndEstado(fromId, AmigoRequest.Estado.ACCEPTED)
                .stream().anyMatch(fr -> fr.getDestinatario().getIdUsuario().equals(toId))
                || repo.findByDestinatario_IdUsuarioAndEstado(fromId, AmigoRequest.Estado.ACCEPTED)
                .stream().anyMatch(fr -> fr.getSolicitante().getIdUsuario().equals(toId));

        if (yaAmigos) {
            throw new IllegalStateException("Ya sois amigos.");
        }

        boolean solicitudExiste = repo.findBySolicitante_IdUsuarioAndEstado(fromId, AmigoRequest.Estado.PENDING)
                .stream().anyMatch(fr -> fr.getDestinatario().getIdUsuario().equals(toId))
                || repo.findByDestinatario_IdUsuarioAndEstado(fromId, AmigoRequest.Estado.PENDING)
                .stream().anyMatch(fr -> fr.getSolicitante().getIdUsuario().equals(toId));

        if (solicitudExiste) {
            throw new IllegalStateException("Ya hay una solicitud pendiente.");
        }

        AmigoRequest fr = new AmigoRequest();
        fr.setSolicitante(from);
        fr.setDestinatario(to);
        repo.save(fr);
    }

    public List<AmigoDTO> getPending(String userId) {
        return repo.findByDestinatario_IdUsuarioAndEstado(userId, AmigoRequest.Estado.PENDING)
                .stream().map(AmigoDTO::new).toList();
    }

    public void respond(Long requestId, boolean accept) {
        var fr = repo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada"));
        fr.setEstado(accept ? AmigoRequest.Estado.ACCEPTED : AmigoRequest.Estado.REJECTED);
        repo.save(fr);
    }

    public List<UsuarioDto> getFriends(String userId) {
        var in = repo.findByDestinatario_IdUsuarioAndEstado(userId, AmigoRequest.Estado.ACCEPTED);
        var out = repo.findBySolicitante_IdUsuarioAndEstado(userId, AmigoRequest.Estado.ACCEPTED);
        List<UsuarioDto> res = new ArrayList<>();
        in.forEach(fr -> res.add(new UsuarioDto(fr.getSolicitante())));
        out.forEach(fr -> res.add(new UsuarioDto(fr.getDestinatario())));
        return res;
    }
    public int contarAmigosDeUsuario(String idUsuario) {
        return repo.contarAmigosAceptados(idUsuario);

    }
}
