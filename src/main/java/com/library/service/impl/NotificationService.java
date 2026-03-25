package com.library.service.impl;

import com.library.enums.TipoNotificacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Microservicio simulado de notificaciones.
 * En producción se reemplazaría por llamada HTTP o mensaje en cola.
 */
@Service
@Slf4j
public class NotificationService {

    /**
     * Envía una notificación (simulada: solo loguea)
     */
    public void enviar(Long usuarioId, String email, TipoNotificacion tipo, String mensaje) {
        log.info("[NOTIFICACION] tipo={} | usuarioId={} | email={} | mensaje={}",
                tipo, usuarioId, email, mensaje);
    }

    public void prestamoConfirmado(Long usuarioId, String email, String tituloLibro) {
        enviar(usuarioId, email, TipoNotificacion.PRESTAMO_CONFIRMADO,
                "Tu préstamo de '" + tituloLibro + "' ha sido confirmado.");
    }

    public void prestamoPorVencer(Long usuarioId, String email, String tituloLibro, long diasRestantes) {
        enviar(usuarioId, email, TipoNotificacion.PRESTAMO_POR_VENCER,
                "Tu préstamo de '" + tituloLibro + "' vence en " + diasRestantes + " días.");
    }

    public void prestamoVencido(Long usuarioId, String email, String tituloLibro) {
        enviar(usuarioId, email, TipoNotificacion.PRESTAMO_VENCIDO,
                "Tu préstamo de '" + tituloLibro + "' está vencido. Se ha generado una multa.");
    }

    public void reservaDisponible(Long usuarioId, String email, String tituloLibro) {
        enviar(usuarioId, email, TipoNotificacion.RESERVA_DISPONIBLE,
                "El libro '" + tituloLibro + "' que reservaste ya está disponible.");
    }

    public void multaGenerada(Long usuarioId, String email, String tituloLibro, String monto) {
        enviar(usuarioId, email, TipoNotificacion.MULTA_GENERADA,
                "Se generó una multa de $" + monto + " por devolución tardía de '" + tituloLibro + "'.");
    }
}
