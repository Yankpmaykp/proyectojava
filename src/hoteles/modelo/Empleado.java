package hoteles.modelo;

/** Miembro del personal del hotel que utiliza la aplicación. */
public class Empleado extends Persona {

    private String usuario;
    private RolEmpleado rol;
    private Hotel hotel;

    public Empleado() {
    }

    public Empleado(int id, String dni, String nombre, String apellidos, String email,
                    String telefono, String usuario, RolEmpleado rol, Hotel hotel) {
        super(id, dni, nombre, apellidos, email, telefono);
        this.usuario = usuario;
        this.rol = rol;
        this.hotel = hotel;
    }

    public boolean esAdministrador() {
        return rol == RolEmpleado.ADMINISTRADOR;
    }

    @Override
    public void validar() throws ValidacionException {
        super.validar();
        if (estaVacio(usuario) || rol == null) {
            throw new ValidacionException("El usuario y el rol del empleado son obligatorios.");
        }
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public RolEmpleado getRol() {
        return rol;
    }

    public void setRol(RolEmpleado rol) {
        this.rol = rol;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    @Override
    public String toString() {
        return "Empleado{" + super.toString() + ", usuario=" + usuario + ", rol=" + rol
                + ", hotel=" + (hotel != null ? hotel.getNombre() : "-") + "}";
    }
}
