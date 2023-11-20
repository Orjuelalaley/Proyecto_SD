package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class SensorSubscriber {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.connect("tcp://localhost:5556");
            socket.subscribe("");

            System.out.println("Esperando mensajes del sensor...");
            while (!Thread.currentThread().isInterrupted()) {
                String message = socket.recvStr(0);
                System.out.println("Mensaje recibido: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
