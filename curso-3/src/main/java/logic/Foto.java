package logic;

import jakarta.persistence.*;


@Entity
@Table(name="Foto")
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    @Lob
    private String fotoBase64;


    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    public Foto() {}
    public Foto(String nombre, String fotoBase64) {
        this.nombre = nombre;
        this.fotoBase64 = fotoBase64;
    }
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

}
