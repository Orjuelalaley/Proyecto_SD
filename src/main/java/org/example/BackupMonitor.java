package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class BackupMonitor {
    private final String type;
    private final ZMQ.Socket socket;
    private final BufferedWriter writer;
    private final ZMQ.Socket qualitySystemSocket;
    private final BufferedWriter timeWriter;
    private final BufferedWriter memoryWriter;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: Monitor <tipo>");
            System.exit(1);
        }

        String type = args[0];

        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.connect("tcp://localhost:5556");
            socket.subscribe(type.getBytes(ZMQ.CHARSET));

            ZMQ.Socket qualitySystemSocket = context.createSocket(SocketType.PUSH);
            qualitySystemSocket.connect("tcp://localhost:5559");

            BufferedWriter writer = new BufferedWriter(new FileWriter("mediciones_" + type + ".txt"));
            BufferedWriter timeWriter = new BufferedWriter(new FileWriter("tiempo_de_ejecucion.txt"));
            BufferedWriter memoryWriter = new BufferedWriter(new FileWriter("memoria.txt"));

            BackupMonitor monitor = new BackupMonitor(type, socket, writer, qualitySystemSocket, timeWriter, memoryWriter);
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BackupMonitor(String type, ZMQ.Socket socket, BufferedWriter writer, ZMQ.Socket qualitySystemSocket, BufferedWriter timeWriter, BufferedWriter memoryWriter) {
        this.type = type;
        this.socket = socket;
        this.writer = writer;
        this.qualitySystemSocket = qualitySystemSocket;
        this.timeWriter = timeWriter;
        this.memoryWriter = memoryWriter;
    }

    public void start() {
        try {
            System.out.println("Esperando mensajes del sensor...");

            while (!Thread.currentThread().isInterrupted()) {
                // Medir la memoria antes de procesar el mensaje
                long memoryBefore = getMemoryUsage();

                String message = socket.recvStr(0);
                System.out.println(message);
                long startTime = System.nanoTime();
                processMessage(message);
                long endTime = System.nanoTime();
                long executionTime = endTime - startTime;

                // Medir la memoria después de procesar el mensaje
                long memoryAfter = getMemoryUsage();
                long memoryUsed = memoryAfter - memoryBefore;


                writeTimeToFile(executionTime);
                writeMemoryToFile(memoryUsed);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private void processMessage(String message) {
        // Analyze the message and check if it is in range and without errors
        String[] parts = message.split(" ");
        double value = Double.parseDouble(parts[1]);
        if (isInRange(value)) {
            try {
                long startTime = System.nanoTime();
                writer.write(message + "\n");
                writer.flush();
                long endTime = System.nanoTime();
                long executionTime = endTime - startTime;


                // Medir la memoria después de escribir en el archivo
                long memoryAfter = getMemoryUsage();
                long memoryUsed = memoryAfter - startTime;


                writeTimeToFile(executionTime);
                writeMemoryToFile(memoryUsed);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendToQualitySystem("¡Alarma! Medida fuera de rango para el tipo " + type + ": " + message);
        }
    }

    private void writeTimeToFile(long executionTime) {
        try {
            timeWriter.write(executionTime + "\n");
            timeWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeMemoryToFile(long memoryUsed) {
        try {
            memoryWriter.write(memoryUsed + "\n");
            memoryWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isInRange(double value) {
        // Check if the value is in range according to the type of sensor
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

    private void sendToQualitySystem(String message) {
        qualitySystemSocket.send(message.getBytes(ZMQ.CHARSET));
    }
}
