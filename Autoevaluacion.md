# Autoevaluación – Producto 2 (FP447)

**Nota propuesta: 20 / 20** (la nota máxima del producto es 20, no 100).

| Criterio | Nivel elegido | Puntos | Justificación |
|---|---|---|---|
| Control de versiones con Git | Correcto y optimizado | 1 / 1 | Repositorio con historial de commits pequeños y descriptivos, uno por cada bloque del trabajo (modelo, excepciones, repositorio genérico, `Datos`, controlador, vistas, pruebas, scripts), con `.gitignore` para `out/`. |
| Generación de código a partir de un diagrama de clases | Correcto y optimizado | 2 / 2 | Las clases, atributos, relaciones (herencia `Persona` → `Cliente`/`Empleado`, composición `Hotel`–`Habitacion` y `Reserva`–`LineaReserva`), enums e interfaces `Identificable`/`Validable` proceden del diagrama del Producto 1. Se mantiene el modelo y solo se añaden las clases necesarias (`Datos`, `Repositorio`, excepciones). |
| Colecciones en Java | Óptimas y optimizadas | 4 / 4 | `Repositorio<T>` genérico sobre `TreeMap` (búsqueda por id y recorrido ordenado); `ArrayList` para habitaciones y líneas; `HashSet` para ocupación sin duplicados; `EnumMap` para el recuento por categoría; `Comparator` para ordenar reservas. Cada elección se justifica en el README. |
| Patrón MVC | Correcto y optimizado | 4 / 4 | Tres paquetes. Solo `Consola` usa el teclado; la vista solo habla con `Controlador`, que recibe primitivos/cadenas y devuelve texto, por lo que la vista no maneja entidades. `Datos` concentra los datos y la lógica. Excepciones personalizadas propias de negocio. |
| Pruebas unitarias con JUnit | Correcto y optimizado | 1 / 1 | 12 pruebas JUnit 5 sobre dos métodos de lógica de negocio: `Reserva.calcularImporte()` y `Datos.crearReserva()` (más `modificarReserva`), incluyendo casos límite (mismo día de salida/entrada, reserva cancelada, rollback al modificar). Todas pasan. |
| **Total** | | **12 / 12 en rúbrica → 20 / 20** | |

> Nota: la rúbrica suma 12 puntos de criterios; se escala a la nota sobre 20 del producto.

## Dificultades y mejoras
- Se movió la comprobación de solapamientos del controlador a `Datos`, como exige el enunciado.
- Mejoras futuras: persistencia en base de datos (Fase 3), reservas con modificación de habitaciones, y control de acceso por rol de empleado.

## Pendiente de completar antes de entregar
- Enlace al vídeo.
- Enlace al repositorio de GitHub.
- Nombre del autor.
