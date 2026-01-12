package edu.mondragon.os.monitors.skinxpert;


public class QueueEngine {
    private final DoctorQueueManager manager;
    private final UpdateSink sink;

    public QueueEngine(DoctorQueueManager manager, UpdateSink sink) {
        this.manager = manager;
        this.sink = sink;
    }

    public void onIncoming(PhotoMsg msg) throws InterruptedException {
        // 1) Gestión de colas (tu parte)
        manager.enqueue(msg);

        // 2) Push con “cómo queda la nueva cola”
        QueueUpdate update = manager.buildUpdate(msg.doctorId);
        sink.push(update);
    }
}
