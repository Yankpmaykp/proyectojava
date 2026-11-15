package hoteles.controlador;

import hoteles.modelo.CategoriaHabitacion;
import hoteles.modelo.Datos;
import hoteles.modelo.Hotel;
import hoteles.modelo.HotelesException;
import hoteles.modelo.ValidacionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Controlador de hoteles y habitaciones (la habitación pertenece al hotel). */
public class HotelController {

    private final Datos datos;

    public HotelController(Datos datos) {
        this.datos = datos;
    }

    public String altaHotel(String nombre, String direccion, String ciudad, String telefono,
                            String email, int estrellas) throws HotelesException {
        return formatoHotel(datos.altaHotel(nombre, direccion, ciudad, telefono, email, estrellas));
    }

    public String altaHabitacion(int hotelId, String numero, int planta, String categoria,
                                 BigDecimal precioNoche) throws HotelesException {
        return Formato.habitacion(datos.altaHabitacion(hotelId, numero, planta,
                parseCategoria(categoria), precioNoche));
    }

    public List<String> listarHoteles() {
        return formatoHoteles(datos.listarHoteles());
    }

    public List<String> buscarHotelesPorCiudad(String ciudad) {
        return formatoHoteles(datos.buscarHotelesPorCiudad(ciudad));
    }

    public List<String> habitacionesDeHotel(int hotelId) throws HotelesException {
        return Formato.habitaciones(datos.habitacionesDeHotel(hotelId));
    }

    public List<String> habitacionesPorCategoria(int hotelId, String categoria) throws HotelesException {
        return Formato.habitaciones(datos.habitacionesPorCategoria(hotelId, parseCategoria(categoria)));
    }

    public List<String> resumenCategorias(int hotelId) throws HotelesException {
        List<String> lineas = new ArrayList<>();
        for (Map.Entry<CategoriaHabitacion, Integer> e : datos.contarHabitacionesPorCategoria(hotelId).entrySet()) {
            lineas.add(e.getKey() + " (" + e.getKey().getCapacidadMaxima() + " pers.): " + e.getValue());
        }
        return lineas;
    }

    /** Habitaciones libres en las fechas indicadas; {@code numPersonas} 0 equivale a no filtrar. */
    public List<String> habitacionesDisponibles(int hotelId, LocalDate entrada, LocalDate salida,
                                                int numPersonas) throws HotelesException {
        return Formato.habitaciones(datos.habitacionesDisponibles(hotelId, entrada, salida, numPersonas));
    }

    public int contarHabitacionesDisponibles(int hotelId, LocalDate entrada, LocalDate salida)
            throws HotelesException {
        return datos.contarHabitacionesDisponibles(hotelId, entrada, salida);
    }

    public List<String> categorias() {
        List<String> nombres = new ArrayList<>();
        for (CategoriaHabitacion c : CategoriaHabitacion.values()) {
            nombres.add(c.name());
        }
        return nombres;
    }

    private static CategoriaHabitacion parseCategoria(String texto) throws ValidacionException {
        try {
            return CategoriaHabitacion.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ValidacionException("La categoría '" + texto + "' no existe. Valores válidos: "
                    + List.of(CategoriaHabitacion.values()) + ".");
        }
    }

    private static String formatoHotel(Hotel h) {
        return "[" + h.getId() + "] " + h.getNombre() + " (" + h.getEstrellas() + " estrellas) | "
                + h.getCiudad() + " | " + h.getDireccion() + " | " + h.getNumHabitaciones() + " habitaciones";
    }

    private static List<String> formatoHoteles(List<Hotel> hoteles) {
        List<String> lineas = new ArrayList<>();
        for (Hotel h : hoteles) {
            lineas.add(formatoHotel(h));
        }
        return lineas;
    }
}
