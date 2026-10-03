package test.java.com.plazoleta.service;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas de exclusión concurrente y sincronización del bloqueo con el ciclo de transacción. */
class PedidoCreationLockTest {

    /** Dos solicitudes para el mismo cliente no pueden poseer simultáneamente el bloqueo local. */
    @Test
    void serializesConcurrentOrderCreationForSameCustomer() throws Exception {
        // H2 selecciona la ruta de candado local y mantiene la base en memoria durante la prueba.
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:pedido-lock-test;DB_CLOSE_DELAY=-1");
        PedidoCreationLock lock = new PedidoCreationLock(new JdbcTemplate(dataSource));
        // Las señales observan inicio y adquisición del segundo hilo sin depender de pausas arbitrarias.
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch secondAcquired = new CountDownLatch(1);
        // Un hilo competidor basta para verificar la exclusión mutua.
        var executor = Executors.newSingleThreadExecutor();

        try {
            // El primer Handle mantiene el bloqueo mientras el segundo intento queda pendiente.
            try (PedidoCreationLock.Handle first = lock.acquire(15L)) {
                var second = executor.submit(() -> {
                    secondStarted.countDown();
                    try (PedidoCreationLock.Handle ignored = lock.acquire(15L)) {
                        secondAcquired.countDown();
                    }
                });
                // El segundo hilo debe haber comenzado antes de comprobar que aún no adquirió el candado.
                assertTrue(secondStarted.await(1, TimeUnit.SECONDS));
                assertFalse(secondAcquired.await(100, TimeUnit.MILLISECONDS));
                // Su Future sigue activo porque el intento continúa bloqueado.
                assertFalse(second.isDone());
            }
            // Al salir del try se libera el primer Handle y la segunda solicitud puede continuar.
            assertTrue(secondAcquired.await(1, TimeUnit.SECONDS));
        } finally {
            // Se detiene el ejecutor incluso cuando alguna aserción falla.
            executor.shutdownNow();
        }
    }

    /** Cerrar el Handle no libera el candado hasta que la transacción confirme o revierta. */
    @Test
    void retainsLockUntilTransactionCompletion() throws Exception {
        // El administrador de transacciones activa los callbacks que PedidoCreationLock usa para afterCompletion.
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:pedido-lock-transaction-test;DB_CLOSE_DELAY=-1");
        PedidoCreationLock lock = new PedidoCreationLock(new JdbcTemplate(dataSource));
        // Se ejecuta una transacción real sobre H2 para comprobar el momento exacto de liberación.
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        CountDownLatch competingRequestAcquired = new CountDownLatch(1);
        var executor = Executors.newSingleThreadExecutor();

        try {
            // El candado se adquiere dentro de una transacción todavía abierta.
            transaction.executeWithoutResult(status -> {
                PedidoCreationLock.Handle handle = lock.acquire(22L);
                // El cierre programa la liberación para después de completar la transacción.
                handle.close();
                // Otra solicitud intenta tomar el mismo candado antes de terminar la transacción actual.
                executor.submit(() -> {
                    try (PedidoCreationLock.Handle ignored = lock.acquire(22L)) {
                        competingRequestAcquired.countDown();
                    }
                });
                try {
                    // El competidor no debe adquirirlo durante la transacción vigente.
                    assertFalse(competingRequestAcquired.await(100, TimeUnit.MILLISECONDS));
                } catch (InterruptedException exception) {
                    // Restablece la señal de interrupción antes de propagar el fallo del test.
                    Thread.currentThread().interrupt();
                    throw new AssertionError(exception);
                }
            });
            // Al finalizar la transacción se ejecuta afterCompletion y el competidor puede adquirir.
            assertTrue(competingRequestAcquired.await(1, TimeUnit.SECONDS));
        } finally {
            // Evita dejar hilos en ejecución al terminar este caso.
            executor.shutdownNow();
        }
    }
}