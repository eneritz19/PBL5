package com.example;
import java.util.List;
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

    public Map<String, List<QueueUpdate.QueueItem>> dumpAll() {
        Map<String, List<QueueUpdate.QueueItem>> out = new java.util.LinkedHashMap<>();
        for (var e : inboxes.entrySet()) {
            out.put(e.getKey(), e.getValue().snapshotOrdered());
        }
        return out;
    }

    public void loadAll(Map<String, List<QueueUpdate.QueueItem>> state) {
        for (var e : state.entrySet()) {
            String doctorId = e.getKey();
            for (var qi : e.getValue()) {
                PhotoMsg.Urgency u = PhotoMsg.Urgency.valueOf(qi.urgency);
                PhotoMsg msg = new PhotoMsg(qi.imageCode, doctorId, u, qi.createdAt);
                getOrCreate(doctorId).enqueue(msg);
            }
        }
    }

    public boolean remove(String doctorId, String imageCode) {
        return getOrCreate(doctorId).removeByImageCode(imageCode);
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