package hoteles.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Reserva de un cliente en un hotel para un intervalo de fechas.
 * Se compone de una o varias líneas (una por habitación reservada).
 */
public class Reserva implements Identificable, Validable {

    private int id;
    private Cliente cliente;
    private Hotel hotel;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private int numPersonas;
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private List<LineaReserva> lineas = new ArrayList<>();

    public Reserva() {
    }

    public Reserva(int id, Cliente cliente, Hotel hotel, LocalDate fechaEntrada,
                   LocalDate fechaSalida, int numPersonas) {
        this.id = id;
        this.cliente = cliente;
        this.hotel = hotel;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.numPersonas = numPersonas;
    }

    public void addHabitacion(Habitacion habitacion) {
        lineas.add(new LineaReserva(habitacion));
    }

    public long getNumNoches() {
        if (fechaEntrada == null || fechaSalida == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }

    public int getCapacidadTotal() {
        int capacidad = 0;
        for (LineaReserva linea : lineas) {
            capacidad += linea.getHabitacion().getCapacidad();
        }
        return capacidad;
    }

    /** Importe = suma, para cada habitación, de (precio por noche × número de noches). */
    public BigDecimal calcularImporte() {
        BigDecimal total = BigDecimal.ZERO;
        long noches = getNumNoches();
        for (LineaReserva linea : lineas) {
            total = total.add(linea.calcularImporte(noches));
        }
        return total;
    }

    /**
     * Indica si esta reserva ocupa el intervalo [entrada, salida). La fecha de salida
     * no cuenta como noche ocupada, por lo que una reserva puede empezar el mismo día
     * en que termina otra. Las reservas canceladas no ocupan habitaciones.
     */
    public boolean seSolapaCon(LocalDate entrada, LocalDate salida) {
        return estado != EstadoReserva.CANCELADA
                && fechaEntrada.isBefore(salida)
                && entrada.isBefore(fechaSalida);
    }

    public boolean incluyeHabitacion(Habitacion habitacion) {
        for (LineaReserva linea : lineas) {
            if (linea.getHabitacion().getId() == habitacion.getId()) {
                return true;
            }
        }
        return false;
    }

    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }

    /**
     * Validaciones propias de la reserva. La comprobación de disponibilidad frente a
     * otras reservas la hará el controlador (Fase 2) o la base de datos (Fase 3),
     * apoyándose en {@link #seSolapaCon(LocalDate, LocalDate)}.
     */
    @Override
    public void validar() throws ValidacionException {
        if (cliente == null) {
            throw new ValidacionException("Debe seleccionar un cliente existente.");
        }
        if (hotel == null) {
            throw new ValidacionException("Debe seleccionar un hotel existente.");
        }
        if (fechaEntrada == null || fechaSalida == null) {
            throw new ValidacionException("Las fechas de entrada y salida son obligatorias.");
        }
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new ValidacionException("La fecha de salida debe ser posterior a la de entrada.");
        }
        if (lineas.isEmpty()) {
            throw new ValidacionException("La reserva debe incluir al menos una habitación.");
        }
        if (numPersonas <= 0 || numPersonas > getCapacidadTotal()) {
            throw new ValidacionException("El número de personas debe estar entre 1 y "
                    + getCapacidadTotal() + " para las habitaciones seleccionadas.");
        }
        List<Integer> vistas = new ArrayList<>();
        for (LineaReserva linea : lineas) {
            Habitacion h = linea.getHabitacion();
            if (h.getHotel() == null || h.getHotel().getId() != hotel.getId()) {
                throw new ValidacionException("La habitación " + h.getNumero() + " no pertenece al hotel seleccionado.");
            }
            if (vistas.contains(h.getId())) {
                throw new ValidacionException("La habitación " + h.getNumero() + " está repetida en la reserva.");
            }
            vistas.add(h.getId());
        }
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDate fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public int getNumPersonas() {
        return numPersonas;
    }

    public void setNumPersonas(int numPersonas) {
        this.numPersonas = numPersonas;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<LineaReserva> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaReserva> lineas) {
        this.lineas = lineas;
    }

    @Override
    public String toString() {
        return "Reserva{id=" + id
                + ", cliente=" + (cliente != null ? cliente.getNombreCompleto() : "-")
                + ", hotel=" + (hotel != null ? hotel.getNombre() : "-")
                + ", entrada=" + fechaEntrada + ", salida=" + fechaSalida
                + ", noches=" + getNumNoches() + ", personas=" + numPersonas
                + ", estado=" + estado + ", lineas=" + lineas
                + ", importe=" + calcularImporte() + " €}";
    }
}
