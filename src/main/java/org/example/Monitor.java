package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Monitor {
    private final String type;
    private final ZMQ.Socket socket;
    private final BufferedWriter writer;
    private final ZMQ.Socket qualitySystemSocket;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: Monitor <tipo>");
            System.exit(1);
        }

        String type = args[0];

        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.connect("tcp://localhost:5556");
            socket.subscribe("");

            ZMQ.Socket qualitySystemSocket = context.createSocket(SocketType.PUSH);
            qualitySystemSocket.connect("tcp://localhost:5558");

            BufferedWriter writer = new BufferedWriter(new FileWriter("mediciones_" + type + ".txt"));

            Monitor monitor = new Monitor(type, socket, writer, qualitySystemSocket);
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Monitor(String type, ZMQ.Socket socket, BufferedWriter writer, ZMQ.Socket qualitySystemSocket) {
        this.type = type;
        this.socket = socket;
        this.writer = writer;
        this.qualitySystemSocket = qualitySystemSocket;
    }

    public void start() {
        try {
            System.out.println("Esperando mensajes del sensor...");

            while (!Thread.currentThread().isInterrupted()) {
                String message = socket.recvStr(0);
                System.out.println(message);
                processMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String message) {
        // Analizar el mensaje y verificar si está en rango y sin errores
        String[] parts = message.split(" ");
        double value = Double.parseDouble(parts[1]);
        if (isInRange(value)) {
            try {
                writer.write(message + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendToQualitySystem("¡Alarma! Medida fuera de rango para el tipo " + type + ": " + message);
        }
    }

    private boolean isInRange(double value) {
        // Verificar si el valor está en rango según el tipo de sensor
        switch (type) {
            case "TEMPERATURE":
                return value >= 68 && value <= 89;
            case "PH":
                return value >= 6.0 && value <= 8.0;
            case "OXYGEN":
                return value >= 2 && value <= 11;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private void sendToQualitySystem(String errorMessage) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PUSH);
            socket.connect("tcp://localhost:5558");  // Puerto para el Sistema de Calidad (SC)

            // Envía el mensaje de alarma
            socket.send(errorMessage.getBytes(ZMQ.CHARSET));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
