package hoteles.vista;

import hoteles.controlador.Controlador;
import hoteles.modelo.HotelesException;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Opciones de menú relacionadas con hoteles y habitaciones. */
public class VistaHoteles {

    private final Controlador controlador;
    private final Consola consola;

    public VistaHoteles(Controlador controlador, Consola consola) {
        this.controlador = controlador;
        this.consola = consola;
    }

    public void mostrarMenu() {
        boolean volver = false;
        while (!volver) {
            int opcion = consola.menu("HOTELES Y HABITACIONES", "Mostrar hoteles", "Buscar hoteles por ciudad",
                    "Habitaciones de un hotel", "Habitaciones disponibles en unas fechas",
                    "Nº de habitaciones disponibles en unas fechas", "Habitaciones por categoría",
                    "Nuevo hotel", "Nueva habitación");
            try {
                switch (opcion) {
                    case 1 -> consola.lista("Hoteles", controlador.listarHoteles());
                    case 2 -> consola.lista("Resultado", controlador.buscarHotelesPorCiudad(consola.texto("Ciudad")));
                    case 3 -> consola.lista("Habitaciones", controlador.habitacionesDeHotel(pedirHotel()));
                    case 4 -> disponibles();
                    case 5 -> contarDisponibles();
                    case 6 -> porCategoria();
                    case 7 -> altaHotel();
                    case 8 -> altaHabitacion();
                    default -> volver = true;
                }
            } catch (HotelesException e) {
                consola.error(e.getMessage());
            }
        }
    }

    private int pedirHotel() {
        consola.lista("Hoteles", controlador.listarHoteles());
        return consola.entero("Id del hotel");
    }

    private void disponibles() throws HotelesException {
        int hotel = pedirHotel();
        LocalDate entrada = consola.fecha("Fecha de entrada");
        LocalDate salida = consola.fecha("Fecha de salida");
        Integer personas = consola.enteroOpcional("Nº de personas");
        consola.lista("Habitaciones disponibles",
                controlador.habitacionesDisponibles(hotel, entrada, salida, personas == null ? 0 : personas));
    }

    private void contarDisponibles() throws HotelesException {
        int hotel = pedirHotel();
        LocalDate entrada = consola.fecha("Fecha de entrada");
        LocalDate salida = consola.fecha("Fecha de salida");
        consola.mensaje("Habitaciones disponibles: " + controlador.contarHabitacionesDisponibles(hotel, entrada, salida));
    }

    private void porCategoria() throws HotelesException {
        int hotel = pedirHotel();
        consola.lista("Recuento por categoría", controlador.resumenCategorias(hotel));
        String categoria = consola.texto("Categoría " + controlador.categorias());
        consola.lista("Habitaciones " + categoria.toUpperCase(), controlador.habitacionesPorCategoria(hotel, categoria));
    }

    private void altaHotel() throws HotelesException {
        String nombre = consola.texto("Nombre");
        String direccion = consola.texto("Dirección");
        String ciudad = consola.texto("Ciudad");
        String telefono = consola.texto("Teléfono");
        String email = consola.texto("Email");
        int estrellas = consola.entero("Estrellas (1-5)");
        consola.mensaje("Hotel creado: " + controlador.altaHotel(nombre, direccion, ciudad, telefono, email, estrellas));
    }

    private void altaHabitacion() throws HotelesException {
        int hotel = pedirHotel();
        String numero = consola.texto("Número de habitación");
        int planta = consola.entero("Planta");
        String categoria = consola.texto("Categoría " + controlador.categorias());
        BigDecimal precio = leerPrecio();
        consola.mensaje("Habitación creada: " + controlador.altaHabitacion(hotel, numero, planta, categoria, precio));
    }

    private BigDecimal leerPrecio() {
        while (true) {
            String texto = consola.texto("Precio por noche");
            try {
                return new BigDecimal(texto.replace(',', '.'));
            } catch (NumberFormatException e) {
                consola.error("\"" + texto + "\" no es un importe válido.");
            }
        }
    }
}
