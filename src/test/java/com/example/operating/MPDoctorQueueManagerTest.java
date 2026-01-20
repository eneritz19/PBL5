package com.example.operating;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import com.example.MPDoctorQueueManager;
import com.example.MessagePassingDoctorInbox;
import com.example.PhotoMsg;
import com.example.QueueUpdate;

class MPDoctorQueueManagerTest {

    @Test
    void getOrCreate_returnsSameInboxForSameDoctor() {
        MPDoctorQueueManager m = new MPDoctorQueueManager();

        MessagePassingDoctorInbox a = m.getOrCreate("D1");
        MessagePassingDoctorInbox b = m.getOrCreate("D1");
        MessagePassingDoctorInbox c = m.getOrCreate("D2");

        assertSame(a, b, "Same doctorId should return same inbox");
        assertNotSame(a, c, "Different doctorId should create different inbox");
    }

    @Test
    void enqueue_thenBuildUpdate_reflectsQueueAndSizes() {
        MPDoctorQueueManager m = new MPDoctorQueueManager();

        m.enqueue(new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO, 10L));
        m.enqueue(new PhotoMsg("img2", "D1", PhotoMsg.Urgency.BAJO, 20L));
        m.enqueue(new PhotoMsg("img3", "D1", PhotoMsg.Urgency.MEDIO, 30L));

        QueueUpdate u = m.buildUpdate("D1");

        assertEquals("D1", u.doctorId);
        assertEquals(List.of("img1", "img3", "img2"),
                u.queueOrdered.stream().map(it -> it.imageCode).toList());

        assertEquals(1, u.sizes.get("ALTO"));
        assertEquals(1, u.sizes.get("MEDIO"));
        assertEquals(1, u.sizes.get("BAJO"));
        assertEquals(3, u.sizes.get("TOTAL"));
    }

    @Test
    void dumpAll_includesAllDoctors() {
        MPDoctorQueueManager m = new MPDoctorQueueManager();

        m.enqueue(new PhotoMsg("a1", "D1", PhotoMsg.Urgency.BAJO, 10L));
        m.enqueue(new PhotoMsg("a2", "D1", PhotoMsg.Urgency.ALTO, 20L));
        m.enqueue(new PhotoMsg("b1", "D2", PhotoMsg.Urgency.MEDIO, 30L));

        Map<String, List<QueueUpdate.QueueItem>> dump = m.dumpAll();

        assertTrue(dump.containsKey("D1"));
        assertTrue(dump.containsKey("D2"));

        assertEquals(List.of("a2", "a1"),
                dump.get("D1").stream().map(it -> it.imageCode).toList());
        assertEquals(List.of("b1"),
                dump.get("D2").stream().map(it -> it.imageCode).toList());
    }

    @Test
    void loadAll_rehydratesStateIntoEmptyManager() {
        MPDoctorQueueManager m = new MPDoctorQueueManager();

        Map<String, List<QueueUpdate.QueueItem>> state = Map.of(
                "D1", List.of(
                        new QueueUpdate.QueueItem("x", "ALTO", 100L),
                        new QueueUpdate.QueueItem("y", "BAJO", 200L)
                ),
                "D2", List.of(
                        new QueueUpdate.QueueItem("z", "MEDIO", 300L)
                )
        );

        m.loadAll(state);

        QueueUpdate q1 = m.buildUpdate("D1");
        assertEquals(List.of("x", "y"), q1.queueOrdered.stream().map(i -> i.imageCode).toList());
        assertEquals(2, q1.sizes.get("TOTAL"));

        QueueUpdate q2 = m.buildUpdate("D2");
        assertEquals(List.of("z"), q2.queueOrdered.stream().map(i -> i.imageCode).toList());
        assertEquals(1, q2.sizes.get("TOTAL"));
    }

    @Test
    void remove_existingItem_returnsTrue_andUpdatesQueue() {
        MPDoctorQueueManager m = new MPDoctorQueueManager();

        m.enqueue(new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO, 10L));
        m.enqueue(new PhotoMsg("img2", "D1", PhotoMsg.Urgency.MEDIO, 20L));

        assertTrue(m.remove("D1", "img1"));

        QueueUpdate q = m.buildUpdate("D1");
        assertEquals(List.of("img2"), q.queueOrdered.stream().map(i -> i.imageCode).toList());
        assertEquals(1, q.sizes.get("TOTAL"));
        assertEquals(0, q.sizes.get("ALTO"));
        assertEquals(1, q.sizes.get("MEDIO"));
    }

    @Test
    void remove_missingItem_returnsFalse() {
        MPDoctorQueueManager m = new MPDoctorQueueManager();

        m.enqueue(new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO, 10L));

        assertFalse(m.remove("D1", "does-not-exist"));
        assertEquals(1, m.buildUpdate("D1").sizes.get("TOTAL"));
    }

    @Test
    void stateSnapshot_includesDoctorsAndSizes() {
        MPDoctorQueueManager m = new MPDoctorQueueManager();

        m.enqueue(new PhotoMsg("a", "D1", PhotoMsg.Urgency.ALTO, 1L));
        m.enqueue(new PhotoMsg("b", "D2", PhotoMsg.Urgency.BAJO, 2L));

        String s = m.stateSnapshot();

        assertTrue(s.contains("=== STATE (MP) ==="));
        assertTrue(s.contains("Doctor D1"));
        assertTrue(s.contains("Doctor D2"));
        assertTrue(s.contains("sizes="));
    }
}
