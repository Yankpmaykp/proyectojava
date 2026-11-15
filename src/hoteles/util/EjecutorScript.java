package hoteles.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Ejecuta un script SQL (como los de la carpeta {@code sql/}) por JDBC, entendiendo la directiva
 * {@code DELIMITER} del cliente {@code mysql}, necesaria para crear procedimientos almacenados.
 * Se usa desde las pruebas JUnit y desde {@link #main(String[])} para crear la BD sin el cliente mysql.
 */
public final class EjecutorScript {

    private EjecutorScript() {
    }

    /** Divide el script en sentencias individuales respetando los cambios de DELIMITER. */
    public static List<String> dividir(String script) {
        List<String> sentencias = new ArrayList<>();
        String delimitador = ";";
        StringBuilder actual = new StringBuilder();
        for (String linea : script.split("\\R")) {
            String t = quitarComentario(linea).strip();
            if (t.isEmpty()) {
                continue;
            }
            if (t.toUpperCase().startsWith("DELIMITER ")) {
                delimitador = t.substring("DELIMITER ".length()).strip();
                continue;
            }
            actual.append(t).append('\n');
            if (t.endsWith(delimitador)) {
                String sentencia = actual.toString().strip();
                sentencias.add(sentencia.substring(0, sentencia.length() - delimitador.length()).strip());
                actual.setLength(0);
            }
        }
        return sentencias;
    }

    /** Elimina un comentario "-- ..." de final de línea si no está dentro de una cadena entre comillas. */
    private static String quitarComentario(String linea) {
        boolean enCadena = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '\'') {
                enCadena = !enCadena;
            } else if (!enCadena && c == '-' && linea.startsWith("--", i)) {
                return linea.substring(0, i);
            }
        }
        return linea;
    }

    /** Ejecuta las sentencias del script que acepte el filtro. */
    public static void ejecutar(Connection con, String script, Predicate<String> filtro) throws SQLException {
        for (String sentencia : dividir(script)) {
            if (filtro.test(sentencia)) {
                try (Statement st = con.createStatement()) {
                    st.execute(sentencia);
                }
            }
        }
    }

    /** Uso: {@code java hoteles.util.EjecutorScript <url> <usuario> <password> <fichero.sql>...} */
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Uso: EjecutorScript <url> <usuario> <password> <script.sql>...");
            System.exit(1);
        }
        try (Connection con = ConexionBD.getConexion(args[0], args[1], args[2])) {
            for (int i = 3; i < args.length; i++) {
                System.out.println("Ejecutando " + args[i] + " ...");
                try {
                    ejecutar(con, Files.readString(Path.of(args[i]), StandardCharsets.UTF_8), s -> true);
                } catch (IOException | SQLException e) {
                    System.err.println("Error: " + e.getMessage());
                    System.exit(2);
                }
            }
        }
        System.out.println("Hecho.");
    }
}
