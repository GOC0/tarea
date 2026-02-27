package database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import logic.Producto;
import logic.Venta;

import java.util.List;

// VentaD.java
public class VentaD {

    public void agregar(Venta v) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.persist(v);
        em.getTransaction().commit();
        em.close();
    }

    public List<Venta> listar() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT v FROM Venta v WHERE v.vendido=true", Venta.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void actualizar(Venta v) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.merge(v);
        em.getTransaction().commit();
        em.close();
    }

    public void eliminar(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        try {
            Venta v = em.find(Venta.class, id);
            if (v != null) em.remove(v);
        } catch (Exception e) {
            // not found
        }
        em.getTransaction().commit();
        em.close();
    }

    public Venta buscarPendiente() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            List<Venta> ventas = em.createQuery(
                            "SELECT v FROM Venta v WHERE v.vendido = false", Venta.class)
                    .getResultList();

            return ventas.isEmpty() ? null : ventas.get(0);
        } finally {
            em.close();
        }
    }
    }
