package edu.mondragon.os.monitors.skinxpert;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityDoctorInbox implements DoctorInbox {
    private final String doctorId;

    private final Semaphore slots; // huecos
    private final Semaphore items; // items

    private final ReentrantLock lock = new ReentrantLock();

    private final Deque<PhotoMsg> high = new ArrayDeque<>();
    private final Deque<PhotoMsg> med  = new ArrayDeque<>();
    private final Deque<PhotoMsg> low  = new ArrayDeque<>();

    public PriorityDoctorInbox(String doctorId, int capacity) {
        this.doctorId = doctorId;
        this.slots = new Semaphore(capacity, true);
        this.items = new Semaphore(0, true);
    }

    @Override
    public void enqueue(PhotoMsg msg) throws InterruptedException {
        // Capacidad: evita overflow sin busy-wait
        slots.acquire();

        lock.lock();
        try {
            switch (msg.urgency) {
                case ALTO -> high.addLast(msg);
                case MEDIO -> med.addLast(msg);
                case BAJO -> low.addLast(msg);
            }
        } finally {
            lock.unlock();
        }

        items.release();
    }

    // (Opcional) si más adelante quieres "sacar siguiente" para médico:
    public PhotoMsg takeNext() throws InterruptedException {
        items.acquire();
        lock.lock();
        try {
            PhotoMsg msg;
            if (!high.isEmpty()) msg = high.removeFirst();
            else if (!med.isEmpty()) msg = med.removeFirst();
            else msg = low.removeFirst();
            slots.release();
            return msg;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<QueueUpdate.QueueItem> snapshotOrdered() {
        lock.lock();
        try {
            ArrayList<QueueUpdate.QueueItem> out = new ArrayList<>(high.size() + med.size() + low.size());
            for (PhotoMsg m : high) out.add(new QueueUpdate.QueueItem(m.imageCode, m.urgency.name(), m.createdAtMillis));
            for (PhotoMsg m : med)  out.add(new QueueUpdate.QueueItem(m.imageCode, m.urgency.name(), m.createdAtMillis));
            for (PhotoMsg m : low)  out.add(new QueueUpdate.QueueItem(m.imageCode, m.urgency.name(), m.createdAtMillis));
            return out;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<String, Integer> sizesSnapshot() {
        lock.lock();
        try {
            int h = high.size(), m = med.size(), l = low.size();
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("ALTO", h);
            map.put("MEDIO", m);
            map.put("BAJO", l);
            map.put("TOTAL", h + m + l);
            return map;
        } finally {
            lock.unlock();
        }
    }
}
