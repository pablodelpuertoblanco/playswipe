package com.example.backend.AnadirAmigos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AmigoDTO {

        private final Long idSolicitud;
        private final String idUsuario;
        private final String nombreUsuario;
        private final String urlImagenPerfil;

        public AmigoDTO(AmigoRequest fr) {
            this(
                    fr.getId(),
                    fr.getSolicitante().getIdUsuario(),
                    fr.getSolicitante().getNombreUsuario(),
                    fr.getSolicitante().getUrlImagenPerfil()
            );
        }
    }

