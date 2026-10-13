package com.hotel.Hotel.service;

import com.hotel.Hotel.domain.*;
import com.hotel.Hotel.dto.request.CrearReservaRequest;
import com.hotel.Hotel.dto.response.ReservaResponse;
import com.hotel.Hotel.exception.ReglaNegocioException;
import com.hotel.Hotel.mapper.ReservaMapper;
import com.hotel.Hotel.repository.ClienteRepository;
import com.hotel.Hotel.repository.HabitacionRepository;
import com.hotel.Hotel.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceCrearTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private ReservaMapper reservaMapper;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    @DisplayName("Debe crear la reserva en estado PENDIENTE y calcular el costo total")
    void debeCrearReservaPendienteYCalcularCosto() {
        // Arrange
        Cliente cliente = new Cliente("Laura Pérez", "laura@correo.com");
        Habitacion habitacion = new HabitacionEstandar("305", 2, 150.0, 2);
        LocalDateTime entrada = LocalDateTime.now().plusDays(1);
        LocalDateTime salida = entrada.plusDays(3);
        CrearReservaRequest request = new CrearReservaRequest(
                cliente.getId(), habitacion.getId(), entrada, salida);

        ReservaResponse responseEsperado = new ReservaResponse(
                UUID.randomUUID(), "Laura Pérez", "305", entrada, salida, 450.0, "PENDIENTE");

        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(habitacionRepository.findById(habitacion.getId())).thenReturn(Optional.of(habitacion));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseEsperado);

        // Act
        ReservaResponse resultado = reservaService.crear(request);

        // Assert
        ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository, times(1)).save(captor.capture());
        Reserva reservaGuardada = captor.getValue();

        assertSame(cliente, reservaGuardada.getCliente());
        assertSame(habitacion, reservaGuardada.getHabitacion());
        assertEquals(EstadoReserva.PENDIENTE, reservaGuardada.getEstado());
        assertEquals(450.0, reservaGuardada.getCostoTotal(), 0.001); // 3 noches x 150
        assertSame(responseEsperado, resultado);
        verify(reservaMapper, times(1)).toResponse(reservaGuardada);
    }

    @Test
    @DisplayName("Debe rechazar la reserva si el cliente está inactivo")
    void debeRechazarReservaDeClienteInactivo() {
        // Arrange
        Cliente cliente = new Cliente("Pedro Ruiz", "pedro@correo.com");
        cliente.registrarPenalizacion();
        cliente.registrarPenalizacion();
        cliente.registrarPenalizacion(); // con 3 penalizaciones queda inactivo
        Habitacion suite = new SuitePresidencial("PH-01", 4, 800.0, true, true);
        LocalDateTime entrada = LocalDateTime.now().plusDays(1);
        LocalDateTime salida = entrada.plusDays(2);
        CrearReservaRequest request = new CrearReservaRequest(
                cliente.getId(), suite.getId(), entrada, salida);

        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(habitacionRepository.findById(suite.getId())).thenReturn(Optional.of(suite));

        // Act
        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class,
                () -> reservaService.crear(request));

        // Assert
        assertEquals("El cliente está inactivo o penalizado y no puede crear reservas.", excepcion.getMessage());
        verify(reservaRepository, never()).save(any(Reserva.class));
        verifyNoInteractions(reservaMapper);
    }
}