package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Broker {
    public void start() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket frontend = context.createSocket(SocketType.XSUB);
            ZMQ.Socket backend = context.createSocket(SocketType.XPUB);
            frontend.connect("tcp://*:5556");
            backend.connect("tcp://*:5557");

            // Conéctate a los sockets frontend y backend
            ZMQ.proxy(frontend, backend, null);
        }
    }

    public static void main(String[] args) {
        // Iniciar el broker
        Broker broker = new Broker();
        broker.start();
    }
}
