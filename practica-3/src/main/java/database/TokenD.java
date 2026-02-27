package database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import logic.tokenS;

public class TokenD {

    public void guardar(tokenS t) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.persist(t);
        em.getTransaction().commit();
        em.close();
    }

    public tokenS buscarPorToken(String token) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM tokenS t WHERE t.token = :token", tokenS.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public void eliminar(String token) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        try {
            tokenS t = em.createQuery(
                            "SELECT t FROM tokenS t WHERE t.token = :token", tokenS.class)
                    .setParameter("token", token)
                    .getSingleResult();
            em.remove(t);
        } catch (NoResultException e) {
            // token not found, nothing to delete
        }
        em.getTransaction().commit();
        em.close();
    }

}