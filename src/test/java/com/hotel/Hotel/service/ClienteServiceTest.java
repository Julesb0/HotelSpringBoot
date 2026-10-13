package com.hotel.Hotel.service;

import com.hotel.Hotel.domain.Cliente;
import com.hotel.Hotel.dto.request.CrearClienteRequest;
import com.hotel.Hotel.dto.response.ClienteResponse;
import com.hotel.Hotel.mapper.ClienteMapper;
import com.hotel.Hotel.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("Debe crear un cliente activo y sin penalizaciones")
    void debeCrearClienteActivoSinPenalizaciones() {
        // Arrange
        CrearClienteRequest request = new CrearClienteRequest("Ana Torres", "ana@correo.com");
        ClienteResponse responseEsperado = new ClienteResponse(
                UUID.randomUUID(), "Ana Torres", "ana@correo.com", true, 0);

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clienteMapper.toResponse(any(Cliente.class))).thenReturn(responseEsperado);

        // Act
        ClienteResponse resultado = clienteService.crear(request);

        // Assert
        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository, times(1)).save(captor.capture());
        Cliente clienteGuardado = captor.getValue();

        assertNotNull(clienteGuardado.getId());
        assertEquals("Ana Torres", clienteGuardado.getNombre());
        assertEquals("ana@correo.com", clienteGuardado.getEmail());
        assertTrue(clienteGuardado.isActivo());
        assertEquals(0, clienteGuardado.getPenalizaciones());
        assertSame(responseEsperado, resultado);
    }
}