package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class SistemaCalidad {
    public void start() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            subscriber.connect("tcp://localhost:5556");  // Conéctate al puerto donde publican los sensores
            subscriber.subscribe("".getBytes());

            // Imprimir mensaje indicando que el Sistema de Calidad está listo
            System.out.println("Sistema de Calidad listo para recibir información.");

            while (true) {
                // Espera por un mensaje del sensor
                byte[] message = subscriber.recv(0);
                String[] parts = new String(message, ZMQ.CHARSET).split(" ");

                // Procesa la medición
                if (parts.length == 2) {
                    SensorType sensorType = SensorType.valueOf(parts[0]);
                    double value = Double.parseDouble(parts[1]);
                    // Procesar la medición aquí, si es necesario
                    System.out.println("Sistema de Calidad - Medición recibida: " + sensorType + " - " + value);

                    // Verificar si la medición está fuera del rango y generar una alarma si es necesario
                    if (!isMeasurementInRange(sensorType, value)) {
                        System.out.println("Sistema de Calidad - Alarma: Medición fuera del rango");
                        generateAlarm(sensorType, value);
                    }
                }
            }
        }
    }

    private boolean isMeasurementInRange(SensorType sensorType, double value) {
        // Implementa la lógica para verificar si la medición está dentro del rango especificado
        // Puedes utilizar una estructura de datos que almacene los rangos permitidos para cada tipo de sensor
        // y comparar la medición con los valores mínimos y máximos permitidos.
        // Retorna true si está dentro del rango, false si está fuera del rango.
        // Por ejemplo:
        return switch (sensorType) {
            case TEMPERATURE -> value >= 68 && value <= 89;
            case PH -> value >= 6 && value <= 8;
            case OXYGEN -> value >= 2 && value <= 11;
        };
    }

    private void generateAlarm(SensorType sensorType, double value) {
        // Aquí puedes agregar la lógica para generar una alarma, por ejemplo, enviándola a un registro o un sistema de alerta.
        // También puedes almacenar las alarmas en una base de datos o en un archivo según tus necesidades.
        // Por ahora, muestra la alarma por pantalla como ejemplo:
        System.out.println("Sistema de Calidad - Alarma generada: " + sensorType + " - " + value + " - Medición fuera del rango");
    }

    public static void main(String[] args) {
        // Iniciar el Sistema de Calidad
        SistemaCalidad sistemaCalidad = new SistemaCalidad();
        sistemaCalidad.start();
    }
}
