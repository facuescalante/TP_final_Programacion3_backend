package com.tp.jpa;

import com.tp.jpa.model.*;
import com.tp.jpa.model.enums.EstadoPedido;
import com.tp.jpa.model.enums.FormaPago;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.PedidoRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.repository.UsuarioRepository;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

/**
 * Clase principal: menú de consola del sistema Food Store.
 * Orden de uso natural: Categorías -> Productos -> Usuarios -> Pedidos.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final CategoriaRepository categoriaRepo = new CategoriaRepository();
    private static final ProductoRepository productoRepo = new ProductoRepository();
    private static final UsuarioRepository usuarioRepo = new UsuarioRepository();
    private static final PedidoRepository pedidoRepo = new PedidoRepository();

    public static void main(String[] args) {
        boolean salir = false;
        while (!salir) {
            System.out.println();
            System.out.println("===== FOOD STORE - MENU PRINCIPAL =====");
            System.out.println("1. Gestionar Categorias");
            System.out.println("2. Gestionar Productos");
            System.out.println("3. Gestionar Usuarios");
            System.out.println("4. Gestionar Pedidos");
            System.out.println("5. Reportes");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            String op = sc.nextLine().trim();
            switch (op) {
                case "1": menuCategorias(); break;
                case "2": menuProductos(); break;
                case "3": menuUsuarios(); break;
                case "4": menuPedidos(); break;
                case "5": menuReportes(); break;
                case "0": salir = true; break;
                default: System.out.println("Opcion invalida.");
            }
        }
        JPAUtil.close();
        System.out.println("Aplicacion finalizada.");
    }

    // ── Submenú Categorías ───────────────────────────────────────

    private static void menuCategorias() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTION DE CATEGORIAS ---");
            System.out.println("1. Alta");
            System.out.println("2. Modificar");
            System.out.println("3. Baja logica");
            System.out.println("4. Listado");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");
            String op = sc.nextLine().trim();
            switch (op) {
                case "1":
                    System.out.print("Ingrese nombre de la categoria: ");
                    String nombre = sc.nextLine().trim();
                    if (nombre.isEmpty()) {
                        System.out.println("Error: El nombre no puede estar vacio.");
                        break;
                    }
                    System.out.print("Ingrese descripcion (opcional): ");
                    String desc = sc.nextLine().trim();
                    Categoria cat = Categoria.builder()
                            .nombre(nombre)
                            .descripcion(desc.isEmpty() ? null : desc)
                            .build();
                    Categoria guardada = categoriaRepo.guardar(cat);
                    System.out.println("Categoria creada con exito! ID generado: " + guardada.getId() );
                    break;

                case "2":
                    listarCategoriasActivas();
                    System.out.print("Ingrese ID de la categoria a modificar: ");
                    try {
                        Long idMod = Long.parseLong(sc.nextLine().trim());
                        Optional<Categoria> optCat = categoriaRepo.buscarPorId(idMod);
                        if (optCat.isEmpty() || optCat.get().isEliminado()) {
                            System.out.println("Error: Categoria no encontrada o dada de baja.");
                            break;
                        }
                        Categoria c = optCat.get();
                        System.out.println("Valores actuales -> Nombre: " + c.getNombre() + " | Descripcion: " + c.getDescripcion());

                        System.out.print("Nuevo nombre (dejar en blanco para conservar): ");
                        String nuevoNombre = sc.nextLine().trim();
                        if (!nuevoNombre.isEmpty()) {
                            c.setNombre(nuevoNombre);
                        }

                        System.out.print("Nueva descripcion (dejar en blanco para conservar): ");
                        String nuevaDesc = sc.nextLine().trim();
                        if (!nuevaDesc.isEmpty()) {
                            c.setDescripcion(nuevaDesc);
                        }

                        categoriaRepo.guardar(c);
                        System.out.println("Categoria modificada con exito!");
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalido.");
                    }
                    break;

                case "3":
                    listarCategoriasActivas();
                    System.out.print("Ingrese ID de la categoria a dar de baja: ");
                    try {
                        Long idBaja = Long.parseLong(sc.nextLine().trim());
                        Optional<Categoria> optBaja = categoriaRepo.buscarPorId(idBaja);
                        if (optBaja.isEmpty() || optBaja.get().isEliminado()) {
                            System.out.println("Error: Categoria no encontrada o ya dada de baja.");
                            break;
                        }
                        List<Producto> prodsActivos = categoriaRepo.buscarProductosPorCategoria(idBaja);
                        if (!prodsActivos.isEmpty()) {
                            System.out.println("Error: No se puede dar de baja. La categoria tiene " + prodsActivos.size() + " producto(s) activo(s).");
                            break;
                        }
                        boolean exito = categoriaRepo.eliminarLogico(idBaja);
                        if (exito) {
                            System.out.println(" Categoria '" + optBaja.get().getNombre() + "' dada de baja correctamente!");
                        } else {
                            System.out.println("Error al intentar dar de baja.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalido.");
                    }
                    break;

                case "4":
                    listarCategoriasActivas();
                    break;

                case "0":
                    volver = true;
                    break;

                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }

    private static void listarCategoriasActivas() {
        List<Categoria> lista = categoriaRepo.listarActivos();
        System.out.println("\n--- LISTADO DE CATEGORIAS ACTIVAS ---");
        if (lista.isEmpty()) {
            System.out.println("No hay categorias registradas.");
        } else {
            for (Categoria c : lista) {
                System.out.println("ID: " + c.getId() + " | Nombre: " + c.getNombre() + " | Desc: " + (c.getDescripcion() != null ? c.getDescripcion() : "-"));
            }
        }
    }

    // ── Submenú Productos ────────────────────────────────────────

    private static void menuProductos() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTION DE PRODUCTOS ---");
            System.out.println("1. Alta");
            System.out.println("2. Modificar");
            System.out.println("3. Baja logica");
            System.out.println("4. Listado completo");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");
            String op = sc.nextLine().trim();
            switch (op) {
                case "1":
                    List<Categoria> cats = categoriaRepo.listarActivos();
                    if (cats.isEmpty()) {
                        System.out.println("Error: No hay categorias activas. Cree una categoria antes de dar de alta productos.");
                        break;
                    }
                    listarCategoriasActivas();
                    System.out.print("Seleccione el ID de la categoria para el producto: ");
                    try {
                        Long catId = Long.parseLong(sc.nextLine().trim());
                        Optional<Categoria> optC = categoriaRepo.buscarPorId(catId);
                        if (optC.isEmpty() || optC.get().isEliminado()) {
                            System.out.println("Error: Categoria inexistente o dada de baja.");
                            break;
                        }

                        System.out.print("Nombre del producto: ");
                        String nombre = sc.nextLine().trim();
                        if (nombre.isEmpty()) {
                            System.out.println("Error: El nombre es obligatorio.");
                            break;
                        }

                        System.out.print("Descripcion: ");
                        String desc = sc.nextLine().trim();

                        System.out.print("Precio (ej: 1500.50): ");
                        Double precio = Double.parseDouble(sc.nextLine().trim());
                        if (precio <= 0) {
                            System.out.println("Error: El precio debe ser mayor a 0.");
                            break;
                        }

                        System.out.print("Stock (entero >= 0): ");
                        int stock = Integer.parseInt(sc.nextLine().trim());
                        if (stock < 0) {
                            System.out.println("Error: El stock no puede ser negativo.");
                            break;
                        }

                        System.out.print("Imagen (URL o ruta, opcional): ");
                        String imagen = sc.nextLine().trim();

                        System.out.print("Esta disponible? (S/N, default S): ");
                        String dispStr = sc.nextLine().trim().toUpperCase();
                        boolean disponible = !dispStr.equals("N");

                        Producto p = Producto.builder()
                                .nombre(nombre)
                                .descripcion(desc)
                                .precio(precio)
                                .stock(stock)
                                .imagen(imagen)
                                .disponible(disponible)
                                .build();

                        categoriaRepo.agregarProducto(catId, p);
                        System.out.println("Producto agregado con exito! ID asignado: " + p.getId() + " en categoria: " + optC.get().getNombre() );
                    } catch (NumberFormatException e) {
                        System.out.println("Error en el formato numerico ingresado.");
                    }
                    break;

                case "2":
                    listarProductosActivosConsola();
                    System.out.print("Ingrese ID del producto a modificar: ");
                    try {
                        Long idProd = Long.parseLong(sc.nextLine().trim());
                        Optional<Producto> optP = productoRepo.buscarPorId(idProd);
                        if (optP.isEmpty() || optP.get().isEliminado()) {
                            System.out.println("Error: Producto no encontrado o dado de baja.");
                            break;
                        }
                        Producto prod = optP.get();
                        System.out.println("Valores actuales -> Nombre: " + prod.getNombre() + " | Precio: " + prod.getPrecio() + " | Stock: " + prod.getStock());

                        System.out.print("Nuevo nombre (dejar en blanco para conservar): ");
                        String nNom = sc.nextLine().trim();
                        if (!nNom.isEmpty()) prod.setNombre(nNom);

                        System.out.print("Nuevo precio (dejar en blanco para conservar): ");
                        String nPrecStr = sc.nextLine().trim();
                        if (!nPrecStr.isEmpty()) {
                            Double nPrec = Double.parseDouble(nPrecStr);
                            if (nPrec <= 0) {
                                System.out.println("Error: Precio invalido. No se actualiza el precio.");
                            } else {
                                prod.setPrecio(nPrec);
                            }
                        }

                        System.out.print("Nuevo stock (dejar en blanco para conservar): ");
                        String nStockStr = sc.nextLine().trim();
                        if (!nStockStr.isEmpty()) {
                            int nStock = Integer.parseInt(nStockStr);
                            if (nStock < 0) {
                                System.out.println("Error: Stock invalido. No se actualiza el stock.");
                            } else {
                                prod.setStock(nStock);
                            }
                        }

                        productoRepo.guardar(prod);
                        System.out.println("Producto modificado con exito!");
                    } catch (NumberFormatException e) {
                        System.out.println("Error en formato numerico.");
                    }
                    break;

                case "3":
                    listarProductosActivosConsola();
                    System.out.print("Ingrese ID del producto a dar de baja: ");
                    try {
                        Long idBajaP = Long.parseLong(sc.nextLine().trim());
                        Optional<Producto> optBP = productoRepo.buscarPorId(idBajaP);
                        if (optBP.isEmpty() || optBP.get().isEliminado()) {
                            System.out.println("Error: Producto inexistente o ya dado de baja.");
                            break;
                        }
                        boolean exitoP = productoRepo.eliminarLogico(idBajaP);
                        if (exitoP) {
                            System.out.println("Producto '" + optBP.get().getNombre() + "' dado de baja correctamente!");
                        } else {
                            System.out.println("No se pudo realizar la baja.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalido.");
                    }
                    break;

                case "4":
                    System.out.println("\n--- LISTADO DE PRODUCTOS POR CATEGORIA ---");
                    List<Categoria> catsL = categoriaRepo.listarActivos();
                    if (catsL.isEmpty()) {
                        System.out.println("No hay categorias activas.");
                    } else {
                        for (Categoria c : catsL) {
                            System.out.println("\nCategoria: " + c.getNombre() + " (ID: " + c.getId() + ")");
                            List<Producto> prods = categoriaRepo.buscarProductosPorCategoria(c.getId());
                            if (prods.isEmpty()) {
                                System.out.println("  -> (Sin productos activos)");
                            } else {
                                for (Producto p : prods) {
                                    System.out.println("    - ID: " + p.getId() + " | " + p.getNombre() + " | Precio: $" + p.getPrecio() + " | Stock: " + p.getStock() + " | Disponible: " + (p.getDisponible() ? "Sí" : "No"));
                                }
                            }
                        }
                    }
                    break;

                case "0":
                    volver = true;
                    break;

                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }

    private static void listarProductosActivosConsola() {
        System.out.println("\n--- PRODUCTOS ACTIVOS ---");
        List<Categoria> cats = categoriaRepo.listarActivos();
        for (Categoria c : cats) {
            List<Producto> prods = categoriaRepo.buscarProductosPorCategoria(c.getId());
            for (Producto p : prods) {
                System.out.println("ID: " + p.getId() + " | " + p.getNombre() + " | Precio: $" + p.getPrecio() + " | Stock: " + p.getStock() + " | Categoría: " + c.getNombre());
            }
        }
    }

    // ── Submenú Usuarios ─────────────────────────────────────────

    private static void menuUsuarios() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTION DE USUARIOS ---");
            System.out.println("1. Alta");
            System.out.println("2. Modificar");
            System.out.println("3. Baja logica");
            System.out.println("4. Listado");
            System.out.println("5. Buscar por mail");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");
            String op = sc.nextLine().trim();
            switch (op) {
                case "1":
                    System.out.print("Nombre: ");
                    String nombre = sc.nextLine().trim();
                    System.out.print("Apellido: ");
                    String apellido = sc.nextLine().trim();
                    System.out.print("Mail: ");
                    String mail = sc.nextLine().trim();

                    if (usuarioRepo.buscarPorMail(mail).isPresent()) {
                        System.out.println("Error: Ya existe un usuario activo registrado con ese mail.");
                        break;
                    }

                    System.out.print("Celular (opcional): ");
                    String celular = sc.nextLine().trim();
                    System.out.print("Contraseña: ");
                    String pass = sc.nextLine().trim();

                    System.out.print("Rol (1. ADMIN / 2. USUARIO): ");
                    String rolOp = sc.nextLine().trim();
                    Rol rol = rolOp.equals("1") ? Rol.ADMIN : Rol.USUARIO;

                    Usuario u = Usuario.builder()
                            .nombre(nombre)
                            .apellido(apellido)
                            .mail(mail)
                            .celular(celular.isEmpty() ? null : celular)
                            .contraseña(pass)
                            .rol(rol)
                            .build();

                    Usuario uGuardado = usuarioRepo.guardar(u);
                    System.out.println("Usuario registrado con exito! ID generado: " + uGuardado.getId() );
                    break;

                case "2":
                    listarUsuariosActivos();
                    System.out.print("Ingrese ID del usuario a modificar: ");
                    try {
                        Long idU = Long.parseLong(sc.nextLine().trim());
                        Optional<Usuario> optU = usuarioRepo.buscarPorId(idU);
                        if (optU.isEmpty() || optU.get().isEliminado()) {
                            System.out.println("Error: Usuario no encontrado o dado de baja.");
                            break;
                        }
                        Usuario user = optU.get();
                        System.out.println("Valores actuales -> Nombre: " + user.getNombre() + " " + user.getApellido() + " | Mail: " + user.getMail());

                        System.out.print("Nuevo nombre (dejar en blanco): ");
                        String nNom = sc.nextLine().trim();
                        if (!nNom.isEmpty()) user.setNombre(nNom);

                        System.out.print("Nuevo apellido (dejar en blanco): ");
                        String nApp = sc.nextLine().trim();
                        if (!nApp.isEmpty()) user.setApellido(nApp);

                        System.out.print("Nuevo mail (dejar en blanco): ");
                        String nMail = sc.nextLine().trim();
                        if (!nMail.isEmpty()) {
                            Optional<Usuario> otro = usuarioRepo.buscarPorMail(nMail);
                            if (otro.isPresent() && !otro.get().getId().equals(idU)) {
                                System.out.println("Error: El mail ya pertenece a otro usuario. No se actualiza el mail.");
                            } else {
                                user.setMail(nMail);
                            }
                        }

                        System.out.print("Nuevo celular (dejar en blanco): ");
                        String nCel = sc.nextLine().trim();
                        if (!nCel.isEmpty()) user.setCelular(nCel);

                        System.out.print("Nueva contraseña (dejar en blanco): ");
                        String nPass = sc.nextLine().trim();
                        if (!nPass.isEmpty()) user.setContraseña(nPass);

                        usuarioRepo.guardar(user);
                        System.out.println("Usuario modificado con exito!");
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalido.");
                    }
                    break;

                case "3":
                    listarUsuariosActivos();
                    System.out.print("Ingrese ID del usuario a dar de baja: ");
                    try {
                        Long idBU = Long.parseLong(sc.nextLine().trim());
                        Optional<Usuario> optBU = usuarioRepo.buscarPorId(idBU);
                        if (optBU.isEmpty() || optBU.get().isEliminado()) {
                            System.out.println("Error: Usuario no encontrado o ya dado de baja.");
                            break;
                        }
                        boolean resU = usuarioRepo.eliminarLogico(idBU);
                        if (resU) {
                            System.out.println("Usuario '" + optBU.get().getNombre() + " " + optBU.get().getApellido() + "' dado de baja con éxito!");
                        } else {
                            System.out.println("No se pudo completar la baja.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalido.");
                    }
                    break;

                case "4":
                    listarUsuariosActivos();
                    break;

                case "5":
                    System.out.print("Ingrese mail a buscar: ");
                    String mailBusca = sc.nextLine().trim();
                    Optional<Usuario> optMail = usuarioRepo.buscarPorMail(mailBusca);
                    if (optMail.isPresent()) {
                        Usuario uB = optMail.get();
                        System.out.println("\n--- DATOS DEL USUARIO ---");
                        System.out.println("ID: " + uB.getId());
                        System.out.println("Nombre completo: " + uB.getNombre() + " " + uB.getApellido());
                        System.out.println("Mail: " + uB.getMail());
                        System.out.println("Celular: " + (uB.getCelular() != null ? uB.getCelular() : "-"));
                        System.out.println("Rol: " + uB.getRol());
                        System.out.println("(Contraseña oculta por seguridad)");
                    } else {
                        System.out.println("No existe usuario activo con ese mail.");
                    }
                    break;

                case "0":
                    volver = true;
                    break;

                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }

    private static void listarUsuariosActivos() {
        List<Usuario> usuarios = usuarioRepo.listarActivos();
        System.out.println("\n--- LISTADO DE USUARIOS ACTIVOS ---");
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
        } else {
            for (Usuario u : usuarios) {
                System.out.println("ID: " + u.getId() + " | Nombre: " + u.getNombre() + " " + u.getApellido() + " | Mail: " + u.getMail() + " | Rol: " + u.getRol());
            }
        }
    }

    // ── Submenú Pedidos ──────────────────────────────────────────

    private static void menuPedidos() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTION DE PEDIDOS ---");
            System.out.println("1. Alta de pedido");
            System.out.println("2. Cambiar estado");
            System.out.println("3. Baja logica");
            System.out.println("4. Listado");
            System.out.println("5. Pedidos por usuario");
            System.out.println("6. Pedidos por estado");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");
            String op = sc.nextLine().trim();
            switch (op) {
                case "1":
                    altaPedidoFlujo();
                    break;

                case "2":
                    listarPedidosActivos();
                    System.out.print("Ingrese ID del pedido a cambiar estado: ");
                    try {
                        Long idPed = Long.parseLong(sc.nextLine().trim());
                        Optional<Pedido> optPed = pedidoRepo.buscarPorId(idPed);
                        if (optPed.isEmpty() || optPed.get().isEliminado()) {
                            System.out.println("Error: Pedido no encontrado o dado de baja.");
                            break;
                        }
                        Pedido ped = optPed.get();
                        System.out.println("Estado actual: " + ped.getEstado());
                        System.out.println("Seleccione nuevo estado: 1. PENDIENTE | 2. CONFIRMADO | 3. TERMINADO | 4. CANCELADO");
                        String estOp = sc.nextLine().trim();
                        EstadoPedido nuevoEstado = switch (estOp) {
                            case "1" -> EstadoPedido.PENDIENTE;
                            case "2" -> EstadoPedido.CONFIRMADO;
                            case "3" -> EstadoPedido.TERMINADO;
                            case "4" -> EstadoPedido.CANCELADO;
                            default -> null;
                        };
                        if (nuevoEstado == null) {
                            System.out.println("Estado invalido.");
                            break;
                        }
                        ped.setEstado(nuevoEstado);
                        pedidoRepo.guardar(ped);
                        System.out.println("Estado actualizado con exito! Pedido #" + ped.getId() + " -> " + nuevoEstado);
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalido.");
                    }
                    break;

                case "3":
                    listarPedidosActivos();
                    System.out.print("Ingrese ID del pedido a dar de baja: ");
                    try {
                        Long idBPed = Long.parseLong(sc.nextLine().trim());
                        Optional<Pedido> optBPed = pedidoRepo.buscarPorId(idBPed);
                        if (optBPed.isEmpty() || optBPed.get().isEliminado()) {
                            System.out.println("Error: Pedido inexistente o ya dado de baja.");
                            break;
                        }
                        boolean okBaja = pedidoRepo.eliminarLogico(idBPed);
                        if (okBaja) {
                            System.out.println("¡Pedido #" + optBPed.get().getId() + " (Total: $" + optBPed.get().getTotal() + ") dado de baja! (Nota: el stock NO se restaura)[cite: 11].");
                        } else {
                            System.out.println("No se pudo dar de baja.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalido.");
                    }
                    break;

                case "4":
                    listarPedidosActivos();
                    break;

                case "5":
                    List<Usuario> usrs = usuarioRepo.listarActivos();
                    if (usrs.isEmpty()) {
                        System.out.println("No hay usuarios activos.");
                        break;
                    }
                    listarUsuariosActivos();
                    System.out.print("Seleccione ID del usuario: ");
                    try {
                        Long uId = Long.parseLong(sc.nextLine().trim());
                        Optional<Usuario> optU = usuarioRepo.buscarPorId(uId);
                        if (optU.isEmpty()) {
                            System.out.println("Usuario no encontrado.");
                            break;
                        }
                        List<Pedido> pUser = usuarioRepo.buscarPedidosPorUsuario(uId);
                        System.out.println("\n--- PEDIDOS DEL USUARIO: " + optU.get().getNombre() + " " + optU.get().getApellido() + " ---");
                        if (pUser.isEmpty()) {
                            System.out.println("El usuario no tiene pedidos activos.");
                        } else {
                            for (Pedido p : pUser) {
                                System.out.println("Pedido ID: " + p.getId() + " | Fecha: " + p.getFecha() + " | Estado: " + p.getEstado() + " | Total: $" + p.getTotal());
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalido.");
                    }
                    break;

                case "6":
                    System.out.println("Seleccione estado: 1. PENDIENTE | 2. CONFIRMADO | 3. TERMINADO | 4. CANCELADO");
                    String eOp = sc.nextLine().trim();
                    EstadoPedido estSel = switch (eOp) {
                        case "1" -> EstadoPedido.PENDIENTE;
                        case "2" -> EstadoPedido.CONFIRMADO;
                        case "3" -> EstadoPedido.TERMINADO;
                        case "4" -> EstadoPedido.CANCELADO;
                        default -> null;
                    };
                    if (estSel == null) {
                        System.out.println("Estado invalido.");
                        break;
                    }
                    List<Pedido> pEst = pedidoRepo.buscarPorEstado(estSel);
                    System.out.println("\n--- PEDIDOS CON ESTADO: " + estSel + " ---");
                    if (pEst.isEmpty()) {
                        System.out.println("No hay pedidos con este estado.");
                    } else {
                        for (Pedido p : pEst) {
                            System.out.println("Pedido ID: " + p.getId() + " | Fecha: " + p.getFecha() + " | Pago: " + p.getFormaPago() + " | Total: $" + p.getTotal());
                        }
                    }
                    break;

                case "0":
                    volver = true;
                    break;

                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }

    private static void listarPedidosActivos() {
        List<Pedido> pedidos = pedidoRepo.listarActivos();
        System.out.println("\n--- LISTADO DE PEDIDOS ACTIVOS ---");
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados.");
        } else {
            for (Pedido p : pedidos) {
                System.out.println("ID: " + p.getId() + " | Fecha: " + p.getFecha() + " | Estado: " + p.getEstado() + " | Pago: " + p.getFormaPago() + " | Total: $" + p.getTotal());
            }
        }
    }

    private static void altaPedidoFlujo() {
        List<Usuario> usuarios = usuarioRepo.listarActivos();
        if (usuarios.isEmpty()) {
            System.out.println("Error: No hay usuarios activos. Debe registrar un usuario antes de crear pedidos.");
            return;
        }
        listarUsuariosActivos();
        System.out.print("Seleccione el ID del usuario que realiza el pedido: ");
        Long idUser;
        try {
            idUser = Long.parseLong(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ID invalido.");
            return;
        }

        System.out.println("Seleccione forma de pago: 1. TARJETA | 2. TRANSFERENCIA | 3. EFECTIVO");
        String pagoOp = sc.nextLine().trim();
        FormaPago formaPago = switch (pagoOp) {
            case "1" -> FormaPago.TARJETA;
            case "2" -> FormaPago.TRANSFERENCIA;
            case "3" -> FormaPago.EFECTIVO;
            default -> null;
        };
        if (formaPago == null) {
            System.out.println("Forma de pago invalida.");
            return;
        }

        class ItemTemp {
            Long prodId;
            int cant;
            ItemTemp(Long p, int c) { prodId = p; cant = c; }
        }
        List<ItemTemp> itemsTemp = new ArrayList<>();

        boolean agregarMas = true;
        while (agregarMas) {
            listarProductosActivosConsola();
            System.out.print("Ingrese ID del producto a agregar: ");
            Long prodId;
            try {
                prodId = Long.parseLong(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("ID invalido.");
                continue;
            }

            Optional<Producto> optP = productoRepo.buscarPorId(prodId);
            if (optP.isEmpty() || optP.get().isEliminado()) {
                System.out.println("Error: Producto no encontrado o dado de baja.");
                continue;
            }
            Producto p = optP.get();
            if (!p.getDisponible()) {
                System.out.println("Error: El producto no se encuentra disponible.");
                continue;
            }

            System.out.print("Ingrese cantidad (Stock disponible: " + p.getStock() + "): ");
            int cantidad;
            try {
                cantidad = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Cantidad invalida.");
                continue;
            }

            if (cantidad <= 0) {
                System.out.println("La cantidad debe ser mayor a 0.");
                continue;
            }
            if (p.getStock() < cantidad) {
                System.out.println("Error: Stock insuficiente. Stock actual: " + p.getStock());
                continue;
            }

            itemsTemp.add(new ItemTemp(prodId, cantidad));
            System.out.println("-> Producto agregado temporalmente.");

            System.out.print("Desea agregar otro producto? (S/N): ");
            String resp = sc.nextLine().trim().toUpperCase();
            if (resp.equals("N")) {
                agregarMas = false;
            }
        }

        if (itemsTemp.isEmpty()) {
            System.out.println("Operacion cancelada: El pedido debe contener al menos un producto.");
            return;
        }

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Usuario user = em.find(Usuario.class, idUser);
            if (user == null || user.isEliminado()) {
                throw new IllegalArgumentException("Usuario inexistente o dado de baja.");
            }

            Pedido pedido = Pedido.builder()
                    .fecha(LocalDate.now())
                    .estado(EstadoPedido.PENDIENTE)
                    .formaPago(formaPago)
                    .build();

            for (ItemTemp it : itemsTemp) {
                Producto prodGestionado = em.find(Producto.class, it.prodId);
                if (prodGestionado == null || prodGestionado.isEliminado() || !prodGestionado.getDisponible()) {
                    throw new IllegalArgumentException("Producto invalido o dado de baja durante la transaccion.");
                }
                if (prodGestionado.getStock() < it.cant) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto: " + prodGestionado.getNombre());
                }

                pedido.addDetallePedido(it.cant, prodGestionado);
                prodGestionado.setStock(prodGestionado.getStock() - it.cant);
            }

            pedido.calcularTotal();
            em.persist(pedido);
            user.getPedidos().add(pedido); // O el método correspondiente en Usuario

            tx.commit();

            System.out.println("\nPEDIDO CREADO EXITOSAMENTE (Transaccion Atomica)!");
            System.out.println("ID Generado: " + pedido.getId());
            System.out.println("Fecha: " + pedido.getFecha());
            System.out.println("Usuario: " + user.getNombre() + " " + user.getApellido());
            System.out.println("Forma de pago: " + pedido.getFormaPago());
            System.out.println("Total: $" + pedido.getTotal());
            System.out.println("Detalle de productos:");
            for (DetallePedido dp : pedido.getDetalles()) {
                System.out.println("  - " + dp.getCantidad() + "x " + dp.getProducto().getNombre() + " (Subtotal: $" + dp.getSubtotal() + ")");
            }

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.out.println("Error en la transaccion del pedido. Se aplico ROLLBACK completo. Causa: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    // ── Submenú Reportes ─────────────────────────────────────────

    private static void menuReportes() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- REPORTES ---");
            System.out.println("1. Productos por categoria");
            System.out.println("2. Pedidos por usuario");
            System.out.println("3. Pedidos por estado");
            System.out.println("4. Total facturado");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");
            String op = sc.nextLine().trim();
            switch (op) {
                case "1":
                    List<Categoria> cats = categoriaRepo.listarActivos();
                    if (cats.isEmpty()) {
                        System.out.println("No hay categorias activas.");
                        break;
                    }
                    listarCategoriasActivas();
                    System.out.print("Seleccione ID de categoria: ");
                    try {
                        Long cId = Long.parseLong(sc.nextLine().trim());
                        List<Producto> prodsCat = categoriaRepo.buscarProductosPorCategoria(cId);
                        System.out.println("\n--- PRODUCTOS DE LA CATEGORIA ---");
                        if (prodsCat.isEmpty()) {
                            System.out.println("No hay productos activos en esta categoria.");
                        } else {
                            for (Producto p : prodsCat) {
                                System.out.println("ID: " + p.getId() + " | Nombre: " + p.getNombre() + " | Precio: $" + p.getPrecio() + " | Stock: " + p.getStock());
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalido.");
                    }
                    break;

                case "2":
                    List<Usuario> users = usuarioRepo.listarActivos();
                    if (users.isEmpty()) {
                        System.out.println("No hay usuarios activos.");
                        break;
                    }
                    listarUsuariosActivos();
                    System.out.print("Seleccione ID de usuario: ");
                    try {
                        Long uId = Long.parseLong(sc.nextLine().trim());
                        Optional<Usuario> optU = usuarioRepo.buscarPorId(uId);
                        if (optU.isEmpty()) {
                            System.out.println("Usuario no encontrado.");
                            break;
                        }
                        List<Pedido> pUsr = usuarioRepo.buscarPedidosPorUsuario(uId);
                        System.out.println("\n--- PEDIDOS DEL USUARIO ---");
                        if (pUsr.isEmpty()) {
                            System.out.println("El usuario no tiene pedidos activos.");
                        } else {
                            for (Pedido p : pUsr) {
                                System.out.println("ID: " + p.getId() + " | Fecha: " + p.getFecha() + " | Estado: " + p.getEstado() + " | Pago: " + p.getFormaPago() + " | Total: $" + p.getTotal());
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ID invalido.");
                    }
                    break;

                case "3":
                    System.out.println("Seleccione estado: 1. PENDIENTE | 2. CONFIRMADO | 3. TERMINADO | 4. CANCELADO");
                    String eOp = sc.nextLine().trim();
                    EstadoPedido estSel = switch (eOp) {
                        case "1" -> EstadoPedido.PENDIENTE;
                        case "2" -> EstadoPedido.CONFIRMADO;
                        case "3" -> EstadoPedido.TERMINADO;
                        case "4" -> EstadoPedido.CANCELADO;
                        default -> null;
                    };
                    if (estSel == null) {
                        System.out.println("Estado invalido.");
                        break;
                    }
                    List<Pedido> pEst = pedidoRepo.buscarPorEstado(estSel);
                    System.out.println("\n--- PEDIDOS CON ESTADO: " + estSel + " ---");
                    if (pEst.isEmpty()) {
                        System.out.println("No hay pedidos con este estado.");
                    } else {
                        for (Pedido p : pEst) {
                            System.out.println("ID: " + p.getId() + " | Fecha: " + p.getFecha() + " | Pago: " + p.getFormaPago() + " | Total: $" + p.getTotal());
                        }
                    }
                    break;

                case "4":
                    List<Pedido> terminados = pedidoRepo.buscarPorEstado(EstadoPedido.TERMINADO);
                    double totalFacturado = terminados.stream()
                            .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                            .sum();
                    System.out.println("\n--- REPORTE DE FACTURACION ---");
                    System.out.println(String.format(Locale.US, "Total facturado (Pedidos Terminados): $%.2f", totalFacturado));
                    break;

                case "0":
                    volver = true;
                    break;

                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }
}