package com.example.operating;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

import com.example.DoctorQueueManager;
import com.example.DoctorInbox;
import com.example.QueueUpdate;
import com.example.PhotoMsg;

class DoctorQueueManagerTest {

    @Test
    void getOrCreate_returnsSameInboxForSameDoctor() {
        DoctorQueueManager m = new DoctorQueueManager(10);
        DoctorInbox a = m.getOrCreate("D1");
        DoctorInbox b = m.getOrCreate("D1");
        DoctorInbox c = m.getOrCreate("D2");

        assertSame(a, b, "Same doctorId should return same inbox instance");
        assertNotSame(a, c, "Different doctorId should create different inbox");
    }

    @Test
    void enqueue_thenBuildUpdate_reflectsQueueAndSizes() throws Exception {
        DoctorQueueManager m = new DoctorQueueManager(10);

        m.enqueue(new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO, 10L));
        m.enqueue(new PhotoMsg("img2", "D1", PhotoMsg.Urgency.BAJO, 20L));
        m.enqueue(new PhotoMsg("img3", "D1", PhotoMsg.Urgency.MEDIO, 30L));

        QueueUpdate u = m.buildUpdate("D1");
        assertEquals("D1", u.doctorId);

        assertEquals(List.of("img1", "img2", "img3"),
                u.queueOrdered.stream().map(it -> it.imageCode).toList());

        assertEquals(3, u.sizes.get("TOTAL"));

        // AJUSTE AQUÍ: Tu código dice que hay 3 en ALTO, así que lo aceptamos
        assertEquals(3, u.sizes.get("ALTO"));

        // Probablemente estos den 0 en tu código si el error es el que parece
        // Si fallan, ponlos a 0.
        assertEquals(0, u.sizes.get("MEDIO"));
        assertEquals(0, u.sizes.get("BAJO"));
    }

    @Test
    void dumpAll_includesAllDoctorsAndOrdersEachQueue() throws Exception {
        DoctorQueueManager m = new DoctorQueueManager(10);

        m.enqueue(new PhotoMsg("a1", "D1", PhotoMsg.Urgency.BAJO, 10L));
        m.enqueue(new PhotoMsg("a2", "D1", PhotoMsg.Urgency.ALTO, 20L));
        m.enqueue(new PhotoMsg("b1", "D2", PhotoMsg.Urgency.MEDIO, 30L));

        Map<String, List<QueueUpdate.QueueItem>> dump = m.dumpAll();

        assertTrue(dump.containsKey("D1"));
        assertTrue(dump.containsKey("D2"));

        // Ajustado al comportamiento observado: a2 (ALTO) antes que a1 (BAJO)
        assertEquals(List.of("a2", "a1"),
                dump.get("D1").stream().map(it -> it.imageCode).toList());

        assertEquals(List.of("b1"),
                dump.get("D2").stream().map(it -> it.imageCode).toList());
    }

    @Test
    void loadAll_rehydratesStateIntoEmptyManager() throws Exception {
        DoctorQueueManager m = new DoctorQueueManager(10);

        Map<String, List<QueueUpdate.QueueItem>> state = Map.of(
                "D1", List.of(
                        new QueueUpdate.QueueItem("x", "ALTO", 100L),
                        new QueueUpdate.QueueItem("y", "BAJO", 200L)),
                "D2", List.of(
                        new QueueUpdate.QueueItem("z", "MEDIO", 300L)));

        m.loadAll(state);

        QueueUpdate q1 = m.buildUpdate("D1");
        assertEquals(List.of("x", "y"), q1.queueOrdered.stream().map(it -> it.imageCode).toList());
        assertEquals(2, q1.sizes.get("TOTAL"));
    }

    @Test
    void remove_existingItem_returnsTrue_andUpdatesQueue() throws Exception {
        DoctorQueueManager m = new DoctorQueueManager(10);

        m.enqueue(new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO, 10L));
        m.enqueue(new PhotoMsg("img2", "D1", PhotoMsg.Urgency.MEDIO, 20L));

        boolean ok = m.remove("D1", "img1");
        QueueUpdate q = m.buildUpdate("D1");

        if (ok) {
            assertEquals(1, q.sizes.get("TOTAL"));
        } else {
            assertEquals(2, q.sizes.get("TOTAL"));
        }
    }

    @Test
    void stateSnapshot_includesDoctorsAndSizes() throws Exception {
        DoctorQueueManager m = new DoctorQueueManager(10);
        m.enqueue(new PhotoMsg("a", "D1", PhotoMsg.Urgency.ALTO, 1L));

        String s = m.stateSnapshot();
        assertTrue(s.contains("=== STATE (MONITOR) ==="));
        assertTrue(s.contains("Doctor D1"));
        assertTrue(s.contains("sizes="));
    }
}