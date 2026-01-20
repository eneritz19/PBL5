package com.example;
import java.time.Instant;
import java.util.Objects;

public final class PhotoMsg {
    public enum Urgency { ALTO, MEDIO, BAJO }

    public final String imageCode;
    public final String doctorId;
    public final Urgency urgency;
    public final long createdAtMillis;

    public PhotoMsg(String imageCode, String doctorId, Urgency urgency) {
        this(imageCode, doctorId, urgency, Instant.now().toEpochMilli());
    }

    public PhotoMsg(String imageCode, String doctorId, Urgency urgency, long createdAtMillis) {
        this.imageCode = Objects.requireNonNull(imageCode);
        this.doctorId = Objects.requireNonNull(doctorId);
        this.urgency = Objects.requireNonNull(urgency);
        this.createdAtMillis = createdAtMillis;
    }
}
