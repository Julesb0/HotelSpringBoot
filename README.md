# Pruebas unitarias – Sistema de Reservas de Hotel

Actividad de pruebas unitarias sobre el proyecto **HotelSpringBoot** visto en clase. Se implementaron 5 pruebas con **JUnit 5** y **Mockito** para la capa de servicios (`ReservaService` y `ClienteService`). Los repositorios y los mappers se reemplazan por mocks, así que no hace falta tener PostgreSQL encendido para ejecutarlas.


## Tecnologías

- Java 17
- Spring Boot 3.3.4
- JUnit 5 (Jupiter 5.10.3)
- Mockito 5.11.0
- Maven (Maven Wrapper)

## Pruebas realizadas

| # | Clase | Prueba | Qué valida |
|---|-------|--------|------------|
| 1 | `ReservaServiceCrearTest` | `debeCrearReservaPendienteYCalcularCosto` | La reserva se crea en estado PENDIENTE y cuesta noches × precio por noche (3 × 150 = 450) |
| 2 | `ReservaServiceCrearTest` | `debeRechazarReservaDeClienteInactivo` | Un cliente con 3 penalizaciones no puede reservar |
| 3 | `ReservaServiceCrearTest` | `debeRechazarFechasInvalidas` | Una fecha de salida anterior a la de entrada se rechaza sin consultar la base de datos |
| 4 | `ClienteServiceTest` | `debeCrearClienteActivoSinPenalizaciones` | Un cliente nuevo se guarda activo y con 0 penalizaciones |
| 5 | `ClienteServiceTest` | `debeLanzarExcepcionSiClienteNoExiste` | Buscar un ID que no existe lanza `IllegalArgumentException` |

Las pruebas 1, 2 y 3 recorren caminos distintos del método `crear` de `ReservaService`:

<!-- Arrastra aquí la imagen flujo-crear-reserva.png -->

## Ubicación

```
src/test/java/com/hotel/Hotel/service/
├── ReservaServiceCrearTest.java   → pruebas 1, 2 y 3
└── ClienteServiceTest.java        → pruebas 4 y 5
```

- [`ReservaServiceCrearTest.java`](src/test/java/com/hotel/Hotel/service/ReservaServiceCrearTest.java)
- [`ClienteServiceTest.java`](src/test/java/com/hotel/Hotel/service/ClienteServiceTest.java)

## Elementos de Mockito usados

- `@Mock` y `@InjectMocks` para crear los mocks y pasarlos al servicio.
- `when(...).thenReturn(...)` y `thenAnswer(...)` para definir qué responden los mocks.
- `ArgumentCaptor` para revisar el objeto que el servicio mandó a guardar.
- `verify(..., times(1))`, `never()` y `verifyNoInteractions(...)` para comprobar cómo se usaron los mocks.

## Cómo ejecutarlas

Requisito: JDK 17 o superior. Los comandos se ejecutan desde la carpeta del proyecto. En Windows con PowerShell o CMD se usa `.\mvnw.cmd` en lugar de `./mvnw`.

### Prueba 1: crear una reserva válida

Simula que el cliente y la habitación existen, crea una reserva de 3 noches y, con un `ArgumentCaptor`, revisa que la reserva guardada quede en estado PENDIENTE y con costo 450.

```bash
./mvnw test "-Dtest=ReservaServiceCrearTest#debeCrearReservaPendienteYCalcularCosto"
```

<!-- Pega aquí la captura de la prueba 1 -->

### Prueba 2: rechazar a un cliente inactivo

Al cliente se le registran 3 penalizaciones, con lo que queda inactivo, y se espera una `ReglaNegocioException`. También verifica que no se guardó ninguna reserva.

```bash
./mvnw test "-Dtest=ReservaServiceCrearTest#debeRechazarReservaDeClienteInactivo"
```

<!-- Pega aquí la captura de la prueba 2 -->

### Prueba 3: rechazar fechas inválidas

La fecha de salida es anterior a la de entrada. Se espera una `ReglaNegocioException` y se verifica que el servicio no consultó ningún repositorio.

```bash
./mvnw test "-Dtest=ReservaServiceCrearTest#debeRechazarFechasInvalidas"
```

<!-- Pega aquí la captura de la prueba 3 -->

### Prueba 4: registrar un cliente

Crea un cliente y, con un `ArgumentCaptor`, revisa que se guarde con el nombre y el correo de la solicitud, activo y con 0 penalizaciones.

```bash
./mvnw test "-Dtest=ClienteServiceTest#debeCrearClienteActivoSinPenalizaciones"
```

<!-- Pega aquí la captura de la prueba 4 -->

### Prueba 5: buscar un cliente que no existe

El repositorio devuelve `Optional.empty()` y se espera una `IllegalArgumentException` con el ID en el mensaje. También verifica que el mapper no se usó.

```bash
./mvnw test "-Dtest=ClienteServiceTest#debeLanzarExcepcionSiClienteNoExiste"
```

<!-- Pega aquí la captura de la prueba 5 -->

### Las 5 pruebas juntas

```bash
./mvnw test "-Dtest=ReservaServiceCrearTest,ClienteServiceTest"
```

Resultado esperado: `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` y `BUILD SUCCESS`.

<!-- Pega aquí la captura de las 5 pruebas -->

## Cómo comprobar que las pruebas detectan errores

Si se hace uno de estos cambios temporales en el código del proyecto, la prueba correspondiente debe fallar. Al deshacer el cambio, vuelve a pasar.

| Prueba | Cambio temporal | Resultado esperado |
|--------|-----------------|--------------------|
| 1 | En `Reserva.java`, cambiar `periodo.getDias()` por `periodo.getDias() - 1` | Falla: `expected: <450.0> but was: <300.0>` |
| 2 | En `Cliente.java`, cambiar `>= 3` por `> 3` | Falla: no se lanza la excepción |
| 3 | En `ReservaService.java`, cambiar la condición de la validación de fechas por `false` | Falla: se lanza otra excepción |
| 4 | En `ClienteService.java`, intercambiar `request.nombre()` y `request.email()` | Falla: el nombre no coincide |
| 5 | En `ClienteService.java`, cambiar el texto `"Cliente no encontrado con ID: "` | Falla: el mensaje no coincide |

## Autor

Julio Bolaños — POO, Universidad Cooperativa de Colombia.
