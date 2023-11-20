package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class QualitySystem {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.connect("tcp://localhost:5558");  // Puerto para el Sistema de Calidad (SC)
            socket.subscribe("");

            System.out.println("Sistema de Calidad iniciado...");

            // Agrega una pausa de 1 segundo antes de comenzar a recibir mensajes
            Thread.sleep(1000);

            while (!Thread.currentThread().isInterrupted()) {
                String message = socket.recvStr(0);
                if (message != null) {
                    System.out.println("¡Alerta del Sistema de Calidad!: " + message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
