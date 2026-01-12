package edu.mondragon.os.monitors.skinxpert;


import java.util.List;
import java.util.Map;

public interface DoctorInbox {
    void enqueue(PhotoMsg msg) throws InterruptedException;

    // para push: obtener cola ordenada + tamaños de forma thread-safe
    List<QueueUpdate.QueueItem> snapshotOrdered();
    Map<String, Integer> sizesSnapshot();
}
