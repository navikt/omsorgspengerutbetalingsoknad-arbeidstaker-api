package no.nav.omsorgspengerutbetaling.kafka

import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer

data class TopicEntry<V>(
    val metadata: Metadata,
    val data: V
)

internal data class TopicUse<V>(
    val name: String,
    val valueSerializer : Serializer<TopicEntry<V>>
) {
    internal fun keySerializer() = StringSerializer()
}

object Topics {
    const val OMS_UT_ARB_MOTTATT = "dusseldorf.privat-omsorgspengerutbetalingsoknad-arbeidstaker-mottatt-v2"
}