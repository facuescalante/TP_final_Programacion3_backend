package com.tp.jpa.repository;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

/**
 * Repositorio de Categoria. Además del CRUD heredado implementa la consulta
 * de productos activos pertenecientes a una categoría y la asociación de productos.
 *
 * Nota de diseño: como la relación es unidireccional y Categoria es la dueña
 * de la colección Set<Producto>, la navegación se hace desde Categoria hacia
 * sus productos (p. ej. JPQL con JOIN sobre c.productos)[cite: 8].
 */
public class CategoriaRepository extends BaseRepository<Categoria> {

    public CategoriaRepository() {
        super(Categoria.class);
    }

    /**
     * Retorna los productos activos que pertenecen a la categoría indicada.
     */
    public List<Producto> buscarProductosPorCategoria(Long categoriaId) {
        EntityManager em = emf.createEntityManager();
        try {
            // Consulta JPQL: retorna los productos activos de una categoría.
            // Como la relación es unidireccional y Categoria es la dueña, se
            // navega desde Categoria hacia su colección c.productos mediante JOIN.
            // Se filtra por el id de la categoría (parámetro nombrado :catId) y
            // por p.eliminado = false para excluir las bajas lógicas.
            String jpql = "SELECT p FROM Categoria c JOIN c.productos p "
                    + "WHERE c.id = :catId AND p.eliminado = false";
            return em.createQuery(jpql, Producto.class)
                    .setParameter("catId", categoriaId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Agrega un producto a una categoría existente en una única transacción y EntityManager,
     * aprovechando CascadeType.ALL para persistir el producto con su categoria_id[cite: 8].
     */
    public Producto agregarProducto(Long categoriaId, Producto producto) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Categoria categoria = em.find(Categoria.class, categoriaId);
            if (categoria == null || categoria.isEliminado()) {
                throw new IllegalArgumentException("La categoría no existe o se encuentra dada de baja.");
            }

            // Asume que la entidad Categoria cuenta con un método helper addProducto(Producto p)
            // o se manipula directamente la colección.
            categoria.getProductos().add(producto);

            tx.commit();
            return producto;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}