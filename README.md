En primer lugar toca compilar el broker y el sistema de calidad. el broker es el encargado de la conexion entre el sensor (publicador) y el monitor especifico(suscriptor)

camando para correr el broker:java -cp ".;C:\Users\carli\.m2\repository\org\zeromq\jeromq\0.5.2\jeromq-0.5.2.jar" src\main\java\org\example\broker.java

comando para correr el sistema de calidad:java -cp ".;C:\Users\carli\.m2\repository\org\zeromq\jeromq\0.5.2\jeromq-0.5.2.jar" src\main\java\org\example\QualitySystem.java

comando para correr un sensor en especifico:java -cp ".;C:\Users\carli\.m2\repository\org\zeromq\jeromq\0.5.2\jeromq-0.5.2.jar" src\main\java\org\example\Sensor.java OXYGEN 2000 src\main\resources\SensorConfig.properties

tener en cuenta que al lado de la métrica que esta leyendo el sensor toca poner el tiempo que quiere que tarde en enviar las mediciones

comando para correr el monitorjava -cp ".;C:\Users\carli\.m2\repository\org\zeromq\jeromq\0.5.2\jeromq-0.5.2.jar" src\main\java\org\example\Monitor.java OXYGEN

Trabajo escrito: https://docs.google.com/document/d/1hZtaE68MShX3aJfWvCIr1-nCpqKBgRiNcVD3e8n18Hs/edit?usp=sharing
