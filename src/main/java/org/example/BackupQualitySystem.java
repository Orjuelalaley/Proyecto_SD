package org.example;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class BackupQualitySystem {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PULL);
            socket.bind("tcp://localhost:5559");  // Usamos un puerto diferente al de Broker

            System.out.println("BackupQualitySystem iniciado, esperando Alertas...");

            while (true) {
                String message = socket.recvStr(0);
                System.out.println("Mensaje recibido de Monitor: " + message);
            }
        }
    }
}