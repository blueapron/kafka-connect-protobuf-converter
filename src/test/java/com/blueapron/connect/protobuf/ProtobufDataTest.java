package com.blueapron.connect.protobuf;

import com.blueapron.connect.protobuf.NestedTestProtoOuterClass.NestedTestProto;
import com.blueapron.connect.protobuf.TestMessageProtos.TestMessage;
import com.blueapron.connect.protobuf.UInt64ValueOuterClass.UInt64Value;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import java.util.stream.Collectors;
import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.DataException;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.blueapron.connect.protobuf.ProtobufData.CONNECT_DECIMAL_PRECISION_PROP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class ProtobufDataTest {

  private final String LEGACY_NAME = "legacy_name";
  private final String VALUE_FIELD_NAME = "value";
  public static final Schema OPTIONAL_DECIMAL_SCHEMA = Decimal.builder(0).parameter(CONNECT_DECIMAL_PRECISION_PROP, "20").optional().build();

  private SchemaAndValue getExpectedSchemaAndValue(Schema fieldSchema, Object value, String name) {
    final SchemaBuilder schemaBuilder = SchemaBuilder.struct().name(name);
    schemaBuilder.field(VALUE_FIELD_NAME, fieldSchema);
    final Schema schema = schemaBuilder.build();
    Struct expectedResult = new Struct(schema);
    expectedResult.put(VALUE_FIELD_NAME, value);
    return new SchemaAndValue(schema, expectedResult);
  };

  private StringValue createStringValueMessage(String messageText) {
    StringValue.Builder builder = StringValue.newBuilder();
    builder.setValue(messageText);
    return builder.build();
  }

  private NestedTestProto createNestedTestProtoStringUserId() throws ParseException {
    return createNestedTestProto(NestedTestProtoOuterClass.UserId.newBuilder().setBaComUserId("my_user").build());
  }

  private NestedTestProto createNestedTestProtoIntUserId() throws ParseException {
    return createNestedTestProto(NestedTestProtoOuterClass.UserId.newBuilder().setOtherUserId(5).build());
  }

  private NestedTestProto createNestedTestProto(NestedTestProtoOuterClass.UserId id) throws ParseException {
    NestedTestProto.Builder message = NestedTestProto.newBuilder();
    message.setUserId(id);
    message.setIsActive(true);
    message.addExperimentsActive("first experiment");
    message.addExperimentsActive("second experiment");
    message.setStatus(NestedTestProtoOuterClass.Status.INACTIVE);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    java.util.Date date = sdf.parse("2017/09/18");
    Timestamp timestamp = Timestamps.fromMillis(date.getTime());
    message.setUpdatedAt(timestamp);
    message.putMapType("Hello", "World");
    return message.build();
  }

  private TestMessage createLegacyTestProto() throws ParseException {
    return TestMessage.newBuilder()
      .setTestString("hello")
      .setSomeField("goodbye")
      .build();
  }

  private Schema getExpectedNestedTestProtoSchemaStringUserId() {
    return getExpectedNestedTestProtoSchema();
  }

  private Schema getExpectedNestedTestProtoSchemaIntUserId() {
    return getExpectedNestedTestProtoSchema();
  }

  private SchemaBuilder getComplexTypeSchemaBuilder() {
    final SchemaBuilder complexTypeBuilder = SchemaBuilder.struct().name("ComplexType");
    complexTypeBuilder.field("one_id", SchemaBuilder.string().optional().build());
    complexTypeBuilder.field("other_id", SchemaBuilder.int32().optional().build());
    complexTypeBuilder.field("is_active", SchemaBuilder.bool().optional().build());
    return complexTypeBuilder;
  }

  private Schema getExpectedNestedTestProtoSchema() {
    final SchemaBuilder builder = SchemaBuilder.struct().name("NestedTestProto");
    final SchemaBuilder userIdBuilder = SchemaBuilder.struct();
    userIdBuilder.field("ba_com_user_id", SchemaBuilder.string().optional().build());
    userIdBuilder.field("other_user_id", SchemaBuilder.int32().optional().build());
    final SchemaBuilder messageIdBuilder = SchemaBuilder.struct();
    messageIdBuilder.field("id", SchemaBuilder.string().optional().build());
    userIdBuilder.field("another_id", messageIdBuilder.optional().name("AnotherId").build());
    builder.field("user_id", userIdBuilder.optional().name("UserId").build());
    builder.field("is_active", SchemaBuilder.bool().optional().build());
    builder.field("experiments_active", SchemaBuilder.array(SchemaBuilder.string().optional().build()).optional().build());
    builder.field("updated_at", org.apache.kafka.connect.data.Timestamp.builder().optional().build());
    builder.field("status", SchemaBuilder.string().optional().build());
    builder.field("complex_type", getComplexTypeSchemaBuilder().optional().build());
    builder.field("map_type", SchemaBuilder.array(SchemaBuilder.struct().field("key", Schema.OPTIONAL_STRING_SCHEMA).field("value", Schema.OPTIONAL_STRING_SCHEMA).optional().name("MapType").build()).optional().build());
    return builder.build();
  }

  private Schema getLegacyTestSchema() {
    final SchemaBuilder builder = SchemaBuilder.struct().name("TestMessage");
    builder.field("test_string", SchemaBuilder.string().optional().build());
    builder.field("legacy_field_name", SchemaBuilder.string().optional().build());
    return builder.build();
  }

  private List<Struct> getTestKeyValueList(Schema schema) {
    Struct keyValue = new Struct(schema.field("map_type").schema().valueSchema());
    keyValue.put("key", "Hello");
    keyValue.put("value", "World");
    List<Struct> keyValueList = new ArrayList<Struct>();
    keyValueList.add(keyValue);
    return keyValueList;
  }

  private Struct getExpectedNestedProtoResultStringUserId() throws ParseException {
    Schema schema = getExpectedNestedTestProtoSchemaStringUserId();
    Struct result = new Struct(schema.schema());
    Struct userId = new Struct(schema.field("user_id").schema());
    userId.put("ba_com_user_id", "my_user");
    result.put("user_id", userId);
    result.put("is_active", true);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    java.util.Date date = sdf.parse("2017/09/18");
    result.put("updated_at", date);

    List<String> experiments = new ArrayList<String>();
    experiments.add("first experiment");
    experiments.add("second experiment");
    result.put("experiments_active", experiments);

    result.put("status", "INACTIVE");
    result.put("map_type", getTestKeyValueList(schema));
    return result;
  }

  private Struct getExpectedNestedTestProtoResultIntUserId() throws ParseException {
    Schema schema = getExpectedNestedTestProtoSchemaIntUserId();
    Struct result = new Struct(schema.schema());
    Struct userId = new Struct(schema.field("user_id").schema());
    userId.put("other_user_id", 5);
    result.put("user_id", userId);
    result.put("is_active", true);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    java.util.Date date = sdf.parse("2017/09/18");
    result.put("updated_at", date);

    List<String> experiments = new ArrayList<String>();
    experiments.add("first experiment");
    experiments.add("second experiment");
    result.put("experiments_active", experiments);

    result.put("status", "INACTIVE");
    result.put("map_type", getTestKeyValueList(schema));
    return result;
  }

  private NestedTestProtoOuterClass.ComplexType createProtoDefaultOneOf() throws ParseException {
    NestedTestProtoOuterClass.ComplexType.Builder complexTypeBuilder = NestedTestProtoOuterClass.ComplexType.newBuilder();
    complexTypeBuilder.setOtherId(0);
    return complexTypeBuilder.build();
  }

  private NestedTestProtoOuterClass.ComplexType createProtoMultipleSetOneOf() throws ParseException {
    NestedTestProtoOuterClass.ComplexType.Builder complexTypeBuilder = NestedTestProtoOuterClass.ComplexType.newBuilder();
    complexTypeBuilder.setOneId("asdf");
    complexTypeBuilder.setOtherId(0);
    return complexTypeBuilder.build();
  }

  private Struct getExpectedComplexTypeProtoWithDefaultOneOf() {
    Schema schema = getComplexTypeSchemaBuilder().build();
    Struct result = new Struct(schema.schema());
    result.put("other_id", 0);
    result.put("is_active", false);
    return result;
  }

  private void assertSchemasEqual(Schema expectedSchema, Schema actualSchema) {
    assertEquals(expectedSchema.type(), actualSchema.type());
    assertEquals(expectedSchema.isOptional(), actualSchema.isOptional());

    if (expectedSchema.type() == Schema.Type.STRUCT) {
      assertEquals(expectedSchema.fields().size(), actualSchema.fields().size());
      for (int i = 0; i < expectedSchema.fields().size(); ++i) {
        Field expectedField = expectedSchema.fields().get(i);
        Field actualField = actualSchema.field(expectedField.name());
        assertSchemasEqual(expectedField.schema(), actualField.schema());
      }
    } else if (expectedSchema.type() == Schema.Type.ARRAY) {
      assertSchemasEqual(expectedSchema.valueSchema(), actualSchema.valueSchema());
    }
  }

  private List<String> getFieldNames(Schema schema) {
    return schema
      .fields()
      .stream()
      .map(field -> field.name())
      .collect(Collectors.toList());
  }

  @Test
  public void testToConnectDataWithLegacyName() throws ParseException {
    TestMessage message = createLegacyTestProto();
    ProtobufData protobufData = new ProtobufData(TestMessage.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());

    List<String> actualFieldNames = getFieldNames(result.schema());
    List<String> expectedFieldNames = getFieldNames(getLegacyTestSchema());

    assertEquals(expectedFieldNames, actualFieldNames);
  }

  @Test
  public void testToConnectDataWithNestedProtobufMessageAndStringUserId() throws ParseException {
    NestedTestProto message = createNestedTestProtoStringUserId();
    ProtobufData protobufData = new ProtobufData(NestedTestProto.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    Schema expectedSchema = getExpectedNestedTestProtoSchemaStringUserId();
    assertSchemasEqual(expectedSchema, result.schema());
    assertEquals(new SchemaAndValue(getExpectedNestedTestProtoSchemaStringUserId(), getExpectedNestedProtoResultStringUserId()), result);
  }

  @Test
  public void testToConnectDataWithNestedProtobufMessageAndIntUserId() throws ParseException {
    NestedTestProto message = createNestedTestProtoIntUserId();
    ProtobufData protobufData = new ProtobufData(NestedTestProto.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertSchemasEqual(getExpectedNestedTestProtoSchemaIntUserId(), result.schema());
    assertEquals(new SchemaAndValue(getExpectedNestedTestProtoSchemaIntUserId(), getExpectedNestedTestProtoResultIntUserId()), result);
  }

  @Test
  public void testToConnectDataDefaultOneOf() throws ParseException {
    Schema schema = getComplexTypeSchemaBuilder().build();
    NestedTestProtoOuterClass.ComplexType message = createProtoDefaultOneOf();
    ProtobufData protobufData = new ProtobufData(NestedTestProtoOuterClass.ComplexType.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertSchemasEqual(schema, result.schema());
    assertEquals(new SchemaAndValue(schema, getExpectedComplexTypeProtoWithDefaultOneOf()), result);
  }

  @Test
  public void testToConnectDataDefaultOneOfCannotHaveTwoOneOfsSet() throws ParseException {
    Schema schema = getComplexTypeSchemaBuilder().build();
    NestedTestProtoOuterClass.ComplexType message = createProtoMultipleSetOneOf();
    ProtobufData protobufData = new ProtobufData(NestedTestProtoOuterClass.ComplexType.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertSchemasEqual(schema, result.schema());
    assertEquals(new SchemaAndValue(schema, getExpectedComplexTypeProtoWithDefaultOneOf()), result);
  }

  // Data Conversion tests
  @Test
  public void testToConnectSupportsOptionalValues() {
    ProtobufData protobufData = new ProtobufData(NestedTestProto.class, LEGACY_NAME);
    Schema schema = SchemaBuilder.OPTIONAL_BOOLEAN_SCHEMA.schema();
    assertNull(protobufData.toConnectData(schema, null));
  }

  @Test
  public void testToConnectBoolean() {
    Boolean expectedValue = true;
    String expectedName = "BoolValue";
    BoolValue.Builder builder = BoolValue.newBuilder();
    builder.setValue(expectedValue);
    BoolValue message = builder.build();

    ProtobufData protobufData = new ProtobufData(BoolValue.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_BOOLEAN_SCHEMA, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectInt32() {
    Integer expectedValue = 12;
    String expectedName = "Int32Value";
    Int32Value.Builder builder = Int32Value.newBuilder();
    builder.setValue(expectedValue);
    Int32Value message = builder.build();

    ProtobufData protobufData = new ProtobufData(Int32Value.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_INT32_SCHEMA, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectInt32With0() {
    Integer expectedValue = 0;
    String expectedName = "Int32Value";
    Int32Value.Builder builder = Int32Value.newBuilder();
    builder.setValue(expectedValue);
    Int32Value message = builder.build();

    ProtobufData protobufData = new ProtobufData(Int32Value.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_INT32_SCHEMA, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectInt32WithSint32() {
    int expectedValue = 12;
    String expectedName = "SInt32Value";
    SInt32ValueOuterClass.SInt32Value.Builder builder = SInt32ValueOuterClass.SInt32Value.newBuilder();
    builder.setValue(expectedValue);
    SInt32ValueOuterClass.SInt32Value message = builder.build();

    ProtobufData protobufData = new ProtobufData(SInt32ValueOuterClass.SInt32Value.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_INT32_SCHEMA, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectInt32WithUInt32() {
    final Long UNSIGNED_RESULT = 4294967295L;
    String expectedName = "UInt32Value";
    Integer expectedValue = -1;
    UInt32ValueOuterClass.UInt32Value.Builder builder = UInt32ValueOuterClass.UInt32Value.newBuilder();
    builder.setValue(expectedValue);
    UInt32ValueOuterClass.UInt32Value message = builder.build();

    ProtobufData protobufData = new ProtobufData(UInt32ValueOuterClass.UInt32Value.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_INT64_SCHEMA, UNSIGNED_RESULT, expectedName), result);
  }

  @Test
  public void testToConnectInt64() {
    Long expectedValue = 12L;
    String expectedName = "Int64Value";
    Int64Value.Builder builder = Int64Value.newBuilder();
    builder.setValue(expectedValue);
    Int64Value message = builder.build();

    ProtobufData protobufData = new ProtobufData(Int64Value.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_INT64_SCHEMA, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectUInt64() {
    BigDecimal expectedValue = BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.valueOf(1));
    String expectedName = "UInt64Value";

    UInt64Value.Builder builder = UInt64Value.newBuilder();
    builder.setValue(expectedValue.longValue());
    UInt64Value message = builder.build();

    ProtobufData protobufData = new ProtobufData(UInt64Value.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());

    SchemaAndValue expectedSchemaAndValue = getExpectedSchemaAndValue(OPTIONAL_DECIMAL_SCHEMA, expectedValue, expectedName);
    assertEquals(expectedSchemaAndValue, result);
  }

  @Test
  public void testToConnectSInt64() {
    Long expectedValue = 12L;
    String expectedName = "SInt64Value";
    SInt64ValueOuterClass.SInt64Value.Builder builder = SInt64ValueOuterClass.SInt64Value.newBuilder();
    builder.setValue(expectedValue);
    SInt64ValueOuterClass.SInt64Value message = builder.build();

    ProtobufData protobufData = new ProtobufData(SInt64ValueOuterClass.SInt64Value.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_INT64_SCHEMA, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectFloat32() {
    Float expectedValue = 12.f;
    String expectedName = "FloatValue";
    FloatValue.Builder builder = FloatValue.newBuilder();
    builder.setValue(expectedValue);
    FloatValue message = builder.build();

    ProtobufData protobufData = new ProtobufData(FloatValue.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_FLOAT32_SCHEMA, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectFloat64() {
    Double expectedValue = 12.0;
    String expectedName = "DoubleValue";
    DoubleValue.Builder builder = DoubleValue.newBuilder();
    builder.setValue(expectedValue);
    DoubleValue message = builder.build();

    ProtobufData protobufData = new ProtobufData(DoubleValue.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_FLOAT64_SCHEMA, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectString() {
    String expectedValue = "Hello";
    String expectedName = "StringValue";
    StringValue message = createStringValueMessage(expectedValue);

    ProtobufData protobufData = new ProtobufData(StringValue.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_STRING_SCHEMA, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectEmptyString() {
    String expectedValue = "";
    String expectedName = "StringValue";
    StringValue message = createStringValueMessage(expectedValue);

    ProtobufData protobufData = new ProtobufData(StringValue.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());
    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_STRING_SCHEMA, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectTimestamp() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    java.util.Date expectedValue = sdf.parse("2017/12/31");
    String expectedName = "TimestampValue";

    Timestamp timestamp = Timestamps.fromMillis(expectedValue.getTime());
    TimestampValueOuterClass.TimestampValue.Builder builder = TimestampValueOuterClass.TimestampValue.newBuilder();
    builder.setValue(timestamp);
    TimestampValueOuterClass.TimestampValue message = builder.build();

    ProtobufData protobufData = new ProtobufData(TimestampValueOuterClass.TimestampValue.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());

    Schema timestampSchema = org.apache.kafka.connect.data.Timestamp.builder().optional().build();
    assertEquals(getExpectedSchemaAndValue(timestampSchema, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectNullTimestamp() {
    String expectedName = "TimestampValue";
    TimestampValueOuterClass.TimestampValue message = TimestampValueOuterClass.TimestampValue.getDefaultInstance();

    ProtobufData protobufData = new ProtobufData(TimestampValueOuterClass.TimestampValue.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());

    Schema timestampSchema = org.apache.kafka.connect.data.Timestamp.builder().optional().build();
    assertEquals(getExpectedSchemaAndValue(timestampSchema, null, expectedName), result);
  }

  @Test
  public void testToConnectEpochTimestamp() {
    java.util.Date expectedValue = java.util.Date.from(Instant.EPOCH);
    String expectedName = "TimestampValue";

    Timestamp timestamp = Timestamp.getDefaultInstance();
    TimestampValueOuterClass.TimestampValue.Builder builder = TimestampValueOuterClass.TimestampValue.newBuilder();
    builder.setValue(timestamp);
    TimestampValueOuterClass.TimestampValue message = builder.build();

    ProtobufData protobufData = new ProtobufData(TimestampValueOuterClass.TimestampValue.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());

    Schema timestampSchema = org.apache.kafka.connect.data.Timestamp.builder().optional().build();
    assertEquals(getExpectedSchemaAndValue(timestampSchema, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectDate() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    java.util.Date expectedValue = sdf.parse("2017/12/31");
    String expectedName = "DateValue";

    com.google.type.Date.Builder dateBuilder = com.google.type.Date.newBuilder();
    dateBuilder.setYear(2017);
    dateBuilder.setMonth(12);
    dateBuilder.setDay(31);

    DateValueOuterClass.DateValue.Builder builder = DateValueOuterClass.DateValue.newBuilder();
    builder.setValue(dateBuilder.build());
    DateValueOuterClass.DateValue message = builder.build();

    ProtobufData protobufData = new ProtobufData(DateValueOuterClass.DateValue.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());

    Schema dateSchema = org.apache.kafka.connect.data.Date.builder().optional().build();
    assertEquals(getExpectedSchemaAndValue(dateSchema, expectedValue, expectedName), result);
  }

  @Test
  public void testToConnectBytes() {
    byte[] bytes = "foo".getBytes();
    ByteBuffer expectedValue = ByteBuffer.wrap(bytes);
    String expectedName = "BytesValue";

    ByteString byteString = ByteString.copyFrom(ByteBuffer.wrap(bytes));
    BytesValueOuterClass.BytesValue.Builder builder = BytesValueOuterClass.BytesValue.newBuilder();
    builder.setValue(byteString);
    BytesValueOuterClass.BytesValue message = builder.build();

    ProtobufData protobufData = new ProtobufData(BytesValueOuterClass.BytesValue.class, LEGACY_NAME);
    SchemaAndValue result = protobufData.toConnectData(message.toByteArray());

    assertEquals(getExpectedSchemaAndValue(Schema.OPTIONAL_BYTES_SCHEMA, expectedValue, expectedName), result);
  }

  private Schema getValueSchema(Schema schema) {
    final SchemaBuilder schemaBuilder = SchemaBuilder.struct();
    schemaBuilder.field(VALUE_FIELD_NAME, schema);
    return schemaBuilder.build();
  }

  @Test(expected = DataException.class)
  public void testToConnectSchemaMismatchPrimitive() {
    ProtobufData protobufData = new ProtobufData(NestedTestProto.class, LEGACY_NAME);
    Schema schema = Schema.OPTIONAL_FLOAT32_SCHEMA;
    protobufData.toConnectData(schema, 12L);
  }

  @Test(expected = DataException.class)
  public void testToConnectSchemaMismatchArray() {
    ProtobufData protobufData = new ProtobufData(NestedTestProto.class, LEGACY_NAME);
    Schema schema = SchemaBuilder.array(Schema.OPTIONAL_STRING_SCHEMA).build();
    protobufData.toConnectData(schema, Arrays.asList(1, 2, 3));
  }

  private Struct wrapValueStruct(Schema schema, Object value) {
    Schema structSchema = SchemaBuilder.struct().field(VALUE_FIELD_NAME, schema).build();
    Struct struct = new Struct(structSchema.schema());
    struct.put(VALUE_FIELD_NAME, value);
    return struct;
  }

  @Test(expected = DataException.class)
  public void testFromConnectInt8() throws InvalidProtocolBufferException {
    // Unsupported type
    Byte value = 15;
    Struct struct = wrapValueStruct(Schema.OPTIONAL_INT8_SCHEMA, value);

    ProtobufData protobufData = new ProtobufData(Int32Value.class, LEGACY_NAME);
    protobufData.fromConnectData(struct);
  }

  @Test(expected = DataException.class)
  public void testFromConnectInt16() throws InvalidProtocolBufferException {
    // Unsupported type
    Short value = 15;
    Struct struct = wrapValueStruct(Schema.OPTIONAL_INT16_SCHEMA, value);

    ProtobufData protobufData = new ProtobufData(Int32Value.class, LEGACY_NAME);
    protobufData.fromConnectData(struct);
  }

  @Test
  public void testFromConnectInt32() throws InvalidProtocolBufferException {
    Integer value = 15;
    Struct struct = wrapValueStruct(Schema.OPTIONAL_INT32_SCHEMA, value);

    ProtobufData protobufData = new ProtobufData(Int32Value.class, LEGACY_NAME);
    byte[] messageBytes = protobufData.fromConnectData(struct);
    Message message = Int32Value.parseFrom(messageBytes);

    assertEquals(1, message.getAllFields().size());

    Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(VALUE_FIELD_NAME);
    assertEquals(value, message.getField(fieldDescriptor));
  }

  @Test
  public void testFromConnectInt64() throws InvalidProtocolBufferException {
    Long value = 15L;
    Struct struct = wrapValueStruct(Schema.OPTIONAL_INT64_SCHEMA, value);

    ProtobufData protobufData = new ProtobufData(Int64Value.class, LEGACY_NAME);
    byte[] messageBytes = protobufData.fromConnectData(struct);
    Message message = Int64Value.parseFrom(messageBytes);

    assertEquals(1, message.getAllFields().size());

    Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(VALUE_FIELD_NAME);
    assertEquals(value, message.getField(fieldDescriptor));
  }

  @Test
  public void testFromConnectTimestamp() throws ParseException, InvalidProtocolBufferException {
    Schema timestampSchema = org.apache.kafka.connect.data.Timestamp.builder().optional().build();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    java.util.Date value = sdf.parse("2017/09/18");

    Struct struct = wrapValueStruct(timestampSchema.schema(), value);

    ProtobufData protobufData = new ProtobufData(TimestampValueOuterClass.TimestampValue.class, LEGACY_NAME);
    Message message = TimestampValueOuterClass.TimestampValue.parseFrom(protobufData.fromConnectData(struct));
    assertEquals(1, message.getAllFields().size());

    Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(VALUE_FIELD_NAME);
    assertEquals(Timestamps.fromMillis(value.getTime()), message.getField(fieldDescriptor));
  }

  @Test
  public void testFromConnectDate() throws ParseException, InvalidProtocolBufferException {
    Schema dateSchema = org.apache.kafka.connect.data.Date.builder().optional().build();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    java.util.Date value = sdf.parse("2017/09/18");

    Struct struct = wrapValueStruct(dateSchema.schema(), value);

    ProtobufData protobufData = new ProtobufData(DateValueOuterClass.DateValue.class, LEGACY_NAME);
    Message message = DateValueOuterClass.DateValue.parseFrom(protobufData.fromConnectData(struct));
    assertEquals(1, message.getAllFields().size());

    Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(VALUE_FIELD_NAME);
    assertEquals(ProtobufUtils.convertToGoogleDate(value), message.getField(fieldDescriptor));
  }

  @Test
  public void testFromConnectFloat32() throws InvalidProtocolBufferException {
    Float value = 12.3f;
    Struct struct = wrapValueStruct(Schema.OPTIONAL_FLOAT32_SCHEMA, value);

    ProtobufData protobufData = new ProtobufData(FloatValue.class, LEGACY_NAME);
    byte[] messageBytes = protobufData.fromConnectData(struct);
    Message message = FloatValue.parseFrom(messageBytes);

    assertEquals(1, message.getAllFields().size());

    Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(VALUE_FIELD_NAME);
    assertEquals(value, message.getField(fieldDescriptor));
  }

  @Test
  public void testFromConnectFloat64() throws InvalidProtocolBufferException {
    Double value = 12.3;
    Struct struct = wrapValueStruct(Schema.OPTIONAL_FLOAT64_SCHEMA, value);

    ProtobufData protobufData = new ProtobufData(DoubleValue.class, LEGACY_NAME);
    byte[] messageBytes = protobufData.fromConnectData(struct);
    Message message = DoubleValue.parseFrom(messageBytes);

    assertEquals(1, message.getAllFields().size());

    Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(VALUE_FIELD_NAME);
    assertEquals(value, message.getField(fieldDescriptor));
  }

  @Test
  public void testFromConnectBoolean() throws InvalidProtocolBufferException {
    Boolean value = true;
    Struct struct = wrapValueStruct(Schema.OPTIONAL_BOOLEAN_SCHEMA, value);

    ProtobufData protobufData = new ProtobufData(BoolValue.class, LEGACY_NAME);
    byte[] messageBytes = protobufData.fromConnectData(struct);
    Message message = BoolValue.parseFrom(messageBytes);

    assertEquals(1, message.getAllFields().size());

    Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(VALUE_FIELD_NAME);
    assertEquals(value, message.getField(fieldDescriptor));
  }

  @Test
  public void testFromConnectBooleanWithFalse() throws InvalidProtocolBufferException {
    Boolean value = false;
    Struct struct = wrapValueStruct(Schema.OPTIONAL_BOOLEAN_SCHEMA, value);

    ProtobufData protobufData = new ProtobufData(BoolValue.class, LEGACY_NAME);
    byte[] messageBytes = protobufData.fromConnectData(struct);
    Message message = BoolValue.parseFrom(messageBytes);

    Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(VALUE_FIELD_NAME);
    assertEquals(value, message.getField(fieldDescriptor));
  }

  @Test
  public void testFromConnectString() throws InvalidProtocolBufferException {
    String value = "Hello";
    Struct struct = wrapValueStruct(Schema.OPTIONAL_STRING_SCHEMA, value);

    ProtobufData protobufData = new ProtobufData(StringValue.class, LEGACY_NAME);
    byte[] messageBytes = protobufData.fromConnectData(struct);
    Message message = StringValue.parseFrom(messageBytes);

    assertEquals(1, message.getAllFields().size());

    Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(VALUE_FIELD_NAME);
    assertEquals(value, message.getField(fieldDescriptor));
  }

  /*@Test
  public void testFromConnectEmptyString() throws InvalidProtocolBufferException {
    String value = "";
    Struct struct = wrapValueStruct(Schema.OPTIONAL_STRING_SCHEMA, value);

    ProtobufData protobufData = new ProtobufData(StringValue.class, LEGACY_NAME);
    byte[] messageBytes = protobufData.fromConnectData(struct);
    Message message = StringValue.parseFrom(messageBytes);

    assertEquals(1, message.getAllFields().size());

    Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(VALUE_FIELD_NAME);
    assertEquals(value, message.getField(fieldDescriptor));
  }*/

  @Test
  public void testFromConnectBytes() throws InvalidProtocolBufferException {
    byte[] value = ByteBuffer.wrap("foo".getBytes()).array();
    Struct struct = wrapValueStruct(Schema.OPTIONAL_BYTES_SCHEMA, value);

    ProtobufData protobufData = new ProtobufData(BytesValue.class, LEGACY_NAME);
    byte[] messageBytes = protobufData.fromConnectData(struct);
    Message message = BytesValue.parseFrom(messageBytes);

    assertEquals(1, message.getAllFields().size());

    Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(VALUE_FIELD_NAME);
    assertEquals(ByteString.copyFrom(value), message.getField(fieldDescriptor));
  }

  @Test(expected = DataException.class)
  public void testFromConnectDataMismatchPrimitive() {
    Struct struct = wrapValueStruct(Schema.OPTIONAL_INT64_SCHEMA, 12L);

    ProtobufData protobufData = new ProtobufData(BoolValue.class, LEGACY_NAME);
    protobufData.fromConnectData(struct);
  }

  @Test(expected = DataException.class)
  public void testFromConnectDataUnsupportedSchemaType() throws ParseException {
    // UserId and ComplexType are structs, which are unsupported
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    java.util.Date value = sdf.parse("2017/09/18");

    Struct struct = new Struct(getExpectedNestedTestProtoSchema());
    struct.put("updated_at", value);

    ProtobufData protobufData = new ProtobufData(NestedTestProto.class, LEGACY_NAME);
    byte[] messageBytes = protobufData.fromConnectData(struct);
  }
}
