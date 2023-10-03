package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PUB);
            socket.bind("tcp://10.195.70.156:5556"); // Enlazar el socket a un puerto único

            // Crear una lista de sensores
            List<Sensor> sensors = new ArrayList<>();
            sensors.add(new Sensor(SensorType.TEMPERATURE, 5000, socket));
            sensors.add(new Sensor(SensorType.PH, 3000, socket));
            sensors.add(new Sensor(SensorType.OXYGEN, 2500, socket));

            // Iniciar y ejecutar cada sensor en hilos separados
            List<Thread> sensorThreads = new ArrayList<>();
            for (Sensor sensor : sensors) {
                Thread sensorThread = new Thread(sensor::start);
                sensorThread.start();
                sensorThreads.add(sensorThread);
            }

            // Esperar a que los hilos de los sensores terminen
            for (Thread sensorThread : sensorThreads) {
                try {
                    sensorThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Ocurrió un error al esperar a que los hilos terminen: " + e.getMessage());
                }
            }
        }
    }
}
