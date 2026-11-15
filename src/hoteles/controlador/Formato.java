package hoteles.controlador;

import hoteles.modelo.Habitacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Utilidades de formato compartidas por los controladores (paquete privado). */
final class Formato {

    private Formato() {
    }

    static String dinero(BigDecimal importe) {
        return importe.setScale(2, RoundingMode.HALF_UP).toPlainString() + " €";
    }

    static String habitacion(Habitacion h) {
        return "[" + h.getId() + "] Hab. " + h.getNumero() + " | planta " + h.getPlanta() + " | "
                + h.getCategoria() + " (" + h.getCapacidad() + " pers.) | " + dinero(h.getPrecioNoche())
                + "/noche" + (h.isEnServicio() ? "" : " | FUERA DE SERVICIO");
    }

    static List<String> habitaciones(List<Habitacion> habitaciones) {
        List<String> lineas = new ArrayList<>();
        for (Habitacion h : habitaciones) {
            lineas.add(habitacion(h));
        }
        return lineas;
    }
}
