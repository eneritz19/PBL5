package edu.mondragon.os.monitors.skinxpert;

import java.util.stream.Collectors;

public class ConsoleUpdateSink implements UpdateSink {
    @Override
    public void push(QueueUpdate update) {
        String queueJson = update.queueOrdered.stream()
                .map(i -> String.format("{\"imageCode\":\"%s\",\"urgency\":\"%s\",\"createdAt\":%d}",
                        i.imageCode, i.urgency, i.createdAt))
                .collect(Collectors.joining(","));

        String sizesJson = update.sizes.entrySet().stream()
                .map(e -> String.format("\"%s\":%d", e.getKey(), e.getValue()))
                .collect(Collectors.joining(","));

        String json = String.format("{\"doctorId\":\"%s\",\"queue\":[%s],\"sizes\":{%s}}",
                update.doctorId, queueJson, sizesJson);

        System.out.println("[PUSH-SIM] " + json);
    }
}
