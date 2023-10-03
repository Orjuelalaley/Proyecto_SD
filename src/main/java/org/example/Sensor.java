package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Sensor {
    private SensorType type;
    private int interval;
    private ZContext context;
    private ZMQ.Socket socket;
    private int port;

    public Sensor(SensorType type, int interval, int port) {
        this.type = type;
        this.interval = interval;
        this.port = port;
        this.context = new ZContext();
        this.socket = context.createSocket(SocketType.PUB);
    }

    public void connectToMonitor() {
        // Establece la conexión con el Sistema de Calidad y los Monitores a través de ZeroMQ
        socket.bind("tcp://localhost:" + port); // Enlazar al puerto específico para este sensor
        System.out.println("Sensor " + type + " conectado al Sistema de Calidad y los Monitores en el puerto " + port + ".");
    }

    public void start() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                double value = generateRandomMeasurement();
                String message = type + " " + value;
                socket.send(message.getBytes(ZMQ.CHARSET));

                System.out.println("Sensor " + type + " - Medición: " + value);

                Thread.sleep(interval);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private double generateRandomMeasurement() {
        // Genera un valor aleatorio dentro de un rango específico para cada tipo de sensor
        switch (type) {
            case TEMPERATURE:
                return 68 + (21 * Math.random()); // Rango: [68, 89]
            case PH:
                return 6 + (2 * Math.random()); // Rango: [6, 8]
            case OXYGEN:
                return 2 + (9 * Math.random()); // Rango: [2, 11]
            default:
                return 0.0;
        }
    }

    public void stop() {
        context.close();
    }
}
