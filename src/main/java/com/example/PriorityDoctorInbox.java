package com.example;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PriorityDoctorInbox implements DoctorInbox {
    private static final Logger LOGGER = Logger.getLogger(PriorityDoctorInbox.class.getName());
    private final String doctorId;

    private final Semaphore slots; // huecos
    private final Semaphore items; // items

    private final ReentrantLock lock = new ReentrantLock();

    private final Deque<PhotoMsg> high = new ArrayDeque<>();
    private final Deque<PhotoMsg> med  = new ArrayDeque<>();
    private final Deque<PhotoMsg> low  = new ArrayDeque<>();

    private static final long MIN = 60_000L; 
    private static final long T_LOW_TO_MED_MS  = 15 * MIN; 
    private static final long T_MED_TO_HIGH_MS = 45 * MIN; 
    private static final long T_LOW_TO_HIGH_MS = 120 * MIN; 

    public PriorityDoctorInbox(String doctorId, int capacity) {
        this.doctorId = doctorId;
        this.slots = new Semaphore(capacity, true);
        this.items = new Semaphore(0, true);
    }

    @Override
    public void enqueue(PhotoMsg msg) throws InterruptedException {
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

    private static PhotoMsg.Urgency effectiveUrgency(PhotoMsg msg, long nowMillis) {
        long wait = nowMillis - msg.createdAtMillis;

        if (msg.urgency == PhotoMsg.Urgency.BAJO) {
            if (wait >= T_LOW_TO_HIGH_MS) return PhotoMsg.Urgency.ALTO;
            if (wait >= T_LOW_TO_MED_MS)  return PhotoMsg.Urgency.MEDIO;
            return PhotoMsg.Urgency.BAJO;
        }

        if (msg.urgency == PhotoMsg.Urgency.MEDIO && wait >= T_MED_TO_HIGH_MS) {
            return PhotoMsg.Urgency.ALTO;
        }

        return msg.urgency == PhotoMsg.Urgency.MEDIO ? PhotoMsg.Urgency.MEDIO : PhotoMsg.Urgency.ALTO;
    }

    private void applyAgingLocked() {
        long now = System.currentTimeMillis();
        processLowAging(now);
        processMedAging(now);
    }

    private void processLowAging(long now) {
        Iterator<PhotoMsg> it = low.iterator();
        while (it.hasNext()) {
            PhotoMsg m = it.next();
            PhotoMsg.Urgency eff = effectiveUrgency(m, now);
            if (eff != PhotoMsg.Urgency.BAJO) {
                it.remove();
                if (eff == PhotoMsg.Urgency.MEDIO) med.addLast(m);
                else high.addLast(m);
            }
        }
    }

    private void processMedAging(long now) {
        Iterator<PhotoMsg> it = med.iterator();
        while (it.hasNext()) {
            PhotoMsg m = it.next();
            if (effectiveUrgency(m, now) == PhotoMsg.Urgency.ALTO) {
                it.remove();
                high.addLast(m);
            }
        }
    }

    public PhotoMsg takeNext() throws InterruptedException {
        items.acquire();
        lock.lock();
        try {
            applyAgingLocked();
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
            applyAgingLocked();
            ArrayList<QueueUpdate.QueueItem> out = new ArrayList<>(high.size() + med.size() + low.size());
            for (PhotoMsg m : high) out.add(new QueueUpdate.QueueItem(m.imageCode, PhotoMsg.Urgency.ALTO.name(),  m.createdAtMillis));
            for (PhotoMsg m : med)  out.add(new QueueUpdate.QueueItem(m.imageCode, PhotoMsg.Urgency.MEDIO.name(), m.createdAtMillis));
            for (PhotoMsg m : low)  out.add(new QueueUpdate.QueueItem(m.imageCode, PhotoMsg.Urgency.BAJO.name(),  m.createdAtMillis));
            return out;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<String, Integer> sizesSnapshot() {
        lock.lock();
        try {
            int h = high.size();
            int m = med.size();
            int l = low.size();
            
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

    @Override
    public boolean removeByImageCode(String imageCode) {
        lock.lock();
        try {
            boolean removed = removeFromDeque(high, imageCode) ||
                             removeFromDeque(med, imageCode)  ||
                             removeFromDeque(low, imageCode);

            if (removed) {
                if (!items.tryAcquire()) {
                    LOGGER.log(Level.WARNING, "No se pudo adquirir el permiso del ítem tras borrado manual");
                }
                slots.release();
            }
            return removed;
        } finally {
            lock.unlock();
        }
    }

    private boolean removeFromDeque(java.util.Deque<PhotoMsg> dq, String imageCode) {
        var it = dq.iterator();
        while (it.hasNext()) {
            PhotoMsg m = it.next();
            if (m.imageCode.equals(imageCode)) {
                it.remove();
                return true;
            }
        }
        return false;
    }
    
    public String getDoctorId() {
        return this.doctorId;
    }
}