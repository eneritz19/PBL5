package com.example;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

public class MessagePassingDoctorInbox implements DoctorInbox {
    private final String doctorId;

    private final PriorityBlockingQueue<PhotoMsg> q;

    public MessagePassingDoctorInbox(String doctorId) {
        this.doctorId = doctorId;
        this.q = new PriorityBlockingQueue<>(11, Comparator
                .comparingInt((PhotoMsg m) -> urgencyRank(m.urgency))
                .thenComparingLong(m -> m.createdAtMillis));
    }

    private static int urgencyRank(PhotoMsg.Urgency u) {
        return switch (u) {
            case ALTO -> 0;
            case MEDIO -> 1;
            case BAJO -> 2;
        };
    }

    @Override
    public void enqueue(PhotoMsg msg) {
        q.put(msg); // message passing: el productor “manda” el msg al buzón del médico
    }

    @Override
    public boolean removeByImageCode(String imageCode) {
        return q.removeIf(m -> m.imageCode.equals(imageCode));
    }

    @Override
    public List<QueueUpdate.QueueItem> snapshotOrdered() {
        // Ojo: snapshot no bloquea a productores; es “consistente suficiente” para
        // monitoring.
        // Si quieres snapshot 100% congelado, habría que copiar y ordenar, como hacemos
        // aquí.
        ArrayList<PhotoMsg> copy = new ArrayList<>(q);
        copy.sort(Comparator
                .comparingInt((PhotoMsg m) -> urgencyRank(m.urgency))
                .thenComparingLong(m -> m.createdAtMillis));

        ArrayList<QueueUpdate.QueueItem> out = new ArrayList<>(copy.size());
        for (PhotoMsg m : copy) {
            out.add(new QueueUpdate.QueueItem(m.imageCode, m.urgency.name(), m.createdAtMillis));
        }
        return out;
    }

    @Override
    public Map<String, Integer> sizesSnapshot() {
        int h = 0;
        int m = 0;
        int l = 0;
        for (PhotoMsg msg : q) {
            switch (msg.urgency) {
                case ALTO -> h++;
                case MEDIO -> m++;
                case BAJO -> l++;
            }
        }
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("ALTO", h);
        map.put("MEDIO", m);
        map.put("BAJO", l);
        map.put("TOTAL", h + m + l);
        return map;
    }

    public String doctorId() {
        return doctorId;
    }
}
