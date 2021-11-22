# Poc - Kafka request/reply

### Para iniciar um kafka local:
```sh
cd src/main/resources/

sudo docker-compose up
```

- Com a imagem de pé, acesse para validar: [kafka-lenses](http://127.0.0.1:3030/)

### Request: /pagamento

```json
{
  "id": "string",
  "data": "string",
  "status": "pagamento_incluido"
}
```

#### Delay de 7 segundos para simular um processamento

### Response
```json
{
  "id": "string",
  "data": "string",
  "status": "pagamento_validado"
}
```