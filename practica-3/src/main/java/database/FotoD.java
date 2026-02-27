package database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import logic.Foto;



public class FotoD {

    public void guardarFoto(Foto t) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.persist(t);
        em.getTransaction().commit();
        em.close();
    }

    public void eliminarFoto(String nombre) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        Foto t = buscarPorNombre(nombre);
        if (t != null) em.remove(em.merge(t));
        em.getTransaction().commit();
        em.close();
    }

    private Foto buscarPorNombre(String nombre) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT f FROM Foto f WHERE f.nombre = :nombre", Foto.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
