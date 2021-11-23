package br.com.pocs.kafkarequestreply.kafka;

import br.com.pocs.kafkarequestreply.dto.PagamentoDto;
import br.com.pocs.kafkarequestreply.dto.PagamentoResponse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class ValidacaoPagamentoListener {

    @KafkaListener(topics = "${kafka.request.topic}", groupId = "${kafka.group.id}")
    @SendTo
    public PagamentoResponse handle(ConsumerRecord<String, PagamentoDto> responseConsumerRecord, @Header(KafkaHeaders.CORRELATION_ID) byte[] correlation) throws InterruptedException {

        responseConsumerRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlation);

        System.out.println("Validando Pagamento: "+ responseConsumerRecord);
        Thread.sleep(7000);

        PagamentoResponse response = new PagamentoResponse();
        response.setId(responseConsumerRecord.value().getId());
        response.setDataPagamento(responseConsumerRecord.value().getDataPagamento());
        response.setSituacaoPagamento("pagamento_validado");

        System.out.println("Pagamento validado: " + response);

        return response;
    }

}
