package database;

import jakarta.persistence.EntityManager;
import logic.Comentario;
import logic.Producto;

import java.util.List;


public class ComentarioD {


    public void guardar(Comentario c) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.persist(c);
        em.getTransaction().commit();
        em.close();
    }

    public List<Comentario> listarPorProducto(Producto p) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Comentario c WHERE c.producto = :producto", Comentario.class)
                    .setParameter("producto", p)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void eliminarComentario(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        try {
            Comentario c = em.find(Comentario.class, id);
            if (c != null) em.remove(c);
        } catch (Exception e) {
            em.getTransaction().rollback();
            em.close();
            return;
        }
        em.getTransaction().commit();
        em.close();
    }
}
