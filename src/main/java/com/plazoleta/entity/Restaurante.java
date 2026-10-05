package main.java.com.plazoleta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Entidad persistida del restaurante; conserva el ID del propietario como referencia al servicio Usuarios. */
@Entity
@Table(name = "restaurantes")
public class Restaurante {

    /** Clave primaria generada por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre obligatorio limitado para evitar almacenar valores fuera del tamaño definido en el modelo. */
    @Column(nullable = false, length = 120)
    private String nombre;

    /** NIT único: la restricción de base protege también contra dos inserciones concurrentes. */
    @Column(nullable = false, unique = true, length = 20)
    private String nit;

    /** Dirección obligatoria del establecimiento. */
    @Column(nullable = false, length = 240)
    private String direccion;

    /** Teléfono guardado como texto para conservar prefijo y ceros iniciales. */
    @Column(nullable = false, length = 13)
    private String telefono;

    /** URL del logo expuesta por los DTO públicos de restaurante. */
    @Column(name = "url_logo", nullable = false, length = 500)
    private String urlLogo;

    /** ID externo de Usuarios; no es una relación JPA porque cada servicio administra su propia base. */
    @Column(name = "id_propietario", nullable = false)
    private Long idPropietario;

    /** Constructor requerido por JPA para reconstruir la entidad desde la tabla. */
    protected Restaurante() {
    }

    /** Construye un restaurante con los datos validados por el contrato de entrada y el servicio. */
    public Restaurante(String nombre, String nit, String direccion, String telefono, String urlLogo, Long idPropietario) {
        this.nombre = nombre;
        this.nit = nit;
        this.direccion = direccion;
        this.telefono = telefono;
        this.urlLogo = urlLogo;
        this.idPropietario = idPropietario;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNit() { return nit; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getUrlLogo() { return urlLogo; }
    public Long getIdPropietario() { return idPropietario; }
}