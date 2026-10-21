# Producto 2 – Gestión de una cadena de hoteles (Java, MVC, consola)

Aplicación de consola con patrón **Modelo-Vista-Controlador**. Los datos se guardan en colecciones en memoria
(se cargan unos datos de ejemplo al arrancar).

## Uso (Windows, JDK 17+)

```
compilar.bat     compila src\ y test\ en out\
ejecutar.bat     arranca la aplicación
probar.bat       ejecuta las pruebas JUnit 5
```

## Estructura

| Paquete | Contenido |
|---|---|
| `hoteles.vista` | `VistaPrincipal`, `VistaClientes`, `VistaHoteles`, `VistaReservas`, `Consola` (única clase que lee del teclado). |
| `hoteles.controlador` | `Controlador`: único puente vista → modelo. Recibe tipos básicos/cadenas y devuelve texto ya formateado, de modo que la vista no maneja entidades del modelo. |
| `hoteles.modelo` | Entidades del Producto 1 (`Persona`, `Cliente`, `Empleado`, `Hotel`, `Habitacion`, `Reserva`, `LineaReserva`, enums, interfaces `Identificable` y `Validable`), la clase `Datos` (todos los datos y la lógica que los modifica), el genérico `Repositorio<T>` y las excepciones. |

> Única referencia al modelo desde la vista: `HotelesException` (se captura para mostrar el mensaje al usuario).

## Colecciones y genéricos

- `Repositorio<T extends Identificable>` (clase genérica): `TreeMap<Integer, T>`; búsqueda por id en O(log n) y recorrido ordenado por id.
- `Hotel.habitaciones`, `Reserva.lineas`: `ArrayList` (orden de inserción, recorridos secuenciales).
- `Set<Integer>` (`HashSet`) para las habitaciones ocupadas en un intervalo: sin duplicados y `contains` en O(1).
- `EnumMap<CategoriaHabitacion, Integer>` para el recuento por categoría.
- Resultados de búsqueda de reservas ordenados con `Comparator` (fecha de entrada, luego id).

## Excepciones personalizadas

`HotelesException` (base, comprobada) → `ValidacionException`, `ElementoNoEncontradoException`,
`ElementoDuplicadoException`, `HabitacionNoDisponibleException`, `OperacionNoPermitidaException`.

## Pruebas unitarias (JUnit 5)

- `ReservaTest` → `Reserva.calcularImporte()`.
- `DatosTest` → `Datos.crearReserva(...)` (solapamientos, fechas, capacidad, canceladas) y `modificarReserva`.
