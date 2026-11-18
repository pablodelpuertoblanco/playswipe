package com.example.backend.AnadirAmigos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
    @Repository
    interface AmigoRequestRepository extends JpaRepository<AmigoRequest, Long> {
        List<AmigoRequest> findByDestinatario_IdUsuarioAndEstado(String idUsuario, AmigoRequest.Estado estado);
        List<AmigoRequest> findBySolicitante_IdUsuarioAndEstado(String idUsuario, AmigoRequest.Estado estado);

        @Query("SELECT COUNT(a) FROM AmigoRequest a WHERE (a.solicitante.idUsuario = :id OR a.destinatario.idUsuario = :id) AND a.estado = 'ACCEPTED'")
        int contarAmigosAceptados(@Param("id") String idUsuario);
    }

