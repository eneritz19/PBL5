package com.example;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessagePassingEngine implements Engine {
    private final MPDoctorQueueManager manager;
    private final UpdateSink sink;

    private final BlockingQueue<PhotoMsg> incomingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<QueueUpdate> updatesQueue = new LinkedBlockingQueue<>();

    private final Thread dispatcher;
    private final Thread pusher;

    private volatile boolean running = true;

    public MessagePassingEngine(MPDoctorQueueManager manager, UpdateSink sink) {
        this.manager = manager;
        this.sink = sink;

        this.dispatcher = new Thread(this::dispatchLoop, "MP-Dispatcher");
        this.pusher = new Thread(this::pushLoop, "MP-Pusher");

        dispatcher.start();
        pusher.start();
    }

    @Override
    public void accept(PhotoMsg msg) throws InterruptedException {
        incomingQueue.put(msg);
    }

    private void dispatchLoop() {
        while (running) {
            try {
                PhotoMsg msg = incomingQueue.take();
                manager.enqueue(msg);

                QueueUpdate update = manager.buildUpdate(msg.doctorId);
                updatesQueue.put(update);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                System.err.println("[MP-Dispatcher] Error: " + ex.getMessage());
            }
        }
    }

    private void pushLoop() {
        while (running) {
            try {
                QueueUpdate update = updatesQueue.take();
                sink.push(update);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                System.err.println("[MP-Pusher] Error: " + ex.getMessage());
            }
        }
    }

    @Override
    public String state() {
        return manager.stateSnapshot();
    }

    @Override
    public void shutdown() {
        running = false;
        dispatcher.interrupt();
        pusher.interrupt();
    }

    @Override
    public QueueUpdate getQueue(String doctorId) {
        return manager.buildUpdate(doctorId);
    }

    @Override
    public Map<String, List<QueueUpdate.QueueItem>> dumpAll() {
        return manager.dumpAll();
    }

    @Override
    public void loadAll(Map<String, List<QueueUpdate.QueueItem>> state) {
        manager.loadAll(state);
    }

    @Override
    public boolean remove(String doctorId, String imageCode) throws InterruptedException {
        // 1) eliminar si aún estaba esperando en incomingQueue
        incomingQueue.removeIf(m -> m.doctorId.equals(doctorId) && m.imageCode.equals(imageCode));

        // 2) eliminar de la cola del doctor
        boolean ok = manager.remove(doctorId, imageCode);

        // 3) push update si se eliminó
        if (ok) {
            updatesQueue.put(manager.buildUpdate(doctorId));
        }
        return ok;
    }
}
