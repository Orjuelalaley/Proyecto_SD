package org.example;

public class Main {
    public static void main(String[] args) {
        // Crear instancias de sensores con intervalos diferentes y puertos distintos
        int temperaturePort = 5556;
        int phPort = 5557;
        int oxygenPort = 5558;

        // Crear instancias de sensores con intervalos diferentes y puertos
        Sensor temperatureSensor = new Sensor(SensorType.TEMPERATURE, 2000, temperaturePort);
        Sensor phSensor = new Sensor(SensorType.PH, 3000, phPort);
        Sensor oxygenSensor = new Sensor(SensorType.OXYGEN, 2500, oxygenPort);

        // Establecer la conexión ZeroMQ en los sensores
        temperatureSensor.connectToMonitor();
        phSensor.connectToMonitor();
        oxygenSensor.connectToMonitor();

        // Iniciar los sensores en hilos separados
        Thread temperatureThread = new Thread(temperatureSensor::start);
        Thread phThread = new Thread(phSensor::start);
        Thread oxygenThread = new Thread(oxygenSensor::start);

        // Iniciar los hilos
        temperatureThread.start();
        phThread.start();
        oxygenThread.start();

        // Esperar a que los hilos terminen (esto no es necesario si tu aplicación sigue ejecutándose)
        try {
            temperatureThread.join();
            phThread.join();
            oxygenThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Ocurrió un error al esperar a que los hilos terminen: " + e.getMessage());
        }
    }
}
