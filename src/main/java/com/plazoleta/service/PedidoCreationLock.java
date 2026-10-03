package main.java.com.plazoleta.service;

import com.plazoleta.exception.DomainException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Serializa la creación de pedidos del mismo cliente entre hilos e instancias de la aplicación. */
/** Serializa la creación de pedidos del mismo cliente entre hilos e instancias de la aplicación. */
@Component
public class PedidoCreationLock {

    /** Respaldo local para pruebas y bases de datos que no ofrecen GET_LOCK. */
    private static final ConcurrentHashMap<Long, ReentrantLock> LOCAL_LOCKS = new ConcurrentHashMap<>();
    /** Ejecuta consultas y detecta qué motor de base de datos está conectado. */
    private final JdbcTemplate jdbcTemplate;

    /** Recibe el acceso JDBC que permite usar el bloqueo distribuido de MySQL/MariaDB. */
    public PedidoCreationLock(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Obtiene el bloqueo asociado al cliente antes de validar o persistir su pedido. */
    public Handle acquire(Long idCliente) {
        // El bloqueo de MySQL es compartido por varias instancias; H2 y otros motores usan el bloqueo local.
        String database = jdbcTemplate.execute((Connection connection) -> connection.getMetaData().getDatabaseProductName());
        if (database != null && (database.equalsIgnoreCase("MySQL") || database.equalsIgnoreCase("MariaDB"))) {
            return acquireMysqlLock(idCliente);
        }

        // Reutilizar el mismo candado por cliente impide dos creaciones simultáneas en esta JVM.
        ReentrantLock lock = LOCAL_LOCKS.computeIfAbsent(idCliente, ignored -> new ReentrantLock());
        try {
            // El límite evita dejar solicitudes esperando indefinidamente por un pedido atascado.
            if (!lock.tryLock(10, TimeUnit.SECONDS)) {
                throw new DomainException(HttpStatus.CONFLICT, "Ya se esta procesando un pedido para este cliente");
            }
        } catch (InterruptedException exception) {
            // Se restaura la señal para que las capas superiores puedan respetar la cancelación del hilo.
            Thread.currentThread().interrupt();
            throw new DomainException(HttpStatus.SERVICE_UNAVAILABLE, "Se interrumpio la espera para crear el pedido");
        }
        // La liberación se difiere hasta el fin de la transacción para cubrir también su commit o rollback.
        return () -> releaseAfterTransaction(lock::unlock);
    }

    /** Usa un candado nombrado del servidor SQL, coordinando clientes atendidos por distintas instancias. */
    private Handle acquireMysqlLock(Long idCliente) {
        // El identificador es determinista por cliente; el timeout coincide con el tiempo máximo de espera.
        String lockName = "plazoleta-order-" + idCliente;
        // GET_LOCK devuelve 1 solo si se obtuvo el candado; null/0 indica que no se consiguió.
        Integer acquired = jdbcTemplate.queryForObject("SELECT GET_LOCK(?, 10)", Integer.class, lockName);
        if (!Integer.valueOf(1).equals(acquired)) {
            throw new DomainException(HttpStatus.CONFLICT, "Ya se esta procesando un pedido para este cliente");
        }
        // El callback libera el mismo candado nombrado cuando termine el trabajo transaccional.
        return () -> releaseAfterTransaction(
                () -> jdbcTemplate.queryForObject("SELECT RELEASE_LOCK(?)", Integer.class, lockName));
    }

    /** Mantiene el bloqueo hasta que la transacción complete, aunque el try-with-resources ya haya cerrado el Handle. */
    private void releaseAfterTransaction(Runnable release) {
        // Sin sincronización activa no hay ciclo transaccional que esperar, así que la liberación es inmediata.
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            release.run();
            return;
        }
        // Spring invoca este callback tanto tras commit como tras rollback.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                // Libera solo cuando ya no puede quedar una escritura del pedido a medio confirmar.
                release.run();
            }
        });
    }

    /** Handle autocerrable para integrar la adquisición con try-with-resources. */
    @FunctionalInterface
    public interface Handle extends AutoCloseable {
        @Override
        void close();
    }
}
