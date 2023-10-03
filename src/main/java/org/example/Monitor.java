package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Monitor {
    public void start() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);

            subscriber.connect("tcp://10.195.70.156:5556");
            subscriber.subscribe(SensorType.TEMPERATURE.toString().getBytes());
            subscriber.subscribe(SensorType.PH.toString().getBytes());
            subscriber.subscribe(SensorType.OXYGEN.toString().getBytes());

            // Imprimir mensaje indicando que el monitor está listo
            System.out.println("Monitor listo para recibir información.");

            while (true) {
                // Espera por un mensaje del sensor
                byte[] message = subscriber.recv(0);
                String[] parts = new String(message, ZMQ.CHARSET).split(" ");

                // Procesa la medición
                if (parts.length == 2) {
                    SensorType sensorType = SensorType.valueOf(parts[0]);
                    double value = Double.parseDouble(parts[1]);
                    // Procesar la medición aquí, si es necesario
                    System.out.println("Monitor - Medición recibida: " + sensorType + " - " + value);
                }
            }
        }
    }

    public static void main(String[] args) {
        // Iniciar el monitor
        Monitor monitor = new Monitor();
        monitor.start();
    }
}
