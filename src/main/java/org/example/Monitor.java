package org.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.FileWriter;
import java.io.IOException;

public class Monitor {
    private final SensorType sensorType;
    private final String csvFileName; // Nombre del archivo CSV
    private FileWriter csvWriter; // Objeto FileWriter para escribir en el archivo CSV

    public Monitor(SensorType sensorType, String databaseUrl, String csvFileName) {
        this.sensorType = sensorType;
        this.csvFileName = csvFileName;

        try {
            this.csvWriter = new FileWriter(csvFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        // En este ejemplo, solo mostramos un mensaje en la consola.
        System.out.println("Monitor de " + sensorType + " - Medición almacenada: " + value);
    }

    private boolean isMeasurementInRange(double value) {
        return switch (sensorType) {
            case TEMPERATURE -> value >= 68 && value <= 89;
            case PH -> value >= 6 && value <= 8;
            case OXYGEN -> value >= 2 && value <= 11;
        };
    }

    private void generateAlarm(double value) {
        // Genera el mensaje de alarma
        String alarmMessage = generateAlarmMessage(value);
        System.out.println(alarmMessage);
        writeAlarmToCSV(alarmMessage);
    }

    private String generateAlarmMessage(double value) {
        // Crea un mensaje de alarma
        return "Monitor de " + sensorType + " - Alarma generada: Medición fuera del rango - Valor: " + value;
    }

    private void writeAlarmToCSV(String alarmMessage) {
        try {
            // Escribe la alarma en el archivo CSV
            csvWriter.write(alarmMessage + "\n");
            csvWriter.flush(); // Asegura que los datos se escriban en el archivo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        // Ejemplo de cómo iniciar un monitor para el tipo de sensor TEMPERATURE
        Monitor temperatureMonitor = new Monitor(SensorType.TEMPERATURE, "jdbc:mysql://localhost/sensor_data", "temperature_alarms.csv");
        temperatureMonitor.start();
    }
}
