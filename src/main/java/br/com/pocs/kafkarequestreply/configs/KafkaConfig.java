package br.com.pocs.kafkarequestreply.configs;

import br.com.pocs.kafkarequestreply.dto.PagamentoDto;
import br.com.pocs.kafkarequestreply.dto.PagamentoResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${kafka.group.id}")
    private String groupId;

    @Value("${kafka.reply.topic}")
    private String replyTopic;

    @Value("${kafka.bootstrap.server}")
    private String bootStrapServer;

    @Bean
    public ReplyingKafkaTemplate<String, PagamentoDto, PagamentoResponse> replyingKafkaTemplate(ProducerFactory<String, PagamentoDto> pf,
                                                                                                ConcurrentKafkaListenerContainerFactory<String, PagamentoResponse> factory) {
        ConcurrentMessageListenerContainer<String, PagamentoResponse> replyContainer = factory.createContainer(replyTopic);
        replyContainer.getContainerProperties().setMissingTopicsFatal(false);
        replyContainer.getContainerProperties().setGroupId(groupId);

        //Controle de Time out
        final var replyingKafkaTemplate = new ReplyingKafkaTemplate(pf, replyContainer);
        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofMillis(1000));

        return replyingKafkaTemplate;
    }
    @Bean
    public KafkaTemplate<String, PagamentoResponse> replyTemplate(ProducerFactory<String, PagamentoResponse> pf,
                                                             ConcurrentKafkaListenerContainerFactory<String, PagamentoResponse> factory) {
        KafkaTemplate<String, PagamentoResponse> kafkaTemplate = new KafkaTemplate<>(pf);
        factory.getContainerProperties().setMissingTopicsFatal(false);
        factory.setReplyTemplate(kafkaTemplate);
        return kafkaTemplate;
    }




    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return props;
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ConsumerFactory<String, PagamentoResponse> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(PagamentoResponse.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PagamentoResponse> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PagamentoResponse> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(replyTemplate());
        return factory;
    }

    @Bean
    public ProducerFactory<String, PagamentoDto> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, PagamentoDto> replyTemplate() {
        KafkaTemplate<String, PagamentoDto> kafkaTemplate = new KafkaTemplate<>(producerFactory());
        kafkaTemplate.setDefaultTopic(replyTopic);
        return kafkaTemplate;
    }


}
