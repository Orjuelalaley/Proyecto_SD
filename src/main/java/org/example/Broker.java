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

                // Configurar el poller para monitorear la conexión del monitor
                ZMQ.Poller poller = context.createPoller(1);
                poller.register(frontend, ZMQ.Poller.POLLIN);

                System.out.println("Esperando conexión del monitor...");

                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < 10000) {
                    int pollResult = poller.poll(5000); // Esperar hasta 1 segundo por eventos
                    if (pollResult > 0) {
                        // Conexión establecida, salir del bucle
                        break;
                    }
                    System.out.println("No se ha conectado el monitor. Intentando de nuevo...");
                }

                // Detener el poller después de esperar 5 segundos
                poller.close();

                // Verificar si el monitor se conectó
                if (poller.pollin(0)) {
                    System.out.println("Monitor conectado. Iniciando broker...");
                } else {
                    System.out.println("Monitor no se ha conectado en 5 segundos. Conectando al puerto de respaldo...");
                    backend.unbind("tcp://localhost:5556");
                    backend.bind("tcp://localhost:5560");
                }

                // Iniciar la proxy después de manejar la conexión del monitor
                ZMQ.proxy(frontend, backend, null);

        }
    }
}