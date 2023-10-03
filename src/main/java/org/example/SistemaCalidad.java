package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

public class SistemaCalidad {
    private Map<SensorType, Range> sensorRanges;

    public SistemaCalidad(Map<SensorType, Range> sensorRanges) {
        this.sensorRanges = sensorRanges;
    }

    public void start() {
        try (ZContext context = new ZContext()) {
            // Configurar socket para recibir datos del Broker
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            subscriber.connect("tcp://10.195.70.156:5556"); // Conéctate al puerto del Broker
            subscriber.subscribe("".getBytes());

            // Imprimir mensaje indicando que el Sistema de Calidad está listo
            System.out.println("Sistema de Calidad listo para recibir información.");

            // Configurar socket para enviar alarmas a los Monitores
            ZMQ.Socket alarmPublisher = context.createSocket(SocketType.PUB);
            alarmPublisher.bind("tcp://localhost:5558");

            while (true) {
                // Espera por un mensaje del Broker
                byte[] message = subscriber.recv(0);
                String[] parts = new String(message, ZMQ.CHARSET).split(" ");
                // Procesa la medición
                if (parts.length == 2) {
                    SensorType sensorType = SensorType.valueOf(parts[0]);
                    double value = Double.parseDouble(parts[1]);
                    // Verificar si la medición está fuera del rango y generar una alarma si es necesario
                    if (isMeasurementOutOfRange(sensorType, value)) {
                        System.out.println("Sistema de Calidad - Alarma: Medición fuera del rango");
                        generateAlarm(sensorType, value);

                        // Enviar alarma a los Monitores
                        alarmPublisher.send(sensorType + " - " + value);
                    }
                }
            }
        }
    }

    private boolean isMeasurementOutOfRange(SensorType sensorType, double value) {
        // Obtén el rango permitido para el tipo de sensor
        Range range = sensorRanges.get(sensorType);

        // Verifica si la medición está fuera del rango especificado
        if (range != null) {
            double minValue = range.getMin();
            double maxValue = range.getMax();
            return value < minValue || value > maxValue;
        }

        // Si el rango no está definido, consideramos que la medición está dentro del rango
        return false;
    }

    private void generateAlarm(SensorType sensorType, double value) {
        // Aquí puedes agregar la lógica para generar una alarma, por ejemplo, enviándola a un registro o un sistema de alerta.
        // También puedes almacenar las alarmas en una base de datos o en un archivo según tus necesidades.
        // Por ahora, muestra la alarma por pantalla como ejemplo:
        System.out.println("Sistema de Calidad - Alarma generada: " + sensorType + " - " + value + " - Medición fuera del rango");
    }

    public static void main(String[] args) {
        // Define los rangos permitidos para cada tipo de sensor
        Map<SensorType, Range> sensorRanges = new HashMap<>();
        sensorRanges.put(SensorType.TEMPERATURE, new Range(68, 89));
        sensorRanges.put(SensorType.PH, new Range(6, 8));
        sensorRanges.put(SensorType.OXYGEN, new Range(2, 11));

        // Iniciar el Sistema de Calidad
        SistemaCalidad sistemaCalidad = new SistemaCalidad(sensorRanges);
        sistemaCalidad.start();
    }
}
