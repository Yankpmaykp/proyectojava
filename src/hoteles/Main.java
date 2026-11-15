package hoteles;

import hoteles.controlador.ClienteController;
import hoteles.controlador.HotelController;
import hoteles.controlador.ReservaController;
import hoteles.modelo.Datos;
import hoteles.vista.VistaPrincipal;

/** Punto de entrada: crea el modelo, los controladores y la vista y arranca el menú. */
public class Main {

    public static void main(String[] args) {
        Datos datos = new Datos();
        datos.cargarDatosDemo();
        new VistaPrincipal(new ClienteController(datos), new HotelController(datos),
                new ReservaController(datos)).iniciar();
    }
}
