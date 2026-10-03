package main.java.com.plazoleta.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Datos requeridos por HU2 para registrar un restaurante y su propietario en Usuarios. */
public record RestauranteRequest(
        // Exige texto y rechaza un nombre formado únicamente por números.
        @NotBlank @Pattern(regexp = ".*\\D.*", message = "El nombre no puede contener solo numeros") String nombre,
        // El NIT se representa como texto para conservar ceros iniciales, pero solo admite dígitos.
        @NotBlank @Pattern(regexp = "\\d+", message = "El NIT debe contener solo numeros") String nit,
        // La dirección debe incluir al menos un carácter no blanco.
        @NotBlank String direccion,
        // Se permiten teléfonos de hasta 13 dígitos, con un signo + opcional al inicio.
        @NotBlank @Size(max = 13) @Pattern(regexp = "\\+?\\d{1,13}", message = "Telefono invalido") String telefono,
        // El logo debe ser una URL que use HTTP o HTTPS.
        @NotBlank @Pattern(regexp = "https?://.+", message = "La URL del logo debe ser HTTP o HTTPS") String urlLogo,
        // Es una referencia al usuario propietario; el servicio Usuarios confirma existencia y rol.
        @NotNull @Positive Long idPropietario) {
}