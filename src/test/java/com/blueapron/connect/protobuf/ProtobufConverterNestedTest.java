package com.blueapron.connect.protobuf;

import static org.junit.Assert.assertEquals;

import com.blueapron.connect.protobuf.NestedTestProtoOuterClass.ComplexType;
import com.blueapron.connect.protobuf.NestedTestProtoOuterClass.MessageId;
import com.blueapron.connect.protobuf.NestedTestProtoOuterClass.NestedTestProto;
import com.blueapron.connect.protobuf.NestedTestProtoOuterClass.Status;
import com.blueapron.connect.protobuf.NestedTestProtoOuterClass.UserId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.junit.Test;

public class ProtobufConverterNestedTest {
  private final String TEST_NESTED_MESSAGE_CLASS_NAME = "com.blueapron.connect.protobuf.NestedTestProtoOuterClass$NestedTestProto";

  private static final NestedTestProto NESTED_MESSAGE = NestedTestProto.newBuilder()
    .setUserId(UserId.newBuilder().setAnotherId(MessageId.newBuilder().setId("foo").build()).build())
    .setIsActive(true)
    .setComplexType(ComplexType.newBuilder().setOneId("bar").build())
    .build();

  private Schema getNestedMessageSchema(String fieldDelimiter) {
    final SchemaBuilder builder = SchemaBuilder.struct();
    builder.field("user_id" + fieldDelimiter + "ba_com_user_id", SchemaBuilder.string().optional().build());
    builder.field("user_id" + fieldDelimiter + "other_user_id", SchemaBuilder.int32().optional().build());
    builder.field("user_id" + fieldDelimiter + "another_id" + fieldDelimiter + "id", SchemaBuilder.string().optional().build());
    builder.field("is_active", SchemaBuilder.bool().optional().build());
    builder.field("experiments_active", SchemaBuilder.array(SchemaBuilder.string().optional().build()).optional().build());
    builder.field("updated_at", org.apache.kafka.connect.data.Timestamp.builder().optional().build());
    builder.field("status", SchemaBuilder.string().optional().build());
    builder.field("complex_type" + fieldDelimiter + "one_id", SchemaBuilder.string().optional().build());
    builder.field("complex_type" + fieldDelimiter + "other_id", SchemaBuilder.int32().optional().build());
    builder.field("complex_type" + fieldDelimiter + "is_active", SchemaBuilder.bool().optional().build());
    builder.field("map_type", SchemaBuilder.array(SchemaBuilder.struct().field("key", Schema.OPTIONAL_STRING_SCHEMA).field("value", Schema.OPTIONAL_STRING_SCHEMA).optional().build()).optional().build());
    return builder.build();
  }

  private Struct getNestedMessageValue(String fieldDelimiter) {
    Schema schema = getNestedMessageSchema(fieldDelimiter);
    Struct result = new Struct(schema.schema());
    result.put("user_id" + fieldDelimiter + "another_id" + fieldDelimiter + "id", "foo");
    result.put("is_active", true);
    result.put("experiments_active", Collections.emptyList());
    result.put("updated_at", new Date(0));
    result.put("status", Status.ACTIVE.name());
    result.put("complex_type" + fieldDelimiter + "one_id", "bar");
    result.put("complex_type" + fieldDelimiter + "is_active", false);
    result.put("map_type", Collections.emptyList());
    return result;
  }

  private ProtobufConverter getConfiguredProtobufConverter(String protobufClassName, boolean flatConnectSchema, String flatConnectSchemaFieldDelimiter, boolean isKey) {
    ProtobufConverter protobufConverter = new ProtobufConverter();

    Map<String, Object> configs = new HashMap<String, Object>();
    configs.put("protoClassName", protobufClassName);
    configs.put("flatConnectSchema", flatConnectSchema);
    configs.put("flatConnectSchemaFieldDelimiter", flatConnectSchemaFieldDelimiter);

    protobufConverter.configure(configs, isKey);

    return protobufConverter;
  }

  @Test
  public void testToConnectDataForNestedValue() {
    final String fieldDelimiter = "$";

    ProtobufConverter testMessageConverter = getConfiguredProtobufConverter(TEST_NESTED_MESSAGE_CLASS_NAME, true, fieldDelimiter, false);
    SchemaAndValue result = testMessageConverter.toConnectData("my-topic", NESTED_MESSAGE.toByteArray());
    SchemaAndValue expected = new SchemaAndValue(getNestedMessageSchema(fieldDelimiter), getNestedMessageValue(fieldDelimiter));

    assertEquals(expected, result);
  }
}
