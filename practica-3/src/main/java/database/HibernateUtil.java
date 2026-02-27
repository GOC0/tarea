package database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.h2.tools.Server;

public class HibernateUtil {

    private static EntityManagerFactory emf;
    private static Server tcpServer;
    private static Server webServer;

    public static void startH2Server() {
        try {
            // Start H2 in TCP server mode
            tcpServer = Server.createTcpServer(
                    "-tcpAllowOthers",
                    "-tcpPort", "9092",
                    "-ifNotExists"
            ).start();
            System.out.println("H2 TCP Server started on port 9092");

            // Start H2 web console
            webServer = Server.createWebServer("-webAllowOthers", "-webPort", "8082").start();
            System.out.println("H2 Web Console at: http://localhost:8082");


            emf = Persistence.createEntityManagerFactory("MiUnidadPersistencia");
            System.out.println("Hibernate initialized successfully");



        } catch (Exception e) {
            throw new RuntimeException("Failed to start H2 or Hibernate", e);
        }
    }

    public static EntityManager getEntityManager() {
        if (emf == null) {
            throw new IllegalStateException("HibernateUtil not initialized. Call startH2Server() first.");
        }
        return emf.createEntityManager();
    }

    public static void shutdown() {
        if (emf != null) emf.close();
        if (tcpServer != null) tcpServer.stop();
        if (webServer != null) webServer.stop();
        System.out.println("H2 Server stopped");
    }
}