package com.tradeshift.kafka.connect.protobuf;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.junit.Test;

import com.blueapron.connect.protobuf.ProtobufConverter;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tradeshift.backend.journal.DTO;

public class DecimalConversionTest {

  @Test
  public void test_protobufConverter() throws InvalidProtocolBufferException {
    ProtobufConverter protobufConverter = new ProtobufConverter();
    HashMap<String, Object> configs = new HashMap<>();
    configs.put("protoClassName", "com.tradeshift.backend.journal.DTO$EventEnvelope");
    configs.put("mapFieldsBy", "NAME");
    protobufConverter.configure(configs, false);

    Schema schema = SchemaBuilder.struct()
      .field("aggregateId", Schema.STRING_SCHEMA)
      .field("sequenceNr", Schema.INT32_SCHEMA)
      .field("event", Schema.BYTES_SCHEMA)
      .field("timestamp", Schema.INT64_SCHEMA)
      .build();

    String aggregateId = UUID.randomUUID().toString();
    long timestamp = System.currentTimeMillis();
    final Struct struct = new Struct(schema);
    struct.put("aggregateId", aggregateId);
    struct.put("sequenceNr", 1000);
    struct.put("event", ByteBuffer.wrap("These are bytes which should be protobuf".getBytes(StandardCharsets.UTF_8)));
    struct.put("timestamp", timestamp);
    struct.validate();

    byte[] fromConnectData = protobufConverter.fromConnectData("test_topic", schema, struct);

    DTO.EventEnvelope envelope = DTO.EventEnvelope.parseFrom(fromConnectData);
    assertEquals(aggregateId, envelope.getAggregateId());
    assertEquals(1000, envelope.getSequenceNr());
    assertEquals(timestamp, envelope.getTimestamp());
    assertEquals(ByteString.copyFromUtf8("These are bytes which should be protobuf"), envelope.getEvent());

  }
}
