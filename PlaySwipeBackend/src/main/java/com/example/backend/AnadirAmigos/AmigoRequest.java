package com.example.backend.AnadirAmigos;

import com.example.backend.Registro.Usuario;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "amigo")
@Getter
@Setter
@NoArgsConstructor

public class AmigoRequest {


        public enum Estado { PENDING, ACCEPTED, REJECTED }

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_amigo")
        private Long id;

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "id_usuario1")
        private Usuario solicitante;

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "id_usuario2")
        private Usuario destinatario;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private Estado estado = Estado.PENDING;

        @Column(name = "fecha_solicitud", nullable = false)
        private LocalDate fechaSolicitud = LocalDate.now();

    }


