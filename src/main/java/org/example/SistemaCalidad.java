package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public class SistemaCalidad {
    private Map<SensorType, Range> sensorRanges;

    public SistemaCalidad() {
        // Define los rangos permitidos para cada tipo de sensor
        sensorRanges = new HashMap<>();
        sensorRanges.put(SensorType.TEMPERATURE, new Range(68, 89));
        sensorRanges.put(SensorType.PH, new Range(6, 8));
        sensorRanges.put(SensorType.OXYGEN, new Range(2, 11));
    }

    public void start() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            subscriber.connect("tcp://localhost:5556");  // Conéctate al puerto donde publica el sensor
            subscriber.subscribe("".getBytes());

            // Imprimir mensaje indicando que el Sistema de Calidad está listo
            System.out.println("Sistema de Calidad listo para recibir información.");

            // Configurar socket para enviar alarmas a los Monitores
            ZMQ.Socket alarmPublisher = context.createSocket(SocketType.PUB);
            alarmPublisher.bind("tcp://localhost:5559");

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

                        // Enviar alarma a los Monitores
                        alarmPublisher.send(sensorType + " - " + value);
                    }
                }
            }
        }
    }

    private boolean isMeasurementInRange(SensorType sensorType, double value) {
        // Obtén el rango permitido para el tipo de sensor
        Range range = sensorRanges.get(sensorType);

        // Verifica si la medición está dentro del rango especificado
        return range != null && range.isValueInRange(value);
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
