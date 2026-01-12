package edu.mondragon.os.monitors.skinxpert;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class IncomingSimulator implements Runnable {
    private final QueueEngine engine;
    private final List<String> doctors;
    private final Random rnd = new Random();
    private final AtomicInteger seq = new AtomicInteger(1);

    public IncomingSimulator(QueueEngine engine, List<String> doctors) {
        this.engine = engine;
        this.doctors = doctors;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String doctorId = doctors.get(rnd.nextInt(doctors.size()));
                PhotoMsg.Urgency u = switch (rnd.nextInt(3)) {
                    case 0 -> PhotoMsg.Urgency.ALTO;
                    case 1 -> PhotoMsg.Urgency.MEDIO;
                    default -> PhotoMsg.Urgency.BAJO;
                };

                PhotoMsg msg = new PhotoMsg("IMG-" + seq.getAndIncrement(), doctorId, u);
                System.out.println("[IN-SIM] " + msg.imageCode + " -> " + msg.doctorId + " (" + msg.urgency + ")");

                engine.onIncoming(msg);

                Thread.sleep(250 + rnd.nextInt(350));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
