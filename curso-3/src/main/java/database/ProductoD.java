package database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import logic.Producto;
import java.util.List;

public class ProductoD {

    public void crear(Producto p) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.persist(p);
        em.getTransaction().commit();
        em.close();
    }

    public void guardar(Producto p) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.persist(p);
        em.getTransaction().commit();
        em.close();
    }

    public Producto leer(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        Producto p = em.find(Producto.class, id);
        em.close();
        return p;
    }

    public List<Producto> listar() {
        EntityManager em = HibernateUtil.getEntityManager();
        List<Producto> productos = em.createQuery(
                        "SELECT p FROM Producto p", Producto.class)
                .getResultList();
        System.out.println("Productos encontrados: " + productos.size());
        em.close();
        return productos;
    }

    public void actualizar(Producto p) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.merge(p);
        em.getTransaction().commit();
        em.close();
    }

    public void eliminar(String nombre) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        try {
            Producto p = em.createQuery(
                            "SELECT p FROM Producto p WHERE p.name = :nombre", Producto.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
            em.remove(p);
        } catch (NoResultException e) {
        }
        em.getTransaction().commit();
        em.close();
    }
    public Producto buscarPorNombre(String nombre) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Producto p WHERE p.name = :nombre", Producto.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
    public List<Producto> listarPaginado(int pagina, int porPagina) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Producto p", Producto.class)
                    .setFirstResult((pagina - 1) * porPagina)
                    .setMaxResults(porPagina)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public long contarProductos() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(p) FROM Producto p", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}