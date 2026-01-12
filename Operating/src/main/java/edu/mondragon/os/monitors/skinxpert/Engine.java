package edu.mondragon.os.monitors.skinxpert;

public interface Engine {
    void accept(PhotoMsg msg) throws InterruptedException; // entrada (simulada o real)
    String state();                                        // monitoring
    void shutdown();                                       // cierre limpio
}
