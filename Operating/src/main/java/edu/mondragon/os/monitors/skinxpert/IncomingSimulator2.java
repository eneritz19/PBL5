package edu.mondragon.os.monitors.skinxpert;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class IncomingSimulator2 implements Runnable {
    private final SupplierEngine engineSupplier;
    private final List<String> doctors;
    private final Random rnd = new Random();

    private final AtomicInteger rateMs;
    private final AtomicBoolean paused;
    private final AtomicLong seq = new AtomicLong(1);

    private volatile boolean running = true;

    public interface SupplierEngine {
        Engine get();
    }

    public IncomingSimulator2(SupplierEngine engineSupplier, List<String> doctors, AtomicInteger rateMs, AtomicBoolean paused) {
        this.engineSupplier = engineSupplier;
        this.doctors = doctors;
        this.rateMs = rateMs;
        this.paused = paused;
    }

    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            try {
                if (paused.get()) {
                    Thread.sleep(100);
                    continue;
                }

                String doctorId = doctors.get(rnd.nextInt(doctors.size()));
                PhotoMsg.Urgency u = switch (rnd.nextInt(3)) {
                    case 0 -> PhotoMsg.Urgency.ALTO;
                    case 1 -> PhotoMsg.Urgency.MEDIO;
                    default -> PhotoMsg.Urgency.BAJO;
                };

                String imageCode = "IMG-" + seq.getAndIncrement();
                PhotoMsg msg = new PhotoMsg(imageCode, doctorId, u);

                System.out.println("[IN-SIM] " + msg.imageCode + " -> " + msg.doctorId + " (" + msg.urgency + ")");
                engineSupplier.get().accept(msg);

                Thread.sleep(rateMs.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                System.err.println("[IN-SIM] Error: " + ex.getMessage());
            }
        }
    }
}
