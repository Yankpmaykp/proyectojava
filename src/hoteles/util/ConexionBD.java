package hoteles.util;

import hoteles.modelo.AccesoDatosException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase utilidad que centraliza la configuración y la apertura de conexiones a la base de datos.
 * <p>
 * Los parámetros ({@code db.url}, {@code db.user}, {@code db.password}, {@code db.tipo}) se leen,
 * por orden de prioridad, de: propiedades del sistema ({@code -Ddb.url=...}), del fichero
 * {@code db.properties} del directorio de trabajo y de unos valores por defecto. Así ni el
 * usuario ni la contraseña están escritos en las clases de acceso a datos.
 * <p>
 * Cada llamada a {@link #getConexion()} devuelve una conexión nueva que el llamador debe cerrar
 * (try-with-resources); de este modo cada transacción dispone de su propia conexión.
 */
public final class ConexionBD {

    private static final String FICHERO = "db.properties";
    private static final Properties CONFIG = cargarConfiguracion();

    private ConexionBD() {
    }

    private static Properties cargarConfiguracion() {
        Properties defecto = new Properties();
        defecto.setProperty("db.tipo", "MYSQL");
        defecto.setProperty("db.url", "jdbc:mysql://localhost:3306/hoteles"
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&connectTimeout=5000");
        defecto.setProperty("db.user", "hoteles_app");
        defecto.setProperty("db.password", "hoteles_app_2026");

        Properties config = new Properties(defecto);
        Path fichero = Path.of(System.getProperty("hoteles.config", FICHERO));
        if (Files.isReadable(fichero)) {
            try (InputStream in = Files.newInputStream(fichero)) {
                config.load(in);
            } catch (IOException e) {
                System.err.println("Aviso: no se pudo leer " + fichero + ", se usan los valores por defecto.");
            }
        }
        for (String clave : defecto.stringPropertyNames()) {
            String valor = System.getProperty(clave);
            if (valor != null) {
                config.setProperty(clave, valor);
            }
        }
        return config;
    }

    /** Tipo de almacén configurado (por ahora solo {@code MYSQL}); lo usa la factoría de DAOs. */
    public static String getTipo() {
        return CONFIG.getProperty("db.tipo");
    }

    public static String getUrl() {
        return CONFIG.getProperty("db.url");
    }

    /** Abre una conexión nueva. El llamador es responsable de cerrarla. */
    public static Connection getConexion() throws AccesoDatosException {
        return getConexion(getUrl(), CONFIG.getProperty("db.user"), CONFIG.getProperty("db.password"));
    }

    /** Abre una conexión con parámetros explícitos (la usa el ejecutor de scripts con el usuario root). */
    public static Connection getConexion(String url, String usuario, String password)
            throws AccesoDatosException {
        try {
            return DriverManager.getConnection(url, usuario, password);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo conectar con la base de datos (" + e.getMessage()
                    + "). Revise que MySQL esté en marcha y la configuración de " + FICHERO + ".", e);
        }
    }
}
