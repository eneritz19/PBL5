package com.example;

public class QueueEngine {
    private final DoctorQueueManager manager;
    private final UpdateSink sink;

    public QueueEngine(DoctorQueueManager manager, UpdateSink sink) {
        this.manager = manager;
        this.sink = sink;
    }

    public void onIncoming(PhotoMsg msg) throws InterruptedException {
        manager.enqueue(msg);
        
        QueueUpdate update = manager.buildUpdate(msg.doctorId);
        sink.push(update);
    }
}
