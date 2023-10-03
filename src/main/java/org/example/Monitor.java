package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Monitor {
    private final SensorType sensorType;

    public Monitor(SensorType sensorType, String databaseUrl) {
        this.sensorType = sensorType;
    }

    public void start() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            subscriber.connect("tcp://localhost:5556");  // Conéctate al puerto donde publica el sensor
            subscriber.subscribe(sensorType.toString().getBytes());

            while (true) {
                // Espera por un mensaje del sensor
                byte[] message = subscriber.recv(0);
                String[] parts = new String(message, ZMQ.CHARSET).split(" ");

                // Validar el mensaje y almacenar la medición si es válida
                if (parts.length == 2 && SensorType.valueOf(parts[0]) == sensorType) {
                    double value = Double.parseDouble(parts[1]);

                    // Almacenar la medición en la base de datos o en el archivo
                    storeMeasurement(value);

                    // Verificar si la medición está fuera del rango y generar una alarma si es necesario
                    if (!isMeasurementInRange(value)) {
                        generateAlarm(value);
                    }
                }
            }
        }
    }

    private void storeMeasurement(double value) {
        // Implementa la lógica para almacenar la medición en la base de datos o en el archivo
        // Utiliza la URL de la base de datos (databaseUrl) para conectarte y guardar la medición.
    }

    private boolean isMeasurementInRange(double value) {
        // Implementa la lógica para verificar si la medición está dentro del rango especificado
        // Retorna true si está dentro del rango, false si está fuera del rango.
        // Puedes utilizar una estructura de datos que almacene los rangos permitidos para cada tipo de sensor.
        return true; // Por ahora, consideramos que todas las mediciones están dentro del rango.
    }

    private void generateAlarm(double value) {
        // Implementa la lógica para generar una alarma, por ejemplo, enviándola a un registro o un sistema de alerta.
        // También puedes almacenar las alarmas en una base de datos o en un archivo según tus necesidades.
        // Por ahora, muestra la alarma por pantalla como ejemplo:
        System.out.println("Monitor de " + sensorType + " - Alarma generada: Medición fuera del rango - Valor: " + value);
    }

    public static void main(String[] args) {
        // Ejemplo de cómo iniciar un monitor para el tipo de sensor TEMPERATURE
        Monitor temperatureMonitor = new Monitor(SensorType.TEMPERATURE, "jdbc:mysql://localhost/sensor_data");
        temperatureMonitor.start();
    }
}
