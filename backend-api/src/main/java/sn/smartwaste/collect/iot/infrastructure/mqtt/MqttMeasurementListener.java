package sn.smartwaste.collect.iot.infrastructure.mqtt;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import sn.smartwaste.collect.iot.application.service.MeasurementIngestionService;

/**
 * Voie d'ingestion MQTT — <b>en parallèle</b> de {@code MeasurementIngestionController} (REST),
 * pas un remplacement. Sert à tester la chaîne capteur → mesure → seuil → alerte contre un broker
 * public, sans matériel physique : les deux voies convergent vers le même
 * {@link MeasurementIngestionService#ingest}, donc le même événement de domaine {@code
 * MeasurementRecorded}, donc la même évaluation de seuil ({@code ThresholdResolver}/{@code
 * FillLevelProjector}) — aucune règle métier n'est dupliquée ici, ce composant ne fait que
 * traduire un message MQTT en cet appel unique.
 *
 * <p><b>Broker choisi : {@code broker.hivemq.com}</b> (port 1883, non chiffré). Justification :
 * gratuit, sans inscription, bonne disponibilité pour un usage de test, client web de vérification
 * disponible ({@code www.hivemq.com/demos/websocket-client/}), et — point important pour un test
 * qu'on veut rejouable — aucune rétention de message par défaut, donc rien ne "traîne" sur le
 * topic entre deux essais. TLS (8883) existe si besoin, non nécessaire ici : voir l'avertissement
 * ci-dessous, aucune donnée sensible n'est censée transiter par ce canal de toute façon.
 *
 * <p><b>⚠️ Broker PUBLIC — jamais de donnée réelle ni sensible sur ce topic.</b> {@code
 * broker.hivemq.com} n'a aucune liste de contrôle d'accès : quiconque connaît le nom du topic peut
 * y publier ou s'y abonner. Le topic est préfixé ({@code smartcollect-pikine-test/}) pour limiter
 * les collisions accidentelles avec d'autres utilisateurs du broker, <b>pas</b> pour garantir une
 * confidentialité qui n'existe pas sur ce canal. La clé de capteur transmise dans la charge utile
 * doit donc être une clé <b>dédiée au test</b> (un capteur enrôlé spécialement,
 * {@code POST /v1/devices/sensors}), jamais celle d'un capteur réellement déployé sur le terrain.
 *
 * <p>Désactivée par défaut ({@code sonaged.mqtt.enabled=false}) : un test ou une CI hors-ligne ne
 * doit jamais dépendre d'Internet ni d'un service tiers non maîtrisé. À activer explicitement
 * (variable d'environnement {@code SONAGED_MQTT_ENABLED=true}) pour une session de test IoT.
 */
@Component
@ConditionalOnProperty(prefix = "sonaged.mqtt", name = "enabled", havingValue = "true")
@Slf4j
public class MqttMeasurementListener {

    private final MeasurementIngestionService ingestionService;
    private final ObjectMapper objectMapper;
    private final String brokerUrl;
    private final String topic;
    private final String clientId;

    private MqttClient client;

    public MqttMeasurementListener(MeasurementIngestionService ingestionService,
                                   ObjectMapper objectMapper,
                                   @Value("${sonaged.mqtt.broker-url}") String brokerUrl,
                                   @Value("${sonaged.mqtt.topic}") String topic,
                                   @Value("${sonaged.mqtt.client-id}") String clientId) {
        this.ingestionService = ingestionService;
        this.objectMapper = objectMapper;
        this.brokerUrl = brokerUrl;
        this.topic = topic;
        this.clientId = clientId;
    }

    @PostConstruct
    public void connect() {
        try {
            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            client.connect(options);
            client.subscribe(topic, 1, (receivedTopic, message) -> handle(message));
            log.info("MQTT : abonné à [{}] sur {}", topic, brokerUrl);
        } catch (MqttException e) {
            // Un broker public de test peut être indisponible sans que ce soit notre panne, et
            // cette voie est explicitement optionnelle : ne jamais empêcher le démarrage de l'API
            // pour elle. La voie REST (MeasurementIngestionController) reste seule active.
            log.error("MQTT : connexion/abonnement impossible ({}) — voie REST seule active",
                    e.getMessage());
        }
    }

    private void handle(MqttMessage message) {
        Payload payload;
        try {
            payload = objectMapper.readValue(message.getPayload(), Payload.class);
        } catch (Exception e) {
            // Un message MQTT malformé ne doit ni planter le listener ni le désabonner : les
            // messages suivants doivent continuer à être traités normalement.
            log.warn("MQTT : message illisible, ignoré ({})", e.getMessage());
            return;
        }
        try {
            boolean created = ingestionService.ingest(payload.deviceKey(), payload.fillLevelPercent(),
                    payload.temperatureCelsius(), payload.humidityPercent(), payload.measuredAt());
            log.info("MQTT : mesure {} pour le capteur transmis", created ? "enregistrée" : "doublon ignoré");
        } catch (Exception e) {
            // Clé inconnue, capteur désactivé, charge utile hors bornes (ResponseStatusException
            // côté service) : le même refus qu'un POST REST équivalent, journalisé plutôt que
            // renvoyé — MQTT n'a pas d'équivalent de code de statut HTTP à répondre à l'émetteur.
            log.warn("MQTT : mesure rejetée ({})", e.getMessage());
        }
    }

    @PreDestroy
    public void disconnect() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                log.warn("MQTT : déconnexion propre échouée ({})", e.getMessage());
            }
        }
    }

    /**
     * Même contrat que {@code MeasurementIngestionController.MeasurementRequest}, plus la clé de
     * capteur : MQTT n'a pas d'équivalent portable de l'en-tête HTTP {@code X-Device-Key} sur un
     * broker public sans authentification par client, donc la clé voyage dans la charge utile.
     */
    record Payload(String deviceKey, Integer fillLevelPercent, Double temperatureCelsius,
                   Double humidityPercent, Instant measuredAt) { }
}
