package hoteles.modelo;

import hoteles.modelo.dao.DAOFactory;
import hoteles.util.ConexionBD;
import hoteles.util.EjecutorScript;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de integración de la capa de modelo contra MySQL real, en una base de datos aparte
 * ({@code hoteles_test}) que se recrea con el mismo script del proyecto, para no tocar los datos
 * de la aplicación. Servidor: propiedad {@code -Dtest.db.host=localhost:3306} (valor por defecto).
 */
class DatosTest {

    private static final LocalDate DIA_1 = LocalDate.of(2030, 6, 1);
    private static final LocalDate DIA_5 = LocalDate.of(2030, 6, 5);
    private static final LocalDate DIA_10 = LocalDate.of(2030, 6, 10);
    private static final String PARAMETROS = "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";

    private Datos datos;
    private int hotelId;
    private int clienteId;
    private int habitacionId;

    @BeforeAll
    static void crearBaseDeDatosDePruebas() throws Exception {
        String host = System.getProperty("test.db.host", "localhost:3306");
        System.setProperty("db.url", "jdbc:mysql://" + host + "/hoteles_test" + PARAMETROS);
        String script = Files.readString(Path.of("sql/01_schema.sql"), StandardCharsets.UTF_8)
                .replaceAll("\\bhoteles\\b", "hoteles_test");
        try (Connection con = ConexionBD.getConexion("jdbc:mysql://" + host + "/" + PARAMETROS,
                "hoteles_app", "hoteles_app_2026")) {
            // El usuario de la aplicación ya existe: se omiten las sentencias de usuarios y permisos.
            EjecutorScript.ejecutar(con, script, s -> !s.startsWith("CREATE USER") && !s.startsWith("GRANT"));
        }
    }

    @BeforeEach
    void preparar() throws Exception {
        try (Connection con = ConexionBD.getConexion(); Statement st = con.createStatement()) {
            st.execute("DELETE FROM linea_reserva");
            st.execute("DELETE FROM reserva");
            st.execute("DELETE FROM habitacion");
            st.execute("DELETE FROM cliente");
            st.execute("DELETE FROM hotel");
        }
        datos = new Datos(DAOFactory.getDAOFactory("MYSQL"));
        hotelId = datos.altaHotel("Hotel Test", "Calle 1", "Valencia", "960000000", "test@hotel.com", 3).getId();
        habitacionId = datos.altaHabitacion(hotelId, "101", 1, CategoriaHabitacion.DOBLE,
                new BigDecimal("80.00")).getId();
        clienteId = datos.altaCliente("12345678Z", "Ana", "García", "ana@email.com", "600000000", "C/ Mayor").getId();
    }

    private int contar(String tabla) throws Exception {
        try (Connection con = ConexionBD.getConexion(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // ------------------------------------------------------------------ reservas

    @Test
    void creaReservaConfirmadaConSuImporte() throws HotelesException {
        Reserva reserva = datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(0, new BigDecimal("320.00").compareTo(reserva.calcularImporte()));
    }

    @Test
    void laReservaSeRecuperaDeLaBaseDeDatosConSusLineas() throws HotelesException {
        Reserva creada = datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));

        Reserva leida = datos.obtenerReserva(creada.getId());

        assertEquals("Ana García", leida.getCliente().getNombreCompleto());
        assertEquals("Hotel Test", leida.getHotel().getNombre());
        assertEquals(1, leida.getLineas().size());
        assertEquals(DIA_1, leida.getFechaEntrada());
    }

    @Test
    void rechazaReservaQueSolapaConOtraDeLaMismaHabitacion() throws HotelesException {
        datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));

        assertThrows(HabitacionNoDisponibleException.class, () ->
                datos.crearReserva(clienteId, hotelId, LocalDate.of(2030, 6, 3), DIA_10, 2, List.of(habitacionId)));
    }

    @Test
    void permiteEntrarElMismoDiaQueSaleOtraReserva() throws HotelesException {
        datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));

        Reserva siguiente = datos.crearReserva(clienteId, hotelId, DIA_5, DIA_10, 2, List.of(habitacionId));

        assertEquals(DIA_5, siguiente.getFechaEntrada());
    }

    @Test
    void unaReservaCanceladaLiberaLaHabitacion() throws HotelesException {
        Reserva primera = datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));
        datos.cancelarReserva(primera.getId());

        assertTrue(datos.habitacionesDisponibles(hotelId, DIA_1, DIA_5, 0).stream()
                .anyMatch(h -> h.getId() == habitacionId));
        datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));
    }

    @Test
    void rechazaFechaDeSalidaAnteriorALaEntrada() {
        assertThrows(ValidacionException.class, () ->
                datos.crearReserva(clienteId, hotelId, DIA_5, DIA_1, 2, List.of(habitacionId)));
    }

    @Test
    void rechazaMasPersonasQueCapacidad() {
        assertThrows(ValidacionException.class, () ->
                datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 3, List.of(habitacionId)));
    }

    @Test
    void rechazaClienteInexistente() {
        assertThrows(ElementoNoEncontradoException.class, () ->
                datos.crearReserva(99999, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId)));
    }

    @Test
    void modificarConChoqueDejaLaReservaIntacta() throws HotelesException {
        datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));
        Reserva segunda = datos.crearReserva(clienteId, hotelId, DIA_5, DIA_10, 2, List.of(habitacionId));

        assertThrows(HabitacionNoDisponibleException.class, () ->
                datos.modificarReserva(segunda.getId(), LocalDate.of(2030, 6, 3), DIA_10, 2));

        assertEquals(DIA_5, datos.obtenerReserva(segunda.getId()).getFechaEntrada());
    }

    @Test
    void modificarReservaCambiaLasFechasEnLaBaseDeDatos() throws HotelesException {
        Reserva reserva = datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));

        datos.modificarReserva(reserva.getId(), DIA_5, DIA_10, 1);

        Reserva leida = datos.obtenerReserva(reserva.getId());
        assertEquals(DIA_5, leida.getFechaEntrada());
        assertEquals(1, leida.getNumPersonas());
    }

    // ------------------------------------------------------------------ transacciones

    @Test
    void siFallaUnaLineaNoQuedaNingunaParteDeLaReserva() throws Exception {
        int segundaHabitacion = datos.altaHabitacion(hotelId, "102", 1, CategoriaHabitacion.DOBLE,
                new BigDecimal("80.00")).getId();
        datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(segundaHabitacion));
        int antes = contar("reserva");

        // La habitación 101 está libre, pero la 102 no: la transacción entera debe deshacerse.
        assertThrows(HabitacionNoDisponibleException.class, () ->
                datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 4, List.of(habitacionId, segundaHabitacion)));

        assertEquals(antes, contar("reserva"));
        assertEquals(1, contar("linea_reserva"));
    }

    @Test
    void dosReservasSimultaneasDeLaMismaHabitacionSoloPuedeGanarUna() throws Exception {
        int hilos = 6;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (int i = 0; i < hilos; i++) {
            Callable<Boolean> tarea = () -> {
                salida.await();
                try {
                    datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));
                    return true;
                } catch (HabitacionNoDisponibleException e) {
                    return false;
                }
            };
            resultados.add(pool.submit(tarea));
        }
        salida.countDown();
        int exitos = 0;
        for (Future<Boolean> f : resultados) {
            if (f.get()) {
                exitos++;
            }
        }
        pool.shutdown();

        assertEquals(1, exitos);
        assertEquals(1, contar("reserva"));
    }

    // ------------------------------------------------------------------ clientes y seguridad

    @Test
    void elIdentificadorLoGeneraLaBaseDeDatos() throws HotelesException {
        Cliente otro = datos.altaCliente("87654321X", "Luis", "Pérez", "luis@email.com", "600", "C/ Sol");

        assertTrue(otro.getId() > clienteId);
        assertEquals("Luis", datos.obtenerCliente(otro.getId()).getNombre());
    }

    @Test
    void rechazaClienteConDniRepetido() {
        assertThrows(ElementoDuplicadoException.class, () ->
                datos.altaCliente("12345678Z", "Otra", "Persona", "otra@email.com", "600", "C/ X"));
    }

    @Test
    void noSePuedeEliminarUnClienteConReservas() throws HotelesException {
        datos.crearReserva(clienteId, hotelId, DIA_1, DIA_5, 2, List.of(habitacionId));

        assertThrows(OperacionNoPermitidaException.class, () -> datos.eliminarCliente(clienteId));
    }

    @Test
    void seEliminaUnClienteSinReservas() throws HotelesException {
        datos.eliminarCliente(clienteId);

        assertThrows(ElementoNoEncontradoException.class, () -> datos.obtenerCliente(clienteId));
    }

    @Test
    void unaInyeccionSqlEnLaBusquedaSeTrataComoTextoNormal() throws HotelesException {
        assertTrue(datos.buscarClientes("' OR '1'='1").isEmpty());
        assertTrue(datos.buscarClientes("x'; DROP TABLE cliente; --").isEmpty());
        assertEquals(1, datos.listarClientes().size());
    }

    @Test
    void unTextoMaliciosoSeGuardaLiteralYNoSeEjecuta() throws Exception {
        String malicioso = "Robert'); DROP TABLE cliente;--";
        Cliente c = datos.altaCliente("00000000T", malicioso, "Tables", "bobby@email.com", "600", "C/ X");

        assertEquals(malicioso, datos.obtenerCliente(c.getId()).getNombre());
        assertEquals(2, contar("cliente"));
    }

    @Test
    void laFactoriaRechazaUnTipoDeAlmacenDesconocido() {
        assertThrows(HotelesException.class, () -> DAOFactory.getDAOFactory("ORACLE"));
    }
}
