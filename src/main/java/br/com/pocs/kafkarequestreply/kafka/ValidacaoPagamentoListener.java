package br.com.pocs.kafkarequestreply.kafka;

import br.com.pocs.kafkarequestreply.dto.PagamentoResponse;
import br.com.pocs.kafkarequestreply.dto.PagamentoDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class ValidacaoPagamentoListener {

    @KafkaListener(topics = "${kafka.request.topic}", groupId = "${kafka.group.id}")
    @SendTo
    public PagamentoResponse handle(PagamentoDto dto) throws InterruptedException {

        System.out.println("Validando Pagamento: "+ dto);
        Thread.sleep(7000);

        PagamentoResponse response = new PagamentoResponse();
        response.setId(dto.getId());
        response.setDataPagamento(dto.getDataPagamento());
        response.setSituacaoPagamento("pagamento_validado");

        System.out.println("Pagamento validado: " + response);

        return response;
    }

}
