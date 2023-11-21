package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sensor {
    private final String type;
    private final int interval;
    private final ZMQ.Socket socket;
    private final Random random = new Random();
    private double probabilidadEnRango;
    private double probabilidadFueraDeRango;
    private double probabilidadErronea;
    private static final Logger LOGGER = Logger.getLogger(Sensor.class.getName());

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Uso: Sensor <tipo> <intervalo> <ruta-config>");
            System.exit(1);
        }
        String type = args[0];
        int timestamp = Integer.parseInt(args[1]);
        String file = args[2];
        if (timestamp < 0) {
            throw new NumberFormatException();
        }
        try (ZContext context = new ZContext()){
            ZMQ.Socket socket = context.createSocket(SocketType.PUB);
            socket.connect("tcp://localhost:5555");
            Sensor sensor = new Sensor(type, timestamp, socket, file);
            sensor.start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public Sensor(String type, int interval, ZMQ.Socket socket, String file) {
        this.type = type;
        this.interval = interval;
        this.socket = socket;
        readConfig(file);
    }

    private void readConfig(String file) {
        try (FileReader reader = new FileReader(file)) {
            Properties props = new Properties();
            props.load(reader);
            this.probabilidadEnRango = Double.parseDouble(props.getProperty("probabilidadEnRango"));
            this.probabilidadFueraDeRango = Double.parseDouble(props.getProperty("probabilidadFueraDeRango"));
            this.probabilidadErronea = Double.parseDouble(props.getProperty("probabilidadErronea"));
            System.out.println("Probability in range: " + probabilidadEnRango);
            System.out.println("Probabilidad out of range: " + probabilidadFueraDeRango);
            System.out.println("Wrong probability: " + probabilidadErronea);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void start() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                double value = generateRandomMeasurement();
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String message = type + " " + value + " at " + timestamp;
                socket.send(message.getBytes(ZMQ.CHARSET));
                System.out.println("Sensor " + type + " - Medición: " + value + " at " + timestamp);

                Thread.sleep(interval);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendToQualitySystem(String errorMessage) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PUB);
            socket.connect("tcp://localhost:5557");
            socket.send(errorMessage.getBytes(ZMQ.CHARSET));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isOutOfRange(double value) {
        switch (type) {
            case "TEMPERATURE":
                return value < 68 || value > 89;
            case "PH":
                return value < 6.0 || value > 8.0;
            case "OXYGEN":
                return value < 2 || value > 11;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private boolean isInvalid(double value) {
        return value < 0;
    }

    private double generateRandomMeasurement() {
        double randomNumber = random.nextDouble();
        if (randomNumber < probabilidadEnRango) {
            return generateValueInRange();
        } else if (randomNumber < probabilidadEnRango + probabilidadFueraDeRango) {
            return generateValueOutOfRange();
        } else {
            return generateValueInvalid(probabilidadErronea);
        }
    }

    private double generateValueInRange() {
        return switch (type) {
            case "TEMPERATURE" -> 68 + (21 * random.nextDouble());
            case "PH" -> 6 + (2 * random.nextDouble());
            case "OXYGEN" -> 2 + (9 * random.nextDouble());
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private double generateValueOutOfRange() {
        return switch (type) {
            case "TEMPERATURE" -> random.nextDouble() < 0.5 ? 100 + random.nextDouble() * 10 : -10 - random.nextDouble() * 10;
            case "PH" -> random.nextDouble() < 0.5 ? 9 + random.nextDouble() * 2 : 12 + random.nextDouble() * 2;
            case "OXYGEN" -> random.nextDouble() < 0.5 ? 1 + random.nextDouble() * 1 : 13 + random.nextDouble() * 1;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private double generateValueInvalid(double probabilidadErronea) {
        return -probabilidadErronea * 10;
    }
}