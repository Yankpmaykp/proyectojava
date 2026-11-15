package hoteles.vista;

import hoteles.controlador.ClienteController;
import hoteles.controlador.HotelController;
import hoteles.controlador.ReservaController;
import hoteles.modelo.HotelesException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Opciones de menú relacionadas con las reservas. */
public class VistaReservas {

    private final ReservaController reservas;
    private final ClienteController clientes;
    private final HotelController hoteles;
    private final Consola consola;

    public VistaReservas(ReservaController reservas, ClienteController clientes, HotelController hoteles,
                         Consola consola) {
        this.reservas = reservas;
        this.clientes = clientes;
        this.hoteles = hoteles;
        this.consola = consola;
    }

    public void mostrarMenu() {
        boolean volver = false;
        while (!volver) {
            int opcion = consola.menu("RESERVAS", "Nueva reserva", "Consultar reservas", "Ver detalle de una reserva",
                    "Modificar reserva", "Cancelar reserva", "Finalizar reserva");
            try {
                switch (opcion) {
                    case 1 -> nueva();
                    case 2 -> consultar();
                    case 3 -> consola.mensaje(reservas.detalleReserva(consola.entero("Id de la reserva")));
                    case 4 -> modificar();
                    case 5 -> cancelar();
                    case 6 -> finalizar();
                    default -> volver = true;
                }
            } catch (HotelesException e) {
                consola.error(e.getMessage());
            }
        }
    }

    /** Flujo: cliente, hotel, fechas, habitaciones disponibles, nº de personas y creación. */
    private void nueva() throws HotelesException {
        consola.lista("Clientes", clientes.listarClientes());
        int cliente = consola.entero("Id del cliente");
        consola.lista("Hoteles", hoteles.listarHoteles());
        int hotel = consola.entero("Id del hotel");
        LocalDate entrada = consola.fecha("Fecha de entrada");
        LocalDate salida = consola.fecha("Fecha de salida");

        List<String> libres = hoteles.habitacionesDisponibles(hotel, entrada, salida, 0);
        consola.lista("Habitaciones disponibles", libres);
        if (libres.isEmpty()) {
            return;
        }
        int personas = consola.entero("Nº de personas");
        List<Integer> ids = pedirIds();
        consola.mensaje("Reserva creada:" + System.lineSeparator()
                + reservas.crearReserva(cliente, hotel, entrada, salida, personas, ids));
    }

    /** Pide los ids de habitación separados por comas, repitiendo hasta que todos sean números. */
    private List<Integer> pedirIds() {
        while (true) {
            String texto = consola.texto("Ids de las habitaciones a reservar (separados por comas)");
            List<Integer> ids = new ArrayList<>();
            try {
                for (String parte : texto.split(",")) {
                    if (!parte.isBlank()) {
                        ids.add(Integer.parseInt(parte.trim()));
                    }
                }
                return ids;
            } catch (NumberFormatException e) {
                consola.error("Escribe solo números separados por comas. Ejemplo: 2,3");
            }
        }
    }

    private void consultar() throws HotelesException {
        consola.mensaje("Deja en blanco los criterios que no quieras aplicar.");
        Integer cliente = consola.enteroOpcional("Id del cliente");
        Integer hotel = consola.enteroOpcional("Id del hotel");
        LocalDate entrada = consola.fechaOpcional("Entrada desde");
        LocalDate salida = consola.fechaOpcional("Salida hasta");
        String estado = consola.textoOpcional("Estado " + reservas.estadosReserva());
        consola.lista("Reservas (ordenadas por fecha de entrada)",
                reservas.buscarReservas(cliente, hotel, entrada, salida, estado));
    }

    private void modificar() throws HotelesException {
        int id = consola.entero("Id de la reserva");
        consola.mensaje(reservas.detalleReserva(id));
        LocalDate entrada = consola.fecha("Nueva fecha de entrada");
        LocalDate salida = consola.fecha("Nueva fecha de salida");
        int personas = consola.entero("Nuevo nº de personas");
        consola.mensaje("Reserva modificada:" + System.lineSeparator()
                + reservas.modificarReserva(id, entrada, salida, personas));
    }

    private void cancelar() throws HotelesException {
        int id = consola.entero("Id de la reserva");
        consola.mensaje(reservas.detalleReserva(id));
        if (consola.confirmar("¿Cancelar esta reserva?")) {
            consola.mensaje("Reserva cancelada:" + System.lineSeparator() + reservas.cancelarReserva(id));
        }
    }

    private void finalizar() throws HotelesException {
        int id = consola.entero("Id de la reserva");
        consola.mensaje("Reserva finalizada:" + System.lineSeparator() + reservas.finalizarReserva(id));
    }
}
