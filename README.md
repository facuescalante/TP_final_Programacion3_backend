# Food Store JPA — TPI (Parte 2)

Aplicación Backend desarrollada en **Java 21**, **JPA / Hibernate 6** y base de datos embebida **H2** gestionada mediante **Gradle**, que implementa la capa de persistencia relacional y un menú interactivo por consola para la gestión integral de categorías, productos, usuarios, pedidos y reportes de **Food Store**.

---

## Tecnologías

- Java 21
- JPA / Hibernate 6
- H2 (base de datos en archivo — `./data/jpa_db`)
- Lombok
- Gradle 8

---

## Estructura del proyecto

```text
src/main/java/com/tp/jpa/
│
├── model/                        # Entidades JPA y modelo de dominio
│   ├── Base.java                 # Clase abstracta base (id, eliminado, createdAt)
│   ├── Calculable.java           # Interfaz con calcularTotal()
│   ├── Categoria.java            # Entidad dueña de relación unidireccional con Producto
│   ├── Producto.java
│   ├── Usuario.java              # Entidad dueña de relación unidireccional con Pedido
│   ├── Pedido.java               # Implementa Calculable y gestiona DetallePedido
│   ├── DetallePedido.java        # Subtotales por ítem de pedido
│   └── enums/
│       ├── Rol.java
│       ├── Estado.java
│       └── FormaPago.java
│
├── util/
│   └── JPAUtil.java              # Factory singleton de EntityManagerFactory
│
├── repository/                   # Repositorios con queries JPQL y baja lógica
│   ├── BaseRepository.java       # CRUD genérico (guardar, buscarPorId, listarActivos, eliminarLogico)
│   ├── ProductoRepository.java   # Operaciones heredadas de BaseRepository
│   ├── CategoriaRepository.java  # Consulta JPQL buscarProductosPorCategoria()
│   ├── UsuarioRepository.java    # Consultas JPQL buscarPorMail() y buscarPedidosPorUsuario()
│   └── PedidoRepository.java     # Consulta JPQL buscarPorEstado()
│
└── Main.java                     # Menús de consola y flujo completo de operaciones
```

---

## Qué está implementado

| Componente | Estado |
|---|---|
| `JPAUtil` | ✅ Completo |
| `BaseRepository` (guardar, buscarPorId, listarActivos, eliminarLogico) | ✅ Completo |
| `ProductoRepository` | ✅ Completo (hereda CRUD de BaseRepository) |
| Modelo completo (todas las entidades y enums) | ✅ Completo |
| `CategoriaRepository`, `UsuarioRepository`, `PedidoRepository` | ✅ Completo |
| `Main` — menús interactivos completos | ✅ Completo |

---

## Detalle de implementación

### Repositorios (`repository/`)

| Clase | Método | Descripción |
|---|---|---|
| `CategoriaRepository` | `buscarProductosPorCategoria(Long categoriaId)` | Consulta JPQL navegando `c.productos` (relación unidireccional donde `Categoria` es la dueña), filtrando por `eliminado = false`. |
| `UsuarioRepository` | `buscarPorMail(String mail)` | Consulta JPQL filtrando por mail y `eliminado = false`, retorna `Optional<Usuario>`. |
| `UsuarioRepository` | `buscarPedidosPorUsuario(Long idUsuario)` | Consulta JPQL navegando `u.pedidos` (relación unidireccional donde `Usuario` es la dueña), filtrando por `eliminado = false`. |
| `PedidoRepository` | `buscarPorEstado(Estado estado)` | Consulta JPQL filtrando por estado y `eliminado = false`. |

### Menú de consola (`Main.java`)

| Método | Descripción |
|---|---|
| `menuCategorias()` | Alta, modificar, baja lógica, listado. |
| `menuProductos()` | Alta (con selección de categoría), modificar, baja lógica, listado. |
| `menuUsuarios()` | Alta (mail único), modificar, baja lógica, listado, buscar por mail. |
| `menuPedidos()` | Alta (transacción atómica con `DetallePedido` y cálculo de totales), cambiar estado, baja lógica, listados. |
| `menuReportes()` | Productos por categoría, pedidos por usuario/estado, total facturado. |

---

## Cómo ejecutar

```bash
./gradlew run
```

O compilar y ejecutar el JAR:

```bash
./gradlew jar
java -jar build/libs/foodstore-jpa-0.0.1-SNAPSHOT.jar
```

La base de datos H2 se crea automáticamente en `./data/jpa_db.mv.db` al primer arranque.

---

## Credenciales / datos de prueba

No hay carga inicial automática. Crear los datos desde el menú de consola en este orden:

1. Categorías
2. Productos (requieren categoría existente)
3. Usuarios
4. Pedidos (requieren usuario y productos existentes)

---

## Entrega

- **Video demostrativo:** https://canva.link/o82z8oc7xp1nt4w
