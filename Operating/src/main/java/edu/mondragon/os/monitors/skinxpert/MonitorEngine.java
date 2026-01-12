package edu.mondragon.os.monitors.skinxpert;

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
        // Estado global (todas las colas creadas hasta ahora)
        return manager.stateSnapshot();
    }

    @Override
    public void shutdown() {
        // no hay hilos internos persistentes aquí
    }
}

