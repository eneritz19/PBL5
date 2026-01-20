package com.example;

import java.util.List;
import java.util.Map;

public final class QueueUpdate {
    public final String doctorId;
    public final List<QueueItem> queueOrdered; // HIGH->MED->LOW (FIFO por urgencia)
    public final Map<String, Integer> sizes;   // ALTO/MEDIO/BAJO/TOTAL

    public QueueUpdate(String doctorId, List<QueueItem> queueOrdered, Map<String, Integer> sizes) {
        this.doctorId = doctorId;
        this.queueOrdered = queueOrdered;
        this.sizes = sizes;
    }

    public static final class QueueItem {
        public final String imageCode;
        public final String urgency;
        public final long createdAt;

        public QueueItem(String imageCode, String urgency, long createdAt) {
            this.imageCode = imageCode;
            this.urgency = urgency;
            this.createdAt = createdAt;
        }
    }
}
