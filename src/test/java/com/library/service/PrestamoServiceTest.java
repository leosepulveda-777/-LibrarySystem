package com.library.service;

import com.library.config.LibraryProperties;
import com.library.dto.request.PrestamoRequest;
import com.library.dto.response.PrestamoResponse;
import com.library.entity.*;
import com.library.enums.*;
import com.library.exception.BusinessException;
import com.library.repository.*;
import com.library.service.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrestamoService - Tests unitarios")
class PrestamoServiceTest {

    @Mock private PrestamoRepository prestamoRepository;
    @Mock private LectorRepository lectorRepository;
    @Mock private EjemplarRepository ejemplarRepository;
    @Mock private LibroDigitalRepository libroDigitalRepository;
    @Mock private MultaRepository multaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private LibraryProperties props;
    @Mock private NotificationService notificationService;
    @Mock private CatalogoService catalogoService;

    @InjectMocks
    private PrestamoService prestamoService;

    private Lector lectorMock;
    private Libro libroMock;
    private Ejemplar ejemplarMock;
    private PrestamoRequest req;

    @BeforeEach
    void setUp() {
        Usuario usuario = Usuario.builder().id(1L).nombre("Ana").apellido("Gomez")
                .email("ana@test.com").rol(Role.LECTOR).activo(true).build();

        lectorMock = Lector.builder().id(1L).usuario(usuario)
                .numeroCarnet("LIB-001").activo(true).maxPrestamos(3).build();

        libroMock = Libro.builder().id(10L).titulo("Clean Code").isbn("978-0-13-235088-4")
                .activo(true).autores(List.of()).categorias(List.of())
                .ejemplares(List.of()).librosDigitales(List.of()).reservas(List.of()).build();

        ejemplarMock = Ejemplar.builder().id(5L).libro(libroMock)
                .codigoInventario("INV-001").estado(EstadoEjemplar.DISPONIBLE).activo(true).build();

        req = new PrestamoRequest();
        req.setLectorId(1L);
        req.setLibroId(10L);
        req.setEjemplarId(5L);
    }

    @Test
    @DisplayName("prestarFisico() - debe crear préstamo y cambiar estado del ejemplar")
    void prestarFisico_ShouldCreateLoanAndUpdateEjemplar() {
        when(lectorRepository.findById(1L)).thenReturn(Optional.of(lectorMock));
        when(multaRepository.findByLectorIdAndEstadoIn(anyLong(), anyList())).thenReturn(List.of());
        when(prestamoRepository.countByLectorIdAndEstado(anyLong(), any())).thenReturn(0L);
        when(ejemplarRepository.findById(5L)).thenReturn(Optional.of(ejemplarMock));
        when(reservaRepository.findColaByLibroId(anyLong())).thenReturn(List.of());
        when(props.getLoanDaysPhysical()).thenReturn(14);
        when(prestamoRepository.save(any())).thenAnswer(i -> {
            Prestamo p = i.getArgument(0);
            p = Prestamo.builder().id(99L).lector(lectorMock).ejemplar(ejemplarMock)
                    .libro(libroMock).fechaPrestamo(LocalDateTime.now())
                    .fechaDevolucionEsperada(LocalDateTime.now().plusDays(14))
                    .estado(EstadoPrestamo.ACTIVO).esDigital(false)
                    .numeroRenovaciones(0).build();
            return p;
        });

        PrestamoResponse response = prestamoService.prestarFisico(req);

        assertThat(response).isNotNull();
        assertThat(response.isEsDigital()).isFalse();
        verify(ejemplarRepository).save(argThat(e -> e.getEstado() == EstadoEjemplar.PRESTADO));
        verify(notificationService).prestamoConfirmado(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("prestarFisico() - debe fallar si lector tiene multas pendientes")
    void prestarFisico_ShouldFailIfHasPendingFines() {
        when(lectorRepository.findById(1L)).thenReturn(Optional.of(lectorMock));
        Multa multa = Multa.builder().id(1L).estado(EstadoMulta.PENDIENTE).build();
        when(multaRepository.findByLectorIdAndEstadoIn(anyLong(), anyList())).thenReturn(List.of(multa));

        assertThatThrownBy(() -> prestamoService.prestarFisico(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("multas pendientes");
    }

    @Test
    @DisplayName("prestarFisico() - debe fallar si lector alcanzó límite de préstamos")
    void prestarFisico_ShouldFailIfMaxLoansReached() {
        when(lectorRepository.findById(1L)).thenReturn(Optional.of(lectorMock));
        when(multaRepository.findByLectorIdAndEstadoIn(anyLong(), anyList())).thenReturn(List.of());
        when(prestamoRepository.countByLectorIdAndEstado(anyLong(), any())).thenReturn(3L);

        assertThatThrownBy(() -> prestamoService.prestarFisico(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("límite de préstamos");
    }

    @Test
    @DisplayName("renovar() - debe fallar si se superó el límite de renovaciones")
    void renovar_ShouldFailIfMaxRenewalsReached() {
        Prestamo prestamo = Prestamo.builder().id(1L).lector(lectorMock).libro(libroMock)
                .estado(EstadoPrestamo.ACTIVO).numeroRenovaciones(2).esDigital(false).build();

        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(props.getMaxRenewals()).thenReturn(2);

        assertThatThrownBy(() -> prestamoService.renovar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("límite de renovaciones");
    }

    @Test
    @DisplayName("devolver() - debe generar multa si hay retraso")
    void devolver_ShouldGenerateFineIfLate() {
        Prestamo prestamo = Prestamo.builder().id(1L).lector(lectorMock).libro(libroMock)
                .ejemplar(ejemplarMock).estado(EstadoPrestamo.ACTIVO).esDigital(false)
                .fechaPrestamo(LocalDateTime.now().minusDays(20))
                .fechaDevolucionEsperada(LocalDateTime.now().minusDays(5))
                .numeroRenovaciones(0).build();

        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(reservaRepository.findFirstByLibroIdAndEstadoOrderByPosicionColaAsc(anyLong(), any()))
                .thenReturn(Optional.empty());
        when(props.getFinePerDay()).thenReturn(500.0);
        when(prestamoRepository.save(any())).thenReturn(prestamo);

        prestamoService.devolver(1L);

        verify(multaRepository).save(argThat(m -> m.getDiasRetraso() > 0));
        verify(notificationService).multaGenerada(anyLong(), anyString(), anyString(), anyString());
    }
}
