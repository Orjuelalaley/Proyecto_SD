package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Random;

public class Sensor {
    private SensorType type;
    private int interval;
    private ZContext context;
    private ZMQ.Socket socket;
    private Random random = new Random();

    // Probabilidades proporcionadas en el archivo de configuración
    private double probabilityCorrect;
    private double probabilityOutOfRange;
    private int port;

    public Sensor(SensorType type, int interval, int port) {
        this.type = type;
        this.interval = interval;
        this.context = new ZContext();
        this.socket = context.createSocket(SocketType.PUB);
        this.port = port;
    }


    public Sensor(SensorType type, int interval, double probabilityCorrect, double probabilityOutOfRange, double probabilityInvalid) {
        this.type = type;
        this.interval = interval;
        this.context = new ZContext();
        this.socket = context.createSocket(SocketType.PUB);
        // Asignar las probabilidades proporcionadas
        this.probabilityCorrect = probabilityCorrect;
        this.probabilityOutOfRange = probabilityOutOfRange;
        // Verificar que las probabilidades sumen 1.0
        double sum = probabilityCorrect + probabilityOutOfRange + probabilityInvalid;
        if (sum != 1.0) {
            throw new IllegalArgumentException("Las probabilidades deben sumar 1.0");
        }
    }

    public void connectToMonitor() {
        // Establece la conexión con el Sistema de Calidad y los Monitores a través de ZeroMQ
        socket.bind("tcp://localhost:" + port); // Enlazar al puerto proporcionado
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
        double randomNumber = random.nextDouble(); // Generar número aleatorio entre 0 y 1

        // Comparar el número aleatorio con las probabilidades para determinar el tipo de medición
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
        // Generar un valor aleatorio dentro del rango específico para cada tipo de sensor
        return switch (type) {
            case TEMPERATURE -> 68 + (21 * random.nextDouble()); // Rango: [68, 89]
            case PH -> 6 + (2 * random.nextDouble()); // Rango: [6, 8]
            case OXYGEN -> 2 + (9 * random.nextDouble()); // Rango: [2, 11]
            default -> 0.0;
        };
    }

    private double generateValueOutOfRange() {
        // Generar un valor aleatorio fuera del rango específico para cada tipo de sensor
        return switch (type) {
            case TEMPERATURE ->
                    random.nextDouble() < 0.5 ? 100 + random.nextDouble() * 10 : -10 - random.nextDouble() * 10;
            case PH -> random.nextDouble() < 0.5 ? 9 + random.nextDouble() * 2 : 12 + random.nextDouble() * 2;
            case OXYGEN -> random.nextDouble() < 0.5 ? 1 + random.nextDouble() * 1 : 13 + random.nextDouble() * 1;
            default -> 0.0;
        };
    }

    private double generateValueInvalid() {
        // Generar un valor inválido (negativo)
        return -random.nextDouble() * 10;
    }

    public void stop() {
        context.close();
    }
}
