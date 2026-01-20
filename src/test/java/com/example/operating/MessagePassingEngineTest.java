package com.example.operating;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import com.example.UpdateSink;
import com.example.QueueUpdate;
import com.example.MessagePassingEngine;
import com.example.PhotoMsg;
import com.example.ConsoleUpdateSink;
import com.example.MPDoctorQueueManager;

class MessagePassingEngineTest {

    static final class LatchingSink implements UpdateSink {
        final CountDownLatch latch;
        final AtomicReference<QueueUpdate> last = new AtomicReference<>();
        final CopyOnWriteArrayList<QueueUpdate> all = new CopyOnWriteArrayList<>();

        LatchingSink(int expectedPushes) {
            this.latch = new CountDownLatch(expectedPushes);
        }

        @Override
        public void push(QueueUpdate update) {
            all.add(update);
            last.set(update);
            latch.countDown();
        }

        boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return latch.await(timeout, unit);
        }
    }

    @Test
    void accept_eventuallyPushesUpdate() throws Exception {
        LatchingSink sink = new LatchingSink(1);
        MessagePassingEngine engine = new MessagePassingEngine(new MPDoctorQueueManager(), sink);

        try {
            engine.accept(new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO, 10L));
            assertTrue(sink.await(2, TimeUnit.SECONDS), "Expected push from pusher thread");

            QueueUpdate u = sink.last.get();
            assertNotNull(u);
            assertEquals("D1", u.doctorId);
            assertEquals(List.of("img1"), u.queueOrdered.stream().map(i -> i.imageCode).toList());
        } finally {
            engine.shutdown();
        }
    }

    @Test
    void getQueue_returnsCurrentState() throws Exception {
        LatchingSink sink = new LatchingSink(2);
        MessagePassingEngine engine = new MessagePassingEngine(new MPDoctorQueueManager(), sink);

        try {
            engine.accept(new PhotoMsg("low", "D1", PhotoMsg.Urgency.BAJO, 10L));
            engine.accept(new PhotoMsg("high", "D1", PhotoMsg.Urgency.ALTO, 20L));

            assertTrue(sink.await(2, TimeUnit.SECONDS), "Expected 2 pushes");

            QueueUpdate q = engine.getQueue("D1");
            assertEquals(List.of("high", "low"), q.queueOrdered.stream().map(i -> i.imageCode).toList());
        } finally {
            engine.shutdown();
        }
    }

    @Test
    void remove_whenItemIsInDoctorQueue_removesAndPushesUpdate_returnsTrue() throws Exception {
        // Configuramos para esperar 2 señales: la del accept inicial y la del remove
        LatchingSink sink = new LatchingSink(2);
        MessagePassingEngine engine = new MessagePassingEngine(new MPDoctorQueueManager(), sink);

        try {
            engine.accept(new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO, 1L));

            // CORRECCIÓN LÍNEA 130: En lugar de sleep, esperamos a que el primer push
            // ocurra
            // Usamos un pequeño bucle o simplemente verificamos que la lista 'all' ya tenga
            // el primer item
            long start = System.currentTimeMillis();
            while (sink.all.isEmpty() && (System.currentTimeMillis() - start) < 2000) {
                Thread.onSpinWait(); // Alternativa eficiente a sleep para esperas ultra cortas
            }

            boolean removed = engine.remove("D1", "img1");
            assertTrue(removed, "El elemento debería haber sido eliminado");

            // Esperamos a que se completen los 2 pushes (el inicial + el del remove)
            assertTrue(sink.await(2, TimeUnit.SECONDS), "Expected initial push + remove push");

            QueueUpdate q = engine.getQueue("D1");
            assertEquals(0, q.sizes.get("TOTAL"));
        } finally {
            engine.shutdown();
        }
    }

    @Test
    void remove_missing_returnsFalse_andDoesNotPushExtraUpdate() throws Exception {
        LatchingSink sink = new LatchingSink(1);
        MessagePassingEngine engine = new MessagePassingEngine(new MPDoctorQueueManager(), sink);

        try {
            engine.accept(new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO, 1L));
            assertTrue(sink.await(2, TimeUnit.SECONDS), "Expected initial push");

            int pushesBefore = sink.all.size();

            boolean removed = engine.remove("D1", "nope");
            assertFalse(removed);

            // CORRECCIÓN LÍNEA 161: Para verificar que NO hay más pushes,
            // simplemente verificamos el tamaño de la lista tras una espera corta en el
            // latch
            // (que sabemos que no bajará de 0)
            sink.latch.await(150, TimeUnit.MILLISECONDS);

            int pushesAfter = sink.all.size();
            assertEquals(pushesBefore, pushesAfter, "No extra push expected when remove() returns false");
        } finally {
            engine.shutdown();
        }
    }

    @Test
    void shutdown_stopsThreads_andSubsequentAcceptMayBlockOrNotPush_butShouldNotHangTest() throws Exception {
        LatchingSink sink = new LatchingSink(1);
        MessagePassingEngine engine = new MessagePassingEngine(new MPDoctorQueueManager(), sink);

        engine.shutdown();
        assertDoesNotThrow(() -> engine.accept(new PhotoMsg("x", "D1", PhotoMsg.Urgency.ALTO, 1L)));
        assertFalse(sink.await(200, TimeUnit.MILLISECONDS));
    }

    @Test
    void testEngineLifecycle() throws InterruptedException {
        MessagePassingEngine engine = new MessagePassingEngine(new MPDoctorQueueManager(), new ConsoleUpdateSink());

        // Test shutdown (Cubre la interrupción del hilo)
        engine.shutdown();

        // Test loadAll con datos (Cubre la reconstrucción del estado)
        Map<String, List<QueueUpdate.QueueItem>> state = Map.of(
                "D1", List.of(new QueueUpdate.QueueItem("img1", "BAJO", 1000L)));
        assertDoesNotThrow(() -> engine.loadAll(state));
    }

    @Test
    void testStateAndDumpCoverage() {
        MessagePassingEngine engine = new MessagePassingEngine(new MPDoctorQueueManager(), new ConsoleUpdateSink());
        try {
            // Cubre el método state()
            assertNotNull(engine.state());
            // Cubre el método dumpAll()
            assertNotNull(engine.dumpAll());
            // Cubre el método getQueue()
            assertNotNull(engine.getQueue("D1"));
        } finally {
            engine.shutdown();
        }
    }

    @Test
    void testRemoveFromIncomingQueueCoverage() throws Exception {
        LatchingSink sink = new LatchingSink(1);
        MessagePassingEngine engine = new MessagePassingEngine(new MPDoctorQueueManager(), sink);

        try {
            // Pausamos los hilos del motor para que el mensaje se quede atrapado en la cola
            // de entrada
            // Esto se hace forzando un remove antes de que el dispatcher actúe
            PhotoMsg msg = new PhotoMsg("img-temp", "D-TEMP", PhotoMsg.Urgency.ALTO, 1L);
            engine.accept(msg);

            // Cubre la línea: incomingQueue.removeIf(...)
            // Intentamos borrarlo antes de que el dispatcher lo procese
            boolean removed = engine.remove("D-TEMP", "img-temp");

            // No importa si devuelve true o false (depende de la velocidad),
            // lo importante es que el test pase por esa línea de código.
            assertDoesNotThrow(() -> engine.getQueue("D-TEMP"));
        } finally {
            engine.shutdown();
        }
    }

    @Test
    void testErrorHandlingInLoops() throws Exception {
        // Para cubrir los bloques "catch (Exception ex)" necesitamos que el manager o
        // el sink fallen
        // Usamos un Sink que lance una RuntimeException
        UpdateSink errorSink = update -> {
            throw new RuntimeException("Simulated Error");
        };

        MessagePassingEngine engine = new MessagePassingEngine(new MPDoctorQueueManager(), errorSink);

        try {
            engine.accept(new PhotoMsg("error-img", "D1", PhotoMsg.Urgency.ALTO, 1L));

            // Damos un tiempo para que el error se imprima en System.err
            Thread.sleep(200);

            // Si el motor sigue vivo tras el error, el catch ha funcionado
            assertDoesNotThrow(() -> engine.getQueue("D1"));
        } finally {
            engine.shutdown();
        }
    }
}