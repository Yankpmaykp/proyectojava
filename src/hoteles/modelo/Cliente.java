package hoteles.modelo;

import java.time.LocalDate;

/** Cliente de la cadena hotelera: persona que realiza reservas. */
public class Cliente extends Persona {

    private String direccion;
    private LocalDate fechaRegistro;

    public Cliente() {
        this.fechaRegistro = LocalDate.now();
    }

    public Cliente(int id, String dni, String nombre, String apellidos, String email,
                   String telefono, String direccion) {
        super(id, dni, nombre, apellidos, email, telefono);
        this.direccion = direccion;
        this.fechaRegistro = LocalDate.now();
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return "Cliente{" + super.toString() + ", direccion=" + direccion
                + ", fechaRegistro=" + fechaRegistro + "}";
    }
}
