package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Main {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PUB);
            socket.bind("tcp://localhost:5556"); // Enlazar el socket a un puerto único

            // Crear instancias de sensores con intervalos diferentes
            Sensor temperatureSensor = new Sensor(SensorType.TEMPERATURE, 2000, socket);
            Sensor phSensor = new Sensor(SensorType.PH, 3000, socket);
            Sensor oxygenSensor = new Sensor(SensorType.OXYGEN, 2500, socket);

            // Iniciar los sensores en hilos separados
            Thread temperatureThread = new Thread(temperatureSensor::start);
            Thread phThread = new Thread(phSensor::start);
            Thread oxygenThread = new Thread(oxygenSensor::start);

            temperatureThread.start();
            phThread.start();
            oxygenThread.start();

            // Esperar a que los hilos terminen (esto no es necesario si tu aplicación sigue ejecutándose)
            try {
                temperatureThread.join();
                phThread.join();
                oxygenThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Ocurrió un error al esperar a que los hilos terminen: " + e.getMessage());
            }
        }
    }
}
