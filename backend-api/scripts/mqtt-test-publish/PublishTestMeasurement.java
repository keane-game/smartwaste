import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Publie UNE mesure de test sur le topic MQTT de test (smartcollect-pikine-test/measurements,
 * broker.hivemq.com) pour vérifier de bout en bout la chaîne capteur -> mesure -> seuil -> alerte.
 *
 * <p>⚠️ N'utilise QUE la clé d'un capteur enrôlé spécialement pour ce test
 * ({@code POST /v1/devices/sensors}), jamais celle d'un capteur réel : le broker est public, sans
 * contrôle d'accès sur ce topic.
 *
 * <p><b>Pour rejouer ce test</b> (backend démarré avec {@code SONAGED_MQTT_ENABLED=true}) :
 * <pre>
 * java -cp "%USERPROFILE%\.m2\repository\org\eclipse\paho\org.eclipse.paho.client.mqttv3\1.2.5\org.eclipse.paho.client.mqttv3-1.2.5.jar" ^
 *      PublishTestMeasurement.java ^
 *      &lt;deviceApiKey&gt; &lt;fillLevelPercent&gt;
 * </pre>
 * (Java 11+ exécute un fichier source unique directement, sans compilation séparée.)
 */
public class PublishTestMeasurement {

    private static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
    private static final String TOPIC = "smartcollect-pikine-test/measurements";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: PublishTestMeasurement <deviceApiKey> <fillLevelPercent>");
            System.exit(1);
        }
        String deviceKey = args[0];
        String fillLevelPercent = args[1];

        String payload = """
                {"deviceKey":"%s","fillLevelPercent":%s,"measuredAt":"%s"}
                """.formatted(deviceKey, fillLevelPercent, java.time.Instant.now());

        MqttClient client = new MqttClient(BROKER_URL, MqttClient.generateClientId(), new MemoryPersistence());
        client.connect();
        MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(1);
        client.publish(TOPIC, message);
        System.out.println("Publié sur " + TOPIC + " : " + payload.trim());
        client.disconnect();
        client.close();
    }
}
