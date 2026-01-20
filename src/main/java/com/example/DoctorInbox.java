package com.example;
import java.util.List;
import java.util.Map;

public interface DoctorInbox {
    void enqueue(PhotoMsg msg) throws InterruptedException;

    List<QueueUpdate.QueueItem> snapshotOrdered();
    Map<String, Integer> sizesSnapshot();

    boolean removeByImageCode(String imageCode) throws InterruptedException;
}
