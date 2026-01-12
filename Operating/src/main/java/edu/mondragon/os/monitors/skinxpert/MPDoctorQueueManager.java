package edu.mondragon.os.monitors.skinxpert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MPDoctorQueueManager {
    private final Map<String, MessagePassingDoctorInbox> inboxes = new ConcurrentHashMap<>();

    public MessagePassingDoctorInbox getOrCreate(String doctorId) {
        return inboxes.computeIfAbsent(doctorId, MessagePassingDoctorInbox::new);
    }

    public void enqueue(PhotoMsg msg) {
        getOrCreate(msg.doctorId).enqueue(msg);
    }

    public QueueUpdate buildUpdate(String doctorId) {
        MessagePassingDoctorInbox inbox = getOrCreate(doctorId);
        return new QueueUpdate(doctorId, inbox.snapshotOrdered(), inbox.sizesSnapshot());
    }

    public String stateSnapshot() {
        StringBuilder sb = new StringBuilder("=== STATE (MP) ===\n");
        for (var e : inboxes.entrySet()) {
            var inbox = e.getValue();
            sb.append("Doctor ").append(e.getKey())
              .append(" sizes=").append(inbox.sizesSnapshot())
              .append("\n");
        }
        return sb.toString();
    }
}

