package com.example;
import java.util.List;
import java.util.Map;

public interface Engine {
    void accept(PhotoMsg msg) throws InterruptedException;
    String state();
    void shutdown();

    // Nuevo: consultar cola real ordenada
    QueueUpdate getQueue(String doctorId);

    // Nuevo: migración de colas entre engines
    Map<String, List<QueueUpdate.QueueItem>> dumpAll();
    void loadAll(Map<String, List<QueueUpdate.QueueItem>> state) throws InterruptedException;

    // Nuevo: eliminar tarea (sincronización con BD)
    boolean remove(String doctorId, String imageCode) throws InterruptedException;
}
