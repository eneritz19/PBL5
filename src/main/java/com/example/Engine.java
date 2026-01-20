package com.example;
import java.util.List;
import java.util.Map;

public interface Engine {
    void accept(PhotoMsg msg) throws InterruptedException;
    String state();
    void shutdown();

    QueueUpdate getQueue(String doctorId);

    Map<String, List<QueueUpdate.QueueItem>> dumpAll();
    void loadAll(Map<String, List<QueueUpdate.QueueItem>> state) throws InterruptedException;

    boolean remove(String doctorId, String imageCode) throws InterruptedException;
}
