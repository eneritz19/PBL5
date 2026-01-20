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
            assertEquals(1, u.sizes.get("ALTO"));
            assertEquals(1, u.sizes.get("TOTAL"));
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
            assertEquals(2, q.sizes.get("TOTAL"));
        } finally {
            engine.shutdown();
        }
    }

    @Test
    void dumpAll_and_loadAll_roundTrip() throws Exception {
        LatchingSink sink1 = new LatchingSink(2);
        MessagePassingEngine engine1 = new MessagePassingEngine(new MPDoctorQueueManager(), sink1);

        Map<String, List<QueueUpdate.QueueItem>> state;
        try {
            engine1.accept(new PhotoMsg("a", "D1", PhotoMsg.Urgency.ALTO, 10L));
            engine1.accept(new PhotoMsg("b", "D2", PhotoMsg.Urgency.MEDIO, 20L));
            assertTrue(sink1.await(2, TimeUnit.SECONDS));

            state = engine1.dumpAll();
            assertTrue(state.containsKey("D1"));
            assertTrue(state.containsKey("D2"));
        } finally {
            engine1.shutdown();
        }

        // Carga en engine2 (no debería necesitar pushes)
        LatchingSink sink2 = new LatchingSink(0);
        MessagePassingEngine engine2 = new MessagePassingEngine(new MPDoctorQueueManager(), sink2);
        try {
            engine2.loadAll(state);

            QueueUpdate d1 = engine2.getQueue("D1");
            QueueUpdate d2 = engine2.getQueue("D2");

            assertEquals(List.of("a"), d1.queueOrdered.stream().map(i -> i.imageCode).toList());
            assertEquals(List.of("b"), d2.queueOrdered.stream().map(i -> i.imageCode).toList());
        } finally {
            engine2.shutdown();
        }
    }

    @Test
    void remove_whenItemIsInDoctorQueue_removesAndPushesUpdate_returnsTrue() throws Exception {
        // Configuramos para esperar 2 señales: accept + remove
        LatchingSink sink = new LatchingSink(2);
        MessagePassingEngine engine = new MessagePassingEngine(new MPDoctorQueueManager(), sink);

        try {
            // 1) Aceptamos el mensaje
            engine.accept(new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO, 1L));

            // ELIMINAMOS EL ASSERTTRUE DE AQUÍ (porque el latch todavía vale 1 y daría
            // timeout)

            // 2) Ahora removemos (debería estar ya en la cola del doctor o en proceso)
            // Damos un pequeño margen para que el dispatcher procese el accept antes del
            // remove
            Thread.sleep(200);

            boolean removed = engine.remove("D1", "img1");
            assertTrue(removed, "El elemento debería haber sido eliminado");

            // 3) AHORA SÍ: Esperamos a que el latch llegue a 0 (las dos señales)
            assertTrue(sink.await(2, TimeUnit.SECONDS), "Expected initial push + remove push");

            // Verificamos estado final
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

            // Da un pequeño margen por si hubiese push inesperado
            Thread.sleep(150);

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

        // Tras shutdown, no garantizamos comportamiento de accept (tu código permite
        // accept),
        // pero sí garantizamos que el test no se cuelga y que no hay pushes.
        assertDoesNotThrow(() -> engine.accept(new PhotoMsg("x", "D1", PhotoMsg.Urgency.ALTO, 1L)));

        // Espera corta: no debería llegar nada
        assertFalse(sink.await(200, TimeUnit.MILLISECONDS));
    }
}
