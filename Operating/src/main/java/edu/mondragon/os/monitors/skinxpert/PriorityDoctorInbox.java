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

    // =========================================================================
    // AGING (evita starvation) - umbrales en minutos
    // =========================================================================
    private static final long MIN = 60_000L; // 1 minuto en milisegundos

    // Ajusta estos valores según criterio clínico / demo
    private static final long T_LOW_TO_MED_MS  = 10 * MIN; // BAJO -> MEDIO tras 10 min
    private static final long T_MED_TO_HIGH_MS = 20 * MIN; // MEDIO -> ALTO tras 20 min
    private static final long T_LOW_TO_HIGH_MS = 40 * MIN; // BAJO -> ALTO tras 40 min (opcional)

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

    /**
     * Urgencia "efectiva" según tiempo de espera (aging).
     * - BAJO puede subir a MEDIO o ALTO si espera demasiado
     * - MEDIO puede subir a ALTO si espera demasiado
     */
    private static PhotoMsg.Urgency effectiveUrgency(PhotoMsg msg, long nowMillis) {
        long wait = nowMillis - msg.createdAtMillis;

        if (msg.urgency == PhotoMsg.Urgency.BAJO) {
            if (wait >= T_LOW_TO_HIGH_MS) return PhotoMsg.Urgency.ALTO;
            if (wait >= T_LOW_TO_MED_MS)  return PhotoMsg.Urgency.MEDIO;
            return PhotoMsg.Urgency.BAJO;
        }

        if (msg.urgency == PhotoMsg.Urgency.MEDIO) {
            if (wait >= T_MED_TO_HIGH_MS) return PhotoMsg.Urgency.ALTO;
            return PhotoMsg.Urgency.MEDIO;
        }

        return PhotoMsg.Urgency.ALTO;
    }

    /**
     * Aplica aging moviendo mensajes entre colas.
     * Debe llamarse siempre con el lock cogido.
     */
    private void applyAgingLocked() {
        long now = System.currentTimeMillis();

        // Revisa BAJO -> MEDIO/ALTO
        if (!low.isEmpty()) {
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

        // Revisa MEDIO -> ALTO
        if (!med.isEmpty()) {
            Iterator<PhotoMsg> it = med.iterator();
            while (it.hasNext()) {
                PhotoMsg m = it.next();
                PhotoMsg.Urgency eff = effectiveUrgency(m, now);
                if (eff == PhotoMsg.Urgency.ALTO) {
                    it.remove();
                    high.addLast(m);
                }
            }
        }
    }

    // (Opcional) si más adelante quieres "sacar siguiente" para médico:
    public PhotoMsg takeNext() throws InterruptedException {
        items.acquire();
        lock.lock();
        try {
            // ✅ Evita starvation: antes de elegir, promovemos por antigüedad
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
            // ✅ El snapshot refleja la prioridad efectiva (sin starvation)
            applyAgingLocked();

            ArrayList<QueueUpdate.QueueItem> out = new ArrayList<>(high.size() + med.size() + low.size());

            // ✅ Urgencia efectiva: al estar en "high/med/low" ya es su prioridad actual
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
            // Si quieres que los contadores también reflejen aging, descomenta:
            // applyAgingLocked();

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