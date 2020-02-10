package com.blueapron.connect.protobuf;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.blueapron.connect.protobuf.TestMessageProtos.TestMessage;

import java.util.HashMap;
import java.util.Map;

public class ProtobufConverterTest {
  private final String LEGACY_FIELD_NAME = "legacy_field_name";
  private final String CUSTOM_LEGACY_NAME = "blueapron.connect.protobuf.legacy_name";
  private final String TEST_MESSAGE_CLASS_NAME = "com.blueapron.connect.protobuf.TestMessageProtos$TestMessage";
  private static final String TEST_MSG_STRING = "Hello World";
  private static final String LEGACY_MSG_STRING = "Some renamed field";
  private static final TestMessage HELLO_WORLD_MESSAGE = TestMessage.newBuilder().setTestString(TEST_MSG_STRING).setSomeField(LEGACY_MSG_STRING).build();

  private Schema getTestMessageSchema(String legacyFieldName) {
    final SchemaBuilder builder = SchemaBuilder.struct().name("TestMessage");
    final SchemaBuilder fieldBuilder = SchemaBuilder.string();
    fieldBuilder.optional();
    builder.field("test_string", fieldBuilder.build());
    builder.field(legacyFieldName, fieldBuilder.build());
    return builder.build();
  }

  private Schema getTestMessageSchema() {
    return getTestMessageSchema(LEGACY_FIELD_NAME);
  }

  private Struct getTestMessageResult(String messageText, String legacyFieldText, String legacyFieldName) {
    Schema schema = getTestMessageSchema(legacyFieldName);
    Struct result = new Struct(schema.schema());
    result.put("test_string", messageText);
    result.put(legacyFieldName, legacyFieldText);
    return result;
  }
  private Struct getTestMessageResult(String messageText, String legacyFieldText) {
    return getTestMessageResult(messageText, legacyFieldText, LEGACY_FIELD_NAME);
  }

  private ProtobufConverter getConfiguredProtobufConverter(String protobufClassName, boolean isKey, String legacy_name) {
    ProtobufConverter protobufConverter = new ProtobufConverter();

    Map<String, Object> configs = new HashMap<String, Object>();
    configs.put("protoClassName", protobufClassName);
    configs.put("legacyName", legacy_name);

    protobufConverter.configure(configs, isKey);

    return protobufConverter;
  }

  private ProtobufConverter getConfiguredProtobufConverter(String protobufClassName, boolean isKey) {
    return getConfiguredProtobufConverter(protobufClassName, isKey, CUSTOM_LEGACY_NAME);
  }

  @Test(expected = ConnectException.class)
  public void testNullValueConverter() {
    // When the value converter is null, we need to throw an exception and stop processing
    getConfiguredProtobufConverter(null, false);
  }

  @Test(expected = ConnectException.class)
  public void testInvalidClassKeyConverter() {
    getConfiguredProtobufConverter("com.does.not.exist", true);
  }

  @Test(expected = ConnectException.class)
  public void testInvalidClassValueConverter() {
    getConfiguredProtobufConverter("com.does.not.exist", false);
  }

  @Test(expected = ConnectException.class)
  public void testNonProtoClassValueConverter() {
    getConfiguredProtobufConverter("java.lang.String", true);
  }

  @Test
  public void testFromConnectDataForKey() {
    final byte[] expected = HELLO_WORLD_MESSAGE.toByteArray();

    ProtobufConverter testMessageConverter = getConfiguredProtobufConverter(TEST_MESSAGE_CLASS_NAME, true);
    byte[] result = testMessageConverter.fromConnectData("my-topic",
                                                          getTestMessageSchema(),
                                                          getTestMessageResult(TEST_MSG_STRING, LEGACY_MSG_STRING));

    assertArrayEquals(expected, result);
  }

  @Test
  public void testFromConnectDataWhenKeyIsNull() {
    // When the key is null, the message content will be empty
    ProtobufConverter testMessageConverter = getConfiguredProtobufConverter(null, true);
    byte[] result = testMessageConverter.fromConnectData("my-topic",
      getTestMessageSchema(),
      getTestMessageResult(TEST_MSG_STRING, LEGACY_MSG_STRING));

    assertArrayEquals(null, result);
  }

  @Test
  public void testFromConnectDataForValue() {
    final byte[] expected = HELLO_WORLD_MESSAGE.toByteArray();

    ProtobufConverter testMessageConverter = getConfiguredProtobufConverter(TEST_MESSAGE_CLASS_NAME, false);
    byte[] result = testMessageConverter.fromConnectData("my-topic",
      getTestMessageSchema(),
      getTestMessageResult(TEST_MSG_STRING, LEGACY_MSG_STRING));

    assertArrayEquals(expected, result);
  }

  @Test
  public void testToConnectDataForKey() {
    ProtobufConverter testMessageConverter = getConfiguredProtobufConverter(TEST_MESSAGE_CLASS_NAME, true);
    SchemaAndValue result = testMessageConverter.toConnectData("my-topic", HELLO_WORLD_MESSAGE.toByteArray());

    SchemaAndValue expected = new SchemaAndValue(getTestMessageSchema(), getTestMessageResult(TEST_MSG_STRING, LEGACY_MSG_STRING));

    assertEquals(expected, result);
  }

  @Test
  public void testToConnectDataWhenKeyIsNull() {
    ProtobufConverter testMessageConverter = getConfiguredProtobufConverter(null, true);
    SchemaAndValue result = testMessageConverter.toConnectData("my-topic", HELLO_WORLD_MESSAGE.toByteArray());

    assertEquals(SchemaAndValue.NULL, result);
  }

  @Test
  public void testToConnectDataForValue() {
    ProtobufConverter testMessageConverter = getConfiguredProtobufConverter(TEST_MESSAGE_CLASS_NAME, false);
    SchemaAndValue result = testMessageConverter.toConnectData("my-topic", HELLO_WORLD_MESSAGE.toByteArray());

    SchemaAndValue expected = new SchemaAndValue(getTestMessageSchema(), getTestMessageResult(TEST_MSG_STRING, LEGACY_MSG_STRING));

    assertEquals(expected, result);
  }

  @Test
  public void testToConnectDataForValueWithMismatchedDefaultLegacyName() {
    ProtobufConverter testMessageConverter = getConfiguredProtobufConverter(TEST_MESSAGE_CLASS_NAME, false, "legacy_name");
    SchemaAndValue result = testMessageConverter.toConnectData("my-topic", HELLO_WORLD_MESSAGE.toByteArray());

    String unrenamedLegacyFieldName = "some_field";

    SchemaAndValue expected = new SchemaAndValue(
      getTestMessageSchema(unrenamedLegacyFieldName),
      getTestMessageResult(TEST_MSG_STRING, LEGACY_MSG_STRING, unrenamedLegacyFieldName)
    );

    assertEquals(expected, result);
  }
}
