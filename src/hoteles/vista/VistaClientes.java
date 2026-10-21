package hoteles.vista;

import hoteles.controlador.Controlador;
import hoteles.modelo.HotelesException;

/** Opciones de menú relacionadas con los clientes. */
public class VistaClientes {

    private final Controlador controlador;
    private final Consola consola;

    public VistaClientes(Controlador controlador, Consola consola) {
        this.controlador = controlador;
        this.consola = consola;
    }

    public void mostrarMenu() {
        boolean volver = false;
        while (!volver) {
            int opcion = consola.menu("CLIENTES", "Listar clientes", "Buscar por nombre o apellido",
                    "Nuevo cliente", "Modificar cliente", "Eliminar cliente", "Reservas de un cliente");
            try {
                switch (opcion) {
                    case 1 -> consola.lista("Clientes", controlador.listarClientes());
                    case 2 -> consola.lista("Resultado", controlador.buscarClientes(consola.texto("Nombre o apellido")));
                    case 3 -> alta();
                    case 4 -> modificar();
                    case 5 -> eliminar();
                    case 6 -> consola.lista("Reservas del cliente",
                            controlador.reservasDeCliente(consola.entero("Id del cliente")));
                    default -> volver = true;
                }
            } catch (HotelesException e) {
                consola.error(e.getMessage());
            }
        }
    }

    private void alta() throws HotelesException {
        String dni = consola.texto("DNI");
        String nombre = consola.texto("Nombre");
        String apellidos = consola.texto("Apellidos");
        String email = consola.texto("Email");
        String telefono = consola.texto("Teléfono");
        String direccion = consola.texto("Dirección");
        consola.mensaje("Cliente creado: " + controlador.altaCliente(dni, nombre, apellidos, email, telefono, direccion));
    }

    private void modificar() throws HotelesException {
        int id = consola.entero("Id del cliente");
        consola.mensaje("Datos actuales: " + controlador.detalleCliente(id));
        String nombre = consola.textoOpcional("Nuevo nombre");
        String apellidos = consola.textoOpcional("Nuevos apellidos");
        String email = consola.textoOpcional("Nuevo email");
        String telefono = consola.textoOpcional("Nuevo teléfono");
        String direccion = consola.textoOpcional("Nueva dirección");
        consola.mensaje("Cliente modificado: " + controlador.modificarCliente(id, nombre, apellidos, email, telefono, direccion));
    }

    private void eliminar() throws HotelesException {
        int id = consola.entero("Id del cliente");
        consola.mensaje(controlador.detalleCliente(id));
        if (consola.confirmar("¿Eliminar este cliente?")) {
            controlador.eliminarCliente(id);
            consola.mensaje("Cliente eliminado.");
        }
    }
}
