import database.*;
import io.javalin.Javalin;
import io.javalin.config.SizeUnit;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import logic.*;

import java.time.LocalDateTime;
import java.util.*;


public class Main {


    public static void main(String[] args) {


        HibernateUtil.startH2Server();

        UserD userD = new UserD();
        ProductoD prodD= new ProductoD();
        TokenD tokenD= new TokenD();
        FotoD fotoD=new FotoD();
        VentaD ventaD=new VentaD();
        ComentarioD comentarioD= new ComentarioD();


        User admin = userD.buscarPorNombre("admin");
        if (admin == null) {
            User usuario = new User("admin", "1234",true);
            userD.crear(usuario);
            System.out.println("Usuario admin creado por defecto.");
        } else {
            System.out.println("Usuario admin ya existe.");
        }

        if (prodD.listar().isEmpty()) {
            prodD.guardar(new Producto("laptop","electronico", 2000, 5));
            prodD.guardar(new Producto("mouse","para usar", 500, 10));
            prodD.guardar(new Producto("teclado","util", 800, 7));
        } else {

        }



        var app = Javalin.create(config -> {
            config.fileRenderer(new JavalinThymeleaf());
            config.http.maxRequestSize = 10_000_000L;
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.hostedPath = "/";
                staticFileConfig.directory = "/public";
                staticFileConfig.location = Location.CLASSPATH;
            });
        }).start(8000);



        app.before("/*",ctx->{

            String path = ctx.path();
            if (path.equals("/login.html") ||
                    path.equals("/login") ||
                    path.endsWith(".css") ||
                    path.endsWith(".js") ||
                    path.endsWith(".png") ||
                    path.endsWith(".jpg") ||
                    path.endsWith(".ico")) return;


            String user = ctx.sessionAttribute("user");
            String tomarCookie= ctx.cookie("user");

            if ( user == null && tomarCookie ==null){
                ctx.redirect("/login.html");
            }else if(user== null && tomarCookie!=null) {
                ctx.sessionAttribute("user",tomarCookie);
                ctx.redirect("/carrito");
            }

        });
        app.get("/login",ctx->{
            ctx.redirect("/login.html");
        });

        app.post("/login", ctx->{
           String name = ctx.formParam("usuario");
           String password= ctx.formParam("password");
           Boolean validar= Boolean.valueOf(ctx.formParam("remember"));
           User a= userD.buscarPorNombre(name);

           if(a!=null && a.getPassword().equals(password) && validar){
               tokenS tok = new tokenS(UUID.randomUUID().toString(), name,
                       LocalDateTime.now().plusDays(7));
               tokenD.guardar(tok);
               ctx.sessionAttribute("user", name);
               ctx.cookie("user", tok.getToken(), 60 * 60 * 24 * 7);
               ctx.redirect("/carrito");
           }else if(a!=null && a.getPassword().equals(password) && !validar){
               ctx.sessionAttribute("user",name);
               ctx.redirect("/carrito");

            }else{
               ctx.status(401).result("not egual");
           }
        });


        app.post("/cerrarSession", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.removeCookie("user");
            ctx.redirect("/login.html");
        });

        app.get("/carrito", ctx -> {
            int pagina = 1;
            String paginaParam = ctx.queryParam("pagina");
            if (paginaParam != null) {
                pagina = Integer.parseInt(paginaParam);
            }

            int porPagina = 10;
            long total = prodD.contarProductos();
            int totalPaginas = (int) Math.ceil((double) total / porPagina);

            Map<String, Object> model = new HashMap<>();
            model.put("nombre", ctx.sessionAttribute("user"));
            model.put("productos", prodD.listarPaginado(pagina, porPagina));
            model.put("paginaActual", pagina);
            model.put("totalPaginas", totalPaginas);
            ctx.render("Template/carrito.html", model);
        });

        app.post("/agregar", ctx -> {
            String nombreProducto = ctx.formParam("productoId");
            int cantidad = Integer.parseInt(Objects.requireNonNull(ctx.formParam("cantidad")));

            Producto p = prodD.buscarPorNombre(nombreProducto);
            if (p == null) {
                ctx.redirect("/carrito");
                return;
            }
            if (p.getCantidad() < cantidad) {
                ctx.status(400).result("Stock insuficiente. Solo quedan " + p.getCantidad());
                return;
            }

            Venta venta = ctx.sessionAttribute("ventaPendiente");
            if (venta == null) {
                venta = ventaD.buscarPendiente();
            }
            if (venta == null) {
                venta = new Venta(new ArrayList<>());
                ventaD.agregar(venta);
            }

            for (int i = 0; i < cantidad; i++) {
                venta.getProductos().add(p);
            }
            ventaD.actualizar(venta);
            p.setCantidad(p.getCantidad() - cantidad);
            prodD.actualizar(p);
            ctx.sessionAttribute("ventaPendiente", venta);
            ctx.redirect("/carrito");
        });



        app.get("/administrar", ctx -> {

            String nombre = ctx.sessionAttribute("user");
            List<Producto> productos = prodD.listar();
                ctx.render("Template/administrar.html", Map.of(
                        "nombre", nombre,
                        "productos", productos
                ));
        });

        app.post("/agregarProduct", ctx -> {
            String n = ctx.formParam("nombre");
            String des = ctx.formParam("descripcion");
            int pre = Integer.parseInt(ctx.formParam("precio"));
            int can = Integer.parseInt(ctx.formParam("cantidad"));
            List<UploadedFile> archivos = ctx.uploadedFiles("fotos");
            if (archivos == null || archivos.isEmpty()) {
                ctx.status(400).result("La imagen es obligatoria");
                return;
            }

            if (prodD.buscarPorNombre(n) == null) {
                Producto p = new Producto(n, des, pre, can);
                prodD.guardar(p);


                for (UploadedFile archivo : archivos) {
                    byte[] bytes = archivo.content().readAllBytes();
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    Foto foto = new Foto(archivo.filename(), base64);
                    foto.setProducto(p);
                    fotoD.guardarFoto(foto);
                }
            } else {
                ctx.status(400).result("Producto ya existe");
                return;
            }

            ctx.redirect("/administrar");
        });

        app.post("/EliminarProd",ctx->{
            String n= ctx.formParam("nombre");
            prodD.eliminar(n);
            ctx.redirect("/administrar");
        });

        app.post("/actualizarProd", ctx -> {
            String n = ctx.formParam("nombre");
            String des = ctx.formParam("descripcion");
            int pre = Integer.parseInt(ctx.formParam("precio"));
            int can = Integer.parseInt(ctx.formParam("cantidad"));

            Producto p = prodD.buscarPorNombre(n);
            if (p != null) {
                p.setDescripcion(des);
                p.setPrecio(pre);
                p.setCantidad(can);

                List<UploadedFile> archivos = ctx.uploadedFiles("fotos");
                if (archivos != null && !archivos.isEmpty()) {
                    for (UploadedFile archivo : archivos) {
                        byte[] bytes = archivo.content().readAllBytes();
                        String base64 = Base64.getEncoder().encodeToString(bytes);
                        Foto foto = new Foto(archivo.filename(), base64);
                        foto.setProducto(p);
                        fotoD.guardarFoto(foto);
                    }
                }

                prodD.actualizar(p);
            }

            ctx.redirect("/administrar");
        });
        app.post("/subirFoto", ctx -> {
            String productoNombre = ctx.formParam("productoNombre");
            var uploadedFile = ctx.uploadedFile("foto");

            byte[] bytes = uploadedFile.content().readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            Producto p = prodD.buscarPorNombre(productoNombre);
            if (p != null) {
                Foto foto = new Foto(uploadedFile.filename(), base64);
                foto.setProducto(p);
                fotoD.guardarFoto(foto);
            }
            ctx.redirect("/administrar");
        });

        app.get("/venta", ctx -> {
            String nombre = ctx.sessionAttribute("user");
            ctx.render("Template/venta.html", Map.of(
                    "nombre", nombre,
                    "ventas", ventaD.listar()
            ));
        });

        app.get("/compra", ctx -> {
            String nombre = ctx.sessionAttribute("user");
            Venta venta = ctx.sessionAttribute("ventaPendiente");
            List<Producto> carrito = venta != null ? venta.getProductos() : new ArrayList<>();

            ctx.render("Template/compra.html", Map.of(
                    "nombre", nombre,
                    "carrito", carrito
            ));
        });

        app.post("/comprarItem", ctx -> {
            String user = ctx.sessionAttribute("user");

            Venta venta = ventaD.buscarPendiente();

            if (venta == null || venta.getProductos().isEmpty()) {
                ctx.status(400).result("El carrito está vacío");
                return;
            }

            venta.setVendido(true);
            ventaD.actualizar(venta);

            ctx.sessionAttribute("ventaPendiente", null);
            ctx.redirect("/venta");
        });

        app.post("/EliminarItem", ctx -> {
            String nombreProducto = ctx.formParam("productoId");

            Venta venta = ctx.sessionAttribute("ventaPendiente");
            if (venta == null) {
                ctx.redirect("/compra");
                return;
            }

            Producto p = prodD.buscarPorNombre(nombreProducto);
            if (p != null) {
                venta.getProductos().removeIf(prod -> prod.getName().equals(nombreProducto));
                ventaD.actualizar(venta);
                p.setCantidad(p.getCantidad() + 1);
                prodD.actualizar(p);
            }
            ctx.sessionAttribute("ventaPendiente", venta);
            ctx.redirect("/compra");
        });

        app.get("/comentario/{nombre}", ctx -> {
            String nombre = ctx.pathParam("nombre");
            String user = ctx.sessionAttribute("user");

            Producto p = prodD.buscarPorNombre(nombre);
            User u = userD.buscarPorNombre(user);

            Map<String, Object> model = new HashMap<>();
            model.put("producto", p);
            model.put("comentarios", comentarioD.listarPorProducto(p));
            model.put("isAdmin", u != null && u.isAdmin());
            model.put("nombre", user);
            ctx.render("Template/comentario.html", model);
        });

        app.post("/agregarComentario", ctx -> {
            String productoNombre = ctx.formParam("productoNombre");
            String mensaje = ctx.formParam("mensaje");
            String autor = ctx.sessionAttribute("user");

            Producto p = prodD.buscarPorNombre(productoNombre);
            if (p != null && mensaje != null && !mensaje.isBlank()) {
                comentarioD.guardar(new Comentario(autor, mensaje, p));
            }
            ctx.redirect("/comentario/" + productoNombre);
        });

        app.post("/eliminarComentario", ctx -> {
            String user = ctx.sessionAttribute("user");
            String productoNombre = ctx.formParam("productoNombre");
            User u = userD.buscarPorNombre(user);

            if (u != null && u.isAdmin()) {
                Long id = Long.parseLong(ctx.formParam("comentarioId"));
                comentarioD.eliminarComentario(id);
            }
            ctx.redirect("/comentario/" + productoNombre);
        });



    }
}
