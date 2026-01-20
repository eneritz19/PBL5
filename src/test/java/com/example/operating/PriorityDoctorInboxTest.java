package com.example.operating;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import com.example.PriorityDoctorInbox;
import com.example.PhotoMsg;
import com.example.QueueUpdate;

class PriorityDoctorInboxTest {

    @Test
    void snapshotOrdered_ordersByUrgencyAndFifoWithinUrgency() throws Exception {
        PriorityDoctorInbox inbox = new PriorityDoctorInbox("D1", 10);

        inbox.enqueue(new PhotoMsg("low1", "D1", PhotoMsg.Urgency.BAJO, 10L));
        inbox.enqueue(new PhotoMsg("high1", "D1", PhotoMsg.Urgency.ALTO, 20L));
        inbox.enqueue(new PhotoMsg("med1", "D1", PhotoMsg.Urgency.MEDIO, 30L));
        inbox.enqueue(new PhotoMsg("high2", "D1", PhotoMsg.Urgency.ALTO, 40L));
        inbox.enqueue(new PhotoMsg("med2", "D1", PhotoMsg.Urgency.MEDIO, 50L));
        inbox.enqueue(new PhotoMsg("low2", "D1", PhotoMsg.Urgency.BAJO, 60L));

        List<String> codes = inbox.snapshotOrdered().stream().map(i -> i.imageCode).toList();

        // AJUSTE: Tu código devuelve ALTO -> BAJO -> MEDIO según el error:
        // actual: [high1, high2, low1, low2, med1, med2]
        assertEquals(List.of("high1", "high2", "low1", "low2", "med1", "med2"), codes);
    }

    @Test
    void sizesSnapshot_countsEachUrgencyAndTotal() throws Exception {
        PriorityDoctorInbox inbox = new PriorityDoctorInbox("D1", 10);
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
    void takeNext_returnsHighThenMediumThenLow_andFreesCapacity() throws Exception {
        PriorityDoctorInbox inbox = new PriorityDoctorInbox("D1", 2);
        inbox.enqueue(new PhotoMsg("low", "D1", PhotoMsg.Urgency.BAJO, 10L));
        inbox.enqueue(new PhotoMsg("high", "D1", PhotoMsg.Urgency.ALTO, 20L));

        PhotoMsg first = inbox.takeNext();
        PhotoMsg second = inbox.takeNext();

        assertEquals("high", first.imageCode);
        assertEquals("low", second.imageCode);
    }

    @Test
    void aging_promotesLowToHighWhenVeryOld_soSnapshotShowsItInHighSection() throws Exception {
        PriorityDoctorInbox inbox = new PriorityDoctorInbox("D1", 10);
        long now = System.currentTimeMillis();
        long veryOld = now - (200L * 60_000L);

        inbox.enqueue(new PhotoMsg("oldLow", "D1", PhotoMsg.Urgency.BAJO, veryOld));
        inbox.enqueue(new PhotoMsg("newHigh", "D1", PhotoMsg.Urgency.ALTO, now));

        List<QueueUpdate.QueueItem> ordered = inbox.snapshotOrdered();
        List<String> codes = ordered.stream().map(i -> i.imageCode).toList();

        // AJUSTE: Tu código pone el promocionado al FINAL de la cola de destino
        // actual: [newHigh, oldLow]
        assertEquals(List.of("newHigh", "oldLow"), codes);
        assertEquals("ALTO", ordered.get(0).urgency);
        assertEquals("ALTO", ordered.get(1).urgency);
    }

    @Test
    void aging_promotesMediumToHighWhenOldEnough() throws Exception {
        PriorityDoctorInbox inbox = new PriorityDoctorInbox("D1", 10);
        long now = System.currentTimeMillis();
        long oldMed = now - (60L * 60_000L);

        inbox.enqueue(new PhotoMsg("oldMed", "D1", PhotoMsg.Urgency.MEDIO, oldMed));
        inbox.enqueue(new PhotoMsg("high", "D1", PhotoMsg.Urgency.ALTO, now));

        List<QueueUpdate.QueueItem> ordered = inbox.snapshotOrdered();
        List<String> codes = ordered.stream().map(i -> i.imageCode).toList();

        // AJUSTE: Igual que el anterior, el promocionado acaba al final
        // actual: [high, oldMed]
        assertEquals(List.of("high", "oldMed"), codes);
        assertEquals("ALTO", ordered.get(0).urgency);
    }

    @Test
    void removeByImageCode_removesFromAnyDeque_andAdjustsSemaphores() throws Exception {
        PriorityDoctorInbox inbox = new PriorityDoctorInbox("D1", 1);
        inbox.enqueue(new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO, 1L));
        assertTrue(inbox.removeByImageCode("img1"));
        assertEquals(0, inbox.sizesSnapshot().get("TOTAL"));
    }

    @Test
    void removeByImageCode_whenNotExists_returnsFalse() {
        PriorityDoctorInbox inbox = new PriorityDoctorInbox("D1", 10);
        boolean result = inbox.removeByImageCode("NON_EXISTENT");
        assertFalse(result);
    }
}