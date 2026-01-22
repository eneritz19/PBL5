package com.example.operating;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import com.example.UpdateSink;
import com.example.PhotoMsg;
import com.example.QueueUpdate;
import com.example.ConsoleUpdateSink;
import com.example.DoctorQueueManager;
import com.example.MonitorEngine;

class MonitorEngineTest {

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
    void accept_enqueuesAndPushesUpdate() throws Exception {
        CapturingSink sink = new CapturingSink();
        DoctorQueueManager manager = new DoctorQueueManager(50);
        MonitorEngine engine = new MonitorEngine(manager, sink);

        PhotoMsg msg = new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO, 10L);

        engine.accept(msg);

        assertEquals(1, sink.pushes.get());
        QueueUpdate u = sink.last.get();
        assertNotNull(u);

        assertEquals("D1", u.doctorId);
        assertEquals(List.of("img1"), u.queueOrdered.stream().map(i -> i.imageCode).toList());
    }

    @Test
    void getQueue_returnsRealOrderedQueue() throws Exception {
        CapturingSink sink = new CapturingSink();
        DoctorQueueManager manager = new DoctorQueueManager(50);
        MonitorEngine engine = new MonitorEngine(manager, sink);

        engine.accept(new PhotoMsg("low", "D1", PhotoMsg.Urgency.BAJO, 1L));
        engine.accept(new PhotoMsg("high", "D1", PhotoMsg.Urgency.ALTO, 2L));

        QueueUpdate q = engine.getQueue("D1");

        assertEquals("D1", q.doctorId);

        assertEquals(List.of("low", "high"), q.queueOrdered.stream().map(i -> i.imageCode).toList());

        assertEquals(2, q.sizes.get("TOTAL"));
    }

    @Test
    void remove_whenRemoved_pushesUpdate_andReturnsTrue() throws Exception {
        CapturingSink sink = new CapturingSink();
        DoctorQueueManager manager = new DoctorQueueManager(50);
        MonitorEngine engine = new MonitorEngine(manager, sink);

        engine.accept(new PhotoMsg("a", "D1", PhotoMsg.Urgency.ALTO, 1L));

        boolean removed = engine.remove("D1", "a");

        assertTrue(removed);
    }

    @Test
    void testMonitorExtraPaths() throws InterruptedException {
        MonitorEngine engine = new MonitorEngine(new DoctorQueueManager(10), new ConsoleUpdateSink());

        boolean removed = engine.remove("D99", "no-existe");
        assertFalse(removed);

        var dump = engine.dumpAll();
        assertNotNull(dump);
    }
}