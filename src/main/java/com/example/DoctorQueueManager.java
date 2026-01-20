package com.example;
import java.util.List;
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

    public Map<String, List<QueueUpdate.QueueItem>> dumpAll() {
        Map<String, List<QueueUpdate.QueueItem>> out = new java.util.LinkedHashMap<>();
        for (var e : inboxes.entrySet()) {
            out.put(e.getKey(), e.getValue().snapshotOrdered());
        }
        return out;
    }

    public void loadAll(Map<String, List<QueueUpdate.QueueItem>> state) throws InterruptedException {
        for (var e : state.entrySet()) {
            String doctorId = e.getKey();
            for (var qi : e.getValue()) {
                PhotoMsg.Urgency u = PhotoMsg.Urgency.valueOf(qi.urgency);
                PhotoMsg msg = new PhotoMsg(qi.imageCode, doctorId, u, qi.createdAt);
                getOrCreate(doctorId).enqueue(msg);
            }
        }
    }

    public boolean remove(String doctorId, String imageCode) throws InterruptedException {
        return getOrCreate(doctorId).removeByImageCode(imageCode);
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
