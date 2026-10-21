package hoteles.modelo;

import java.util.regex.Pattern;

/**
 * Superclase abstracta con los datos comunes a cualquier persona del sistema
 * (clientes y empleados). No tiene sentido instanciar una "persona" genérica.
 */
public abstract class Persona implements Identificable, Validable {

    private static final Pattern PATRON_EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[A-Za-z]{2,}$");

    protected int id;
    protected String dni;
    protected String nombre;
    protected String apellidos;
    protected String email;
    protected String telefono;

    protected Persona() {
    }

    protected Persona(int id, String dni, String nombre, String apellidos, String email, String telefono) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public static boolean esEmailValido(String email) {
        return email != null && PATRON_EMAIL.matcher(email).matches();
    }

    protected static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    @Override
    public void validar() throws ValidacionException {
        if (estaVacio(dni) || estaVacio(nombre) || estaVacio(apellidos) || estaVacio(email)) {
            throw new ValidacionException("El DNI, el nombre, los apellidos y el email son obligatorios.");
        }
        if (!esEmailValido(email)) {
            throw new ValidacionException("El email '" + email + "' no tiene un formato válido.");
        }
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return "id=" + id + ", dni=" + dni + ", nombre=" + getNombreCompleto()
                + ", email=" + email + ", telefono=" + telefono;
    }
}
