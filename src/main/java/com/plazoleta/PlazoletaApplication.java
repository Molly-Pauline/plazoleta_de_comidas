package main.java.com.plazoleta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada que activa el escaneo de componentes y la configuración automática de Spring Boot. */
@SpringBootApplication
public class PlazoletaApplication {

    /** Inicia el contexto de Spring y mantiene levantado el microservicio. */
    public static void main(String[] args) {
        SpringApplication.run(PlazoletaApplication.class, args);
    }
}