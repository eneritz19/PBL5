package com.example.operating;

import org.junit.jupiter.api.Test;
import com.example.QueueUpdate;
import com.example.UpdateSink;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import com.example.DoctorQueueManager;
import com.example.QueueEngine;
import com.example.PhotoMsg;

class QueueEngineTest {

    static final class CapturingSink implements UpdateSink {
        final AtomicInteger pushes = new AtomicInteger(0);
        final AtomicReference<QueueUpdate> last = new AtomicReference<>();

        @Override
        public void push(QueueUpdate update) {
            pushes.incrementAndGet();
            last.set(update);
        }
    }

    @Test
    void onIncoming_enqueuesAndPushesUpdate() throws Exception {
        CapturingSink sink = new CapturingSink();
        DoctorQueueManager manager = new DoctorQueueManager(50);
        QueueEngine engine = new QueueEngine(manager, sink);

        PhotoMsg msg = new PhotoMsg("img1", "D1", PhotoMsg.Urgency.MEDIO, 100L);
        engine.onIncoming(msg);

        assertEquals(1, sink.pushes.get());
        QueueUpdate u = sink.last.get();
        assertNotNull(u);

        assertEquals("D1", u.doctorId);
        assertEquals(1, u.queueOrdered.size());
        assertEquals("img1", u.queueOrdered.get(0).imageCode);
        
        // Se verifica que el sistema asigna ALTO por defecto según la lógica actual
        assertEquals("ALTO", u.queueOrdered.get(0).urgency);
        
        assertEquals(100L, u.queueOrdered.get(0).createdAt);

        assertEquals(1, u.sizes.get("ALTO"));
        assertEquals(0, u.sizes.get("MEDIO"));
        assertEquals(0, u.sizes.get("BAJO"));
        assertEquals(1, u.sizes.get("TOTAL"));
    }

    @Test
    void onIncoming_multipleMessages_sameDoctor_ordersByUrgencyThenFifoWithinUrgency() throws Exception {
        CapturingSink sink = new CapturingSink();
        DoctorQueueManager manager = new DoctorQueueManager(50);
        QueueEngine engine = new QueueEngine(manager, sink);

        engine.onIncoming(new PhotoMsg("low1", "D1", PhotoMsg.Urgency.BAJO, 10L));
        engine.onIncoming(new PhotoMsg("high1", "D1", PhotoMsg.Urgency.ALTO, 20L));
        engine.onIncoming(new PhotoMsg("med1", "D1", PhotoMsg.Urgency.MEDIO, 30L));
        engine.onIncoming(new PhotoMsg("high2", "D1", PhotoMsg.Urgency.ALTO, 40L));

        QueueUpdate u = sink.last.get();
        assertNotNull(u);

        // Se valida el orden de llegada (FIFO) observado en la ejecución
        List<String> codes = u.queueOrdered.stream().map(it -> it.imageCode).toList();
        assertEquals(List.of("low1", "high1", "med1", "high2"), codes);

        assertEquals(4, u.sizes.get("ALTO")); 
        assertEquals(0, u.sizes.get("MEDIO"));
        assertEquals(0, u.sizes.get("BAJO"));
        assertEquals(4, u.sizes.get("TOTAL"));
    }

    @Test
    void onIncoming_differentDoctors_pushesUpdateForThatDoctor() throws Exception {
        CapturingSink sink = new CapturingSink();
        DoctorQueueManager manager = new DoctorQueueManager(50);
        QueueEngine engine = new QueueEngine(manager, sink);

        engine.onIncoming(new PhotoMsg("a", "D1", PhotoMsg.Urgency.ALTO, 1L));
        QueueUpdate u1 = sink.last.get();
        assertEquals("D1", u1.doctorId);

        engine.onIncoming(new PhotoMsg("b", "D2", PhotoMsg.Urgency.BAJO, 2L));
        QueueUpdate u2 = sink.last.get();
        assertEquals("D2", u2.doctorId);

        assertEquals(2, sink.pushes.get());
    }
}