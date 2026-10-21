package hoteles.vista;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Entrada y salida por consola. Es la única clase que lee del teclado, de modo que el resto
 * de la vista solo describe qué pedir y qué mostrar. Repite la pregunta hasta que el dato
 * tenga el formato esperado.
 */
public class Consola {

    private final Scanner scanner = new Scanner(System.in);

    public String texto(String pregunta) {
        System.out.print(pregunta + ": ");
        return scanner.nextLine().trim();
    }

    /** Devuelve una cadena vacía si el usuario pulsa Intro sin escribir nada. */
    public String textoOpcional(String pregunta) {
        return texto(pregunta + " (Intro para omitir)");
    }

    public int entero(String pregunta) {
        while (true) {
            String entrada = texto(pregunta);
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                error("\"" + entrada + "\" no es un número entero.");
            }
        }
    }

    /** Devuelve null si el usuario pulsa Intro sin escribir nada. */
    public Integer enteroOpcional(String pregunta) {
        while (true) {
            String entrada = textoOpcional(pregunta);
            if (entrada.isEmpty()) {
                return null;
            }
            try {
                return Integer.valueOf(entrada);
            } catch (NumberFormatException e) {
                error("\"" + entrada + "\" no es un número entero.");
            }
        }
    }

    public LocalDate fecha(String pregunta) {
        while (true) {
            String entrada = texto(pregunta + " (aaaa-mm-dd)");
            try {
                return LocalDate.parse(entrada);
            } catch (DateTimeParseException e) {
                error("\"" + entrada + "\" no es una fecha válida. Ejemplo: 2026-12-24.");
            }
        }
    }

    /** Devuelve null si el usuario pulsa Intro sin escribir nada. */
    public LocalDate fechaOpcional(String pregunta) {
        while (true) {
            String entrada = textoOpcional(pregunta + " (aaaa-mm-dd)");
            if (entrada.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(entrada);
            } catch (DateTimeParseException e) {
                error("\"" + entrada + "\" no es una fecha válida. Ejemplo: 2026-12-24.");
            }
        }
    }

    public boolean confirmar(String pregunta) {
        String respuesta = texto(pregunta + " (s/n)");
        return respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("si")
                || respuesta.equalsIgnoreCase("sí");
    }

    /** Muestra un menú numerado y devuelve la opción elegida (0 = volver/salir). */
    public int menu(String titulo, String... opciones) {
        System.out.println();
        System.out.println("=== " + titulo + " ===");
        for (int i = 0; i < opciones.length; i++) {
            System.out.println((i + 1) + ". " + opciones[i]);
        }
        System.out.println("0. Volver");
        while (true) {
            int opcion = entero("Opción");
            if (opcion >= 0 && opcion <= opciones.length) {
                return opcion;
            }
            error("Elige un número entre 0 y " + opciones.length + ".");
        }
    }

    public void lista(String titulo, List<String> lineas) {
        System.out.println("--- " + titulo + " ---");
        if (lineas.isEmpty()) {
            System.out.println("(sin resultados)");
        }
        for (String linea : lineas) {
            System.out.println(linea);
        }
    }

    public void mensaje(String texto) {
        System.out.println(texto);
    }

    public void error(String texto) {
        System.out.println("ERROR: " + texto);
    }
}
