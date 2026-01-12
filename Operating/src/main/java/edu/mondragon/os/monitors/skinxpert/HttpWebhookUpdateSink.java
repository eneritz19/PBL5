package edu.mondragon.os.monitors.skinxpert;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

public class HttpWebhookUpdateSink implements UpdateSink {
    private final HttpClient client = HttpClient.newHttpClient();
    private final URI webhookUri;

    public HttpWebhookUpdateSink(String webhookUrl) {
        this.webhookUri = URI.create(webhookUrl);
    }

    @Override
    public void push(QueueUpdate update) {
        try {
            String queueJson = update.queueOrdered.stream()
                    .map(i -> String.format("{\"imageCode\":\"%s\",\"urgency\":\"%s\",\"createdAt\":%d}",
                            i.imageCode, i.urgency, i.createdAt))
                    .collect(Collectors.joining(","));

            String sizesJson = update.sizes.entrySet().stream()
                    .map(e -> String.format("\"%s\":%d", e.getKey(), e.getValue()))
                    .collect(Collectors.joining(","));

            String json = String.format("{\"doctorId\":\"%s\",\"queue\":[%s],\"sizes\":{%s}}",
                    update.doctorId, queueJson, sizesJson);

            HttpRequest req = HttpRequest.newBuilder(webhookUri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            System.err.println("[PUSH-HTTP] Error pushing update: " + e.getMessage());
        }
    }
}
