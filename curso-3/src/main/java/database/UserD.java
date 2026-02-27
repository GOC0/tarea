package database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import logic.User;

import java.util.List;

public class UserD {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("MiUnidadPersistencia");

    public void Eliminar(Long id){
       EntityManager em =emf.createEntityManager();
        User u = em.find(User.class, id);
        if (u != null) em.remove(u);
        em.getTransaction().commit();
        em.close();
    }

    public void crear(User u) {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.persist(u);
        em.getTransaction().commit();
        em.close();
    }

    public User buscarPorNombre(String nombre) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.name = :nombre", User.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public User leer(Long id) {
        EntityManager em = emf.createEntityManager();
        User u = em.find(User.class, id);
        em.close();
        return u;
    }

    public List<User> listar() {
        EntityManager em = emf.createEntityManager();
        List<User> usuarios = em.createQuery("from User", User.class).getResultList();
        em.close();
        return usuarios;
    }

    public void actualizar(User u) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(u);
        em.getTransaction().commit();
        em.close();
    }




}
