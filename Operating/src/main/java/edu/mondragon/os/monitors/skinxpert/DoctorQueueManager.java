package edu.mondragon.os.monitors.skinxpert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DoctorQueueManager {
    private final Map<String, DoctorInbox> inboxes = new ConcurrentHashMap<>();
    private final int perDoctorCapacity;

    public DoctorQueueManager(int perDoctorCapacity) {
        this.perDoctorCapacity = perDoctorCapacity;
    }

    public DoctorInbox getOrCreate(String doctorId) {
        return inboxes.computeIfAbsent(doctorId, id -> new PriorityDoctorInbox(id, perDoctorCapacity));
    }

    public void enqueue(PhotoMsg msg) throws InterruptedException {
        getOrCreate(msg.doctorId).enqueue(msg);
    }

    public QueueUpdate buildUpdate(String doctorId) {
        DoctorInbox inbox = getOrCreate(doctorId);
        return new QueueUpdate(
                doctorId,
                inbox.snapshotOrdered(),
                inbox.sizesSnapshot()
        );
    }

    public String stateSnapshot() {
    StringBuilder sb = new StringBuilder("=== STATE (MONITOR) ===\n");
    for (var e : inboxes.entrySet()) {
        sb.append("Doctor ").append(e.getKey())
          .append(" sizes=").append(e.getValue().sizesSnapshot())
          .append("\n");
    }
    return sb.toString();
}
}
