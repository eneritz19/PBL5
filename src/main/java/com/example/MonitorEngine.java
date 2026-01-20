package com.example;
import java.util.List;
import java.util.Map;

public class MonitorEngine implements Engine {
    private final DoctorQueueManager manager;
    private final UpdateSink sink;

    public MonitorEngine(DoctorQueueManager manager, UpdateSink sink) {
        this.manager = manager;
        this.sink = sink;
    }

    @Override
    public void accept(PhotoMsg msg) throws InterruptedException {
        manager.enqueue(msg);
        sink.push(manager.buildUpdate(msg.doctorId));
    }

    @Override
    public String state() {
        return manager.stateSnapshot();
    }

    @Override
    public void shutdown() { }

    @Override
    public QueueUpdate getQueue(String doctorId) {
        return manager.buildUpdate(doctorId);
    }

    @Override
    public Map<String, List<QueueUpdate.QueueItem>> dumpAll() {
        return manager.dumpAll();
    }

    @Override
    public void loadAll(Map<String, List<QueueUpdate.QueueItem>> state) throws InterruptedException {
        manager.loadAll(state);
    }

    @Override
    public boolean remove(String doctorId, String imageCode) throws InterruptedException {
        boolean ok = manager.remove(doctorId, imageCode);
        if (ok) sink.push(manager.buildUpdate(doctorId));
        return ok;
    }
}
