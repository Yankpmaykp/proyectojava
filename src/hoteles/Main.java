package hoteles;

import hoteles.controlador.Controlador;
import hoteles.modelo.Datos;
import hoteles.vista.VistaPrincipal;

/** Punto de entrada: crea el modelo, el controlador y la vista y arranca el menú. */
public class Main {

    public static void main(String[] args) {
        Datos datos = new Datos();
        datos.cargarDatosDemo();
        new VistaPrincipal(new Controlador(datos)).iniciar();
    }
}
