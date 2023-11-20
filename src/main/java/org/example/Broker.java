package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Broker {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket frontend = context.createSocket(SocketType.XSUB);
            ZMQ.Socket backend = context.createSocket(SocketType.XPUB);
            frontend.bind("tcp://localhost:5555"); // Sensores se conectan aquí
            backend.bind("tcp://localhost:5556");
            ZMQ.Socket qualitySystemSocket = context.createSocket(SocketType.PUSH);
            qualitySystemSocket.bind("tcp://localhost:5558");

            System.out.println("Broker iniciado...");
            ZMQ.proxy(frontend, backend, null);
        }
    }
}