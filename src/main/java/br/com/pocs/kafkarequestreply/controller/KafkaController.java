package br.com.pocs.kafkarequestreply.controller;

import br.com.pocs.kafkarequestreply.dto.PagamentoResponse;
import br.com.pocs.kafkarequestreply.dto.PagamentoDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
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

        ProducerRecord<String, PagamentoDto> producerRecord = new ProducerRecord<>(requestTopic, null, pagamentoDto.getId(), pagamentoDto);

        RequestReplyFuture<String, PagamentoDto, PagamentoResponse> future = replyingKafkaTemplate.sendAndReceive(producerRecord);

        ConsumerRecord<String, PagamentoResponse> response = future.get();

        return new ResponseEntity<>(response.value(), HttpStatus.OK);
    }


}
