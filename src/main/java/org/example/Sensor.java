package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Random;

public class Sensor {
    private final SensorType type;
    private final int interval;
    private final ZMQ.Socket socket;
    private final Random random = new Random();

    public Sensor(SensorType type, int interval, ZMQ.Socket socket) {
        this.type = type;
        this.interval = interval;
        this.socket = socket;
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
        double randomNumber = random.nextDouble();
        double probabilityOutOfRange = 0.3;
        double probabilityCorrect = 0.6;
        if (randomNumber < probabilityCorrect) {
            // Valor correcto dentro del rango
            return generateValueInRange();
        } else if (randomNumber < probabilityCorrect + probabilityOutOfRange) {
            // Valor fuera del rango
            return generateValueOutOfRange();
        } else {
            // Valor inválido (negativo)
            return generateValueInvalid();
        }
    }

    private double generateValueInRange() {
        return switch (type) {
            case TEMPERATURE -> 68 + (21 * random.nextDouble()); // Rango: [68, 89]
            case PH -> 6 + (2 * random.nextDouble()); // Rango: [6, 8]
            case OXYGEN -> 2 + (9 * random.nextDouble()); // Rango: [2, 11]
        };
    }

    private double generateValueOutOfRange() {
        return switch (type) {
            case TEMPERATURE ->
                    random.nextDouble() < 0.5 ? 100 + random.nextDouble() * 10 : -10 - random.nextDouble() * 10;
            case PH -> random.nextDouble() < 0.5 ? 9 + random.nextDouble() * 2 : 12 + random.nextDouble() * 2;
            case OXYGEN -> random.nextDouble() < 0.5 ? 1 + random.nextDouble() * 1 : 13 + random.nextDouble() * 1;
        };
    }

    private double generateValueInvalid() {
        return -random.nextDouble() * 10;
    }
}
