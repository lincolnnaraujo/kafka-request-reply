package br.com.pocs.kafkarequestreply.configs;

import br.com.pocs.kafkarequestreply.dto.PagamentoResponse;
import br.com.pocs.kafkarequestreply.dto.PagamentoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
public class KafkaConfig {

    @Value("${kafka.group.id}")
    private String groupId;

    @Value("${kafka.reply.topic}")
    private String replyTopic;

    @Bean
    public ReplyingKafkaTemplate<String, PagamentoDto, PagamentoResponse> replyingKafkaTemplate(ProducerFactory<String, PagamentoDto> pf,
                                                                                                ConcurrentKafkaListenerContainerFactory<String, PagamentoResponse> factory) {
        ConcurrentMessageListenerContainer<String, PagamentoResponse> replyContainer = factory.createContainer(replyTopic);
        replyContainer.getContainerProperties().setMissingTopicsFatal(false);
        replyContainer.getContainerProperties().setGroupId(groupId);
        return new ReplyingKafkaTemplate<>(pf, replyContainer);
    }
    @Bean
    public KafkaTemplate<String, PagamentoResponse> replyTemplate(ProducerFactory<String, PagamentoResponse> pf,
                                                             ConcurrentKafkaListenerContainerFactory<String, PagamentoResponse> factory) {
        KafkaTemplate<String, PagamentoResponse> kafkaTemplate = new KafkaTemplate<>(pf);
        factory.getContainerProperties().setMissingTopicsFatal(false);
        factory.setReplyTemplate(kafkaTemplate);
        return kafkaTemplate;
    }

}
