package com.example.operating;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import com.example.MessagePassingDoctorInbox;
import com.example.QueueUpdate;
import com.example.PhotoMsg;

class MessagePassingDoctorInboxTest {

    @Test
    void enqueue_and_snapshotOrdered_ordersByUrgencyThenCreatedAt() {
        MessagePassingDoctorInbox inbox = new MessagePassingDoctorInbox("D1");

        // Mezcla, y con timestamps para ordenar determinísticamente
        inbox.enqueue(new PhotoMsg("low2", "D1", PhotoMsg.Urgency.BAJO, 30L));
        inbox.enqueue(new PhotoMsg("high2", "D1", PhotoMsg.Urgency.ALTO, 40L));
        inbox.enqueue(new PhotoMsg("med1", "D1", PhotoMsg.Urgency.MEDIO, 10L));
        inbox.enqueue(new PhotoMsg("high1", "D1", PhotoMsg.Urgency.ALTO, 20L));
        inbox.enqueue(new PhotoMsg("low1", "D1", PhotoMsg.Urgency.BAJO, 5L));

        List<QueueUpdate.QueueItem> ordered = inbox.snapshotOrdered();
        List<String> codes = ordered.stream().map(i -> i.imageCode).toList();

        // Orden: ALTO (createdAt asc) -> MEDIO -> BAJO
        assertEquals(List.of("high1", "high2", "med1", "low1", "low2"), codes);

        // En snapshotOrdered, urgency sale como name() del enum original
        assertEquals("ALTO", ordered.get(0).urgency);
        assertEquals("ALTO", ordered.get(1).urgency);
        assertEquals("MEDIO", ordered.get(2).urgency);
        assertEquals("BAJO", ordered.get(3).urgency);
        assertEquals("BAJO", ordered.get(4).urgency);
    }

    @Test
    void sizesSnapshot_countsCorrectly() {
        MessagePassingDoctorInbox inbox = new MessagePassingDoctorInbox("D1");

        inbox.enqueue(new PhotoMsg("a", "D1", PhotoMsg.Urgency.ALTO, 1L));
        inbox.enqueue(new PhotoMsg("b", "D1", PhotoMsg.Urgency.MEDIO, 2L));
        inbox.enqueue(new PhotoMsg("c", "D1", PhotoMsg.Urgency.BAJO, 3L));
        inbox.enqueue(new PhotoMsg("d", "D1", PhotoMsg.Urgency.BAJO, 4L));

        Map<String, Integer> sizes = inbox.sizesSnapshot();

        assertEquals(1, sizes.get("ALTO"));
        assertEquals(1, sizes.get("MEDIO"));
        assertEquals(2, sizes.get("BAJO"));
        assertEquals(4, sizes.get("TOTAL"));
    }

    @Test
    void removeByImageCode_removesMatchingAndReturnsTrue() {
        MessagePassingDoctorInbox inbox = new MessagePassingDoctorInbox("D1");

        inbox.enqueue(new PhotoMsg("x", "D1", PhotoMsg.Urgency.ALTO, 1L));
        inbox.enqueue(new PhotoMsg("y", "D1", PhotoMsg.Urgency.BAJO, 2L));

        assertTrue(inbox.removeByImageCode("y"));

        List<String> codes = inbox.snapshotOrdered().stream().map(i -> i.imageCode).toList();
        assertEquals(List.of("x"), codes);

        Map<String, Integer> sizes = inbox.sizesSnapshot();
        assertEquals(1, sizes.get("TOTAL"));
        assertEquals(0, sizes.get("BAJO"));
    }

    @Test
    void removeByImageCode_missingReturnsFalse() {
        MessagePassingDoctorInbox inbox = new MessagePassingDoctorInbox("D1");

        inbox.enqueue(new PhotoMsg("x", "D1", PhotoMsg.Urgency.ALTO, 1L));

        assertFalse(inbox.removeByImageCode("nope"));
        assertEquals(1, inbox.sizesSnapshot().get("TOTAL"));
    }

    @Test
    void doctorId_returnsProvidedDoctorId() {
        MessagePassingDoctorInbox inbox = new MessagePassingDoctorInbox("D99");
        assertEquals("D99", inbox.doctorId());
    }
}
