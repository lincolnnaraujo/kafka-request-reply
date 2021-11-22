package br.com.pocs.kafkarequestreply.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PagamentoDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("data")
    private String dataPagamento;

    @JsonProperty("situacao")
    private String situacaoPagamento;

}
