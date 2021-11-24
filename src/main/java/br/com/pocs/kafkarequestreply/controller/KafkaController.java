package br.com.pocs.kafkarequestreply.controller;

import br.com.pocs.kafkarequestreply.dto.PagamentoDto;
import br.com.pocs.kafkarequestreply.dto.PagamentoResponse;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
public class KafkaController {

    @Value("${kafka.request.topic}")
    private String requestTopic;

    private final ReplyingKafkaTemplate<String, PagamentoDto, PagamentoResponse> replyingKafkaTemplate;

    @PostMapping("/verificar-situacao")
    public ResponseEntity<PagamentoResponse> getObject(@RequestBody PagamentoDto pagamentoDto)
            throws InterruptedException, ExecutionException {

        //Criar Producer Record
        ProducerRecord<String, PagamentoDto> producerRecord = new ProducerRecord<>(requestTopic, null, pagamentoDto.getId(), pagamentoDto);

        //Add cabeçalho
        producerRecord.headers().add(new RecordHeader("topico.pagamento.resultado", requestTopic.getBytes()));

        //Postar no kafka
        RequestReplyFuture<String, PagamentoDto, PagamentoResponse> replyFuture = replyingKafkaTemplate.sendAndReceive(producerRecord);

        //Callback
        replyFuture.addCallback(new ListenableFutureCallback<ConsumerRecord<String, PagamentoResponse>>() {
            @Override
            public void onFailure(Throwable ex) {
                replyFuture.cancel(true);
                System.out.println("ex: " + ex);
            }

            @Override
            public void onSuccess(ConsumerRecord<String, PagamentoResponse> resultado) {
                // get consumer record value
                PagamentoResponse reply = resultado.value();
                System.out.println("Reply: " + reply.toString());
            }
        });

        //Confirmar se a mensagem foi produzida com sucesso
        SendResult<String, PagamentoDto> sendResult = replyFuture.getSendFuture().get();

        //Log headers
        sendResult.getProducerRecord().headers().forEach(header -> System.out.println(header.key() + ":" + header.value().toString()));

        //Recupera consumer record
        ConsumerRecord<String, PagamentoResponse> response = replyFuture.get();

        return new ResponseEntity<>(response.value(), HttpStatus.OK);
    }


}
