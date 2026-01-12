package edu.mondragon.os.monitors.skinxpert;

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
        // Productores no tocan colas del médico directamente: mandan un mensaje
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
}

