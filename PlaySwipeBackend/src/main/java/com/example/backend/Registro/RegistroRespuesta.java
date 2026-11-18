package com.example.backend.Registro;

public class RegistroRespuesta {

    private String mensaje;

    public RegistroRespuesta(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}