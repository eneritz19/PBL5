package com.example.operating;
import org.junit.jupiter.api.Test;

import com.example.QueueUpdate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueueUpdateTest {

    @Test
    void constructor_storesAllFields() {
        List<QueueUpdate.QueueItem> queue = List.of(
                new QueueUpdate.QueueItem("img1", "ALTO", 10L),
                new QueueUpdate.QueueItem("img2", "BAJO", 20L)
        );

        Map<String, Integer> sizes = new LinkedHashMap<>();
        sizes.put("ALTO", 1);
        sizes.put("MEDIO", 0);
        sizes.put("BAJO", 1);
        sizes.put("TOTAL", 2);

        QueueUpdate u = new QueueUpdate("D1", queue, sizes);

        assertEquals("D1", u.doctorId);
        assertSame(queue, u.queueOrdered, "Should keep the same list reference (no copy expected)");
        assertSame(sizes, u.sizes, "Should keep the same map reference (no copy expected)");
        assertEquals(2, u.queueOrdered.size());
        assertEquals(2, u.sizes.get("TOTAL"));
    }

    @Test
    void queueItem_storesAllFields() {
        QueueUpdate.QueueItem item = new QueueUpdate.QueueItem("x.jpg", "MEDIO", 999L);

        assertEquals("x.jpg", item.imageCode);
        assertEquals("MEDIO", item.urgency);
        assertEquals(999L, item.createdAt);
    }

    @Test
    void constructor_allowsEmptyCollections() {
        QueueUpdate u = new QueueUpdate("D2", List.of(), Map.of());

        assertEquals("D2", u.doctorId);
        assertNotNull(u.queueOrdered);
        assertNotNull(u.sizes);
        assertTrue(u.queueOrdered.isEmpty());
        assertTrue(u.sizes.isEmpty());
    }

    @Test
    void fields_areEffectivelyImmutablePublicFinalButUnderlyingCollectionsCanMutate() {
        List<QueueUpdate.QueueItem> queue = new ArrayList<>();
        Map<String, Integer> sizes = new LinkedHashMap<>();

        QueueUpdate u = new QueueUpdate("D3", queue, sizes);

        queue.add(new QueueUpdate.QueueItem("img", "ALTO", 1L));
        sizes.put("TOTAL", 1);

        assertEquals(1, u.queueOrdered.size(), "Because no defensive copy is made, changes are reflected");
        assertEquals(1, u.sizes.get("TOTAL"));
    }
}
