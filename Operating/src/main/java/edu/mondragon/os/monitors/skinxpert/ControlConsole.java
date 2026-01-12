package edu.mondragon.os.monitors.skinxpert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ControlConsole implements Runnable {
    private final Supplier<String> stateSupplier;
    private final Consumer<String> modeChanger;
    private final AtomicInteger rateMs;
    private final AtomicBoolean paused;
    private final Runnable onQuit;

    public ControlConsole(
            Supplier<String> stateSupplier,
            Consumer<String> modeChanger,
            AtomicInteger rateMs,
            AtomicBoolean paused,
            Runnable onQuit
    ) {
        this.stateSupplier = stateSupplier;
        this.modeChanger = modeChanger;
        this.rateMs = rateMs;
        this.paused = paused;
        this.onQuit = onQuit;
    }

    @Override
    public void run() {
        System.out.println("""
                [CONTROL] Commands:
                  state
                  rate <ms>
                  pause | resume
                  mode monitor | mode mp
                  quit
                """);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equalsIgnoreCase("state")) {
                    System.out.println(stateSupplier.get());
                } else if (line.toLowerCase().startsWith("rate ")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length == 2) {
                        try {
                            int ms = Integer.parseInt(parts[1]);
                            if (ms < 10) ms = 10;
                            rateMs.set(ms);
                            System.out.println("[CONTROL] rateMs=" + rateMs.get());
                        } catch (NumberFormatException e) {
                            System.out.println("[CONTROL] Invalid number.");
                        }
                    }
                } else if (line.equalsIgnoreCase("pause")) {
                    paused.set(true);
                    System.out.println("[CONTROL] paused=true");
                } else if (line.equalsIgnoreCase("resume")) {
                    paused.set(false);
                    System.out.println("[CONTROL] paused=false");
                } else if (line.toLowerCase().startsWith("mode ")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length == 2) {
                        modeChanger.accept(parts[1].toLowerCase());
                    }
                } else if (line.equalsIgnoreCase("quit")) {
                    onQuit.run();
                    return;
                } else {
                    System.out.println("[CONTROL] Unknown command.");
                }
            }
        } catch (Exception e) {
            System.err.println("[CONTROL] Error: " + e.getMessage());
        }
    }
}

