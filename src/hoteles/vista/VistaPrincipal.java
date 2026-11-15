package hoteles.vista;

import hoteles.controlador.ClienteController;
import hoteles.controlador.HotelController;
import hoteles.controlador.ReservaController;

import java.util.NoSuchElementException;

/** Menú principal: reparte el control entre las vistas de cada área. */
public class VistaPrincipal {

    private final Consola consola = new Consola();
    private final VistaClientes clientes;
    private final VistaHoteles hoteles;
    private final VistaReservas reservas;

    public VistaPrincipal(ClienteController clienteController, HotelController hotelController,
                          ReservaController reservaController) {
        this.clientes = new VistaClientes(clienteController, reservaController, consola);
        this.hoteles = new VistaHoteles(hotelController, consola);
        this.reservas = new VistaReservas(reservaController, clienteController, hotelController, consola);
    }

    public void iniciar() {
        consola.mensaje("Sistema de gestión de una cadena de hoteles");
        try {
            boolean salir = false;
            while (!salir) {
                switch (consola.menu("MENÚ PRINCIPAL", "Clientes", "Hoteles y habitaciones", "Reservas")) {
                    case 1 -> clientes.mostrarMenu();
                    case 2 -> hoteles.mostrarMenu();
                    case 3 -> reservas.mostrarMenu();
                    default -> salir = true;
                }
            }
        } catch (NoSuchElementException e) {
            consola.mensaje("");   // fin de la entrada (Ctrl+Z): se sale sin error
        }
        consola.mensaje("Hasta pronto.");
    }
}
