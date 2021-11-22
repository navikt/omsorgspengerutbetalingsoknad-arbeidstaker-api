package no.nav.omsorgspengerutbetaling.kafka

import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.omsorgspengerutbetaling.felles.formaterStatuslogging
import no.nav.omsorgspengerutbetaling.felles.somJson
import no.nav.omsorgspengerutbetaling.soknad.KomplettSøknad
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serializer
import org.json.JSONObject
import org.slf4j.LoggerFactory

class KafkaProducer(
    val kafkaConfig: KafkaConfig
) : HealthCheck {
    private val NAME = "SøknadProducer"
    private val OMS_UT_ARB_MOTTATT_TOPIC = TopicUse(
        name = Topics.OMS_UT_ARB_MOTTATT,
        valueSerializer = SøknadSerializer()
    )
    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)
    private val producer = KafkaProducer(
        kafkaConfig.producer(NAME),
        OMS_UT_ARB_MOTTATT_TOPIC.keySerializer(),
        OMS_UT_ARB_MOTTATT_TOPIC.valueSerializer
    )

    internal fun produserKafkaMelding(
        komplettSøknad: KomplettSøknad,
        metadata: Metadata
    ) {
        if (metadata.version != 1) throw IllegalStateException("Kan ikke legge melding med versjon ${metadata.version} til rprosessering.")
        val recordMetaData = producer.send(
            ProducerRecord(
                OMS_UT_ARB_MOTTATT_TOPIC.name,
                komplettSøknad.søknadId,
                TopicEntry(
                    metadata = metadata,
                    data = JSONObject(komplettSøknad.somJson())
                )
            )
        ).get()
        logger.info(
            formaterStatuslogging(
                komplettSøknad.søknadId,
                "sendes til topic ${OMS_UT_ARB_MOTTATT_TOPIC.name} med offset '${recordMetaData.offset()}' til partition '${recordMetaData.partition()}'"
            )
        )
    }

    internal fun stop() = producer.close()

    override suspend fun check(): Result {
        return try {
            producer.partitionsFor(OMS_UT_ARB_MOTTATT_TOPIC.name)
            Healthy(NAME, "Tilkobling til Kafka OK!")
        } catch (cause: Throwable) {
            logger.error("Feil ved tilkobling til Kafka", cause)
            UnHealthy(NAME, "Feil ved tilkobling mot Kafka. ${cause.message}")
        }
    }
}

private class SøknadSerializer : Serializer<TopicEntry<JSONObject>> {
    override fun serialize(topic: String, data: TopicEntry<JSONObject>): ByteArray {
        val metadata = JSONObject()
            .put("correlationId", data.metadata.correlationId)
            .put("version", data.metadata.version)

        return JSONObject()
            .put("metadata", metadata)
            .put("data", data.data)
            .toString()
            .toByteArray()
    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun close() {}
}