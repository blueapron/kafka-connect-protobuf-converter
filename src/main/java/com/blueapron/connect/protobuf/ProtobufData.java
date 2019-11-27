package com.blueapron.connect.protobuf;


import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.apache.kafka.connect.data.Date;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.DataException;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import com.google.protobuf.util.Timestamps;

import static com.google.protobuf.Descriptors.FieldDescriptor.Type.BOOL;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.BYTES;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.DOUBLE;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.FLOAT;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.SINT32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.SINT64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.STRING;


class ProtobufData {
  private final Method newBuilder;
  private final Schema schema;
  private final String legacyName;
  public static final Descriptors.FieldDescriptor.Type[] PROTO_TYPES_WITH_DEFAULTS = new Descriptors.FieldDescriptor.Type[] { INT32, INT64, SINT32, SINT64, FLOAT, DOUBLE, BOOL, STRING, BYTES, ENUM };
  private HashMap<String, String> connectProtoNameMap = new HashMap<String, String>();

  private GeneratedMessageV3.Builder getBuilder() {
    try {
      return (GeneratedMessageV3.Builder) newBuilder.invoke(Object.class);
    } catch (Exception e) {
      throw new ConnectException("Not a valid proto3 builder", e);
    }
  }

  private Message getMessage(byte[] value) {
    try {
      return getBuilder().mergeFrom(value).build();
    } catch (InvalidProtocolBufferException e) {
      throw new DataException("Invalid protobuf data", e);
    }
  }

  private String getProtoMapKey(String descriptorContainingTypeName, String connectFieldName) {
    return descriptorContainingTypeName.concat(connectFieldName);
  }

  private String getConnectFieldName(Descriptors.FieldDescriptor descriptor) {
    String name = descriptor.getName();
    for (Map.Entry<Descriptors.FieldDescriptor, Object> option: descriptor.getOptions().getAllFields().entrySet()) {
      if (option.getKey().getName().equalsIgnoreCase(this.legacyName)) {
        name = option.getValue().toString();
      }
    }

    connectProtoNameMap.put(getProtoMapKey(descriptor.getContainingType().getFullName(), name), descriptor.getName());
    return name;
  }

  private String getProtoFieldName(String descriptorForTypeName, String connectFieldName) {
    return connectProtoNameMap.get(getProtoMapKey(descriptorForTypeName, connectFieldName));
  }

  ProtobufData(Class<? extends com.google.protobuf.GeneratedMessageV3> clazz, String legacyName) {
    this.legacyName = legacyName;

    try {
      this.newBuilder = clazz.getDeclaredMethod("newBuilder");
    } catch (NoSuchMethodException e) {
      throw new ConnectException("Proto class " + clazz.getCanonicalName() + " is not a valid proto3 message class", e);
    }

    this.schema = toConnectSchema(getBuilder().getDefaultInstanceForType());
  }

  SchemaAndValue toConnectData(byte[] value) {
    Message message = getMessage(value);
    if (message == null) {
      return SchemaAndValue.NULL;
    }
    return new SchemaAndValue(this.schema, toConnectData(this.schema, message));
  }

  private Schema toConnectSchema(Message message) {
    final SchemaBuilder builder = SchemaBuilder.struct().name(message.getDescriptorForType().getName());
    final List<Descriptors.FieldDescriptor> fieldDescriptorList = message.getDescriptorForType().getFields();
    for (Descriptors.FieldDescriptor descriptor : fieldDescriptorList) {
      builder.field(getConnectFieldName(descriptor), toConnectSchema(descriptor));
    }

    return builder.build();
  }

  private boolean isTimestampDescriptor(Descriptors.FieldDescriptor descriptor) {
    return descriptor.getMessageType().getFullName().equals("google.protobuf.Timestamp");
  }

  private boolean isDateDescriptor(Descriptors.FieldDescriptor descriptor) {
    return descriptor.getMessageType().getFullName().equals("google.type.Date");
  }

  private Schema toConnectSchema(Descriptors.FieldDescriptor descriptor) {
    final SchemaBuilder builder;

    switch (descriptor.getType()) {
      case INT32:
      case SINT32:
      {
        builder = SchemaBuilder.int32();
        break;
      }

      case INT64:
      case SINT64:
      case UINT32:
      {
        builder = SchemaBuilder.int64();
        break;
      }

      case FLOAT: {
        builder = SchemaBuilder.float32();
        break;
      }

      case DOUBLE: {
        builder = SchemaBuilder.float64();
        break;
      }

      case BOOL: {
        builder = SchemaBuilder.bool();
        break;
      }

      // TODO - Do we need to support byte or short?
      /*case INT8:
        // Encoded as an Integer
        converted = value == null ? null : ((Integer) value).byteValue();
        break;
      case INT16:
        // Encoded as an Integer
        converted = value == null ? null : ((Integer) value).shortValue();
        break;*/

      case STRING:
        builder = SchemaBuilder.string();
        break;

      case BYTES:
        builder = SchemaBuilder.bytes();
        break;

      case ENUM:
        builder = SchemaBuilder.string();
        break;

      case MESSAGE: {
        if (isTimestampDescriptor(descriptor)) {
          builder = Timestamp.builder();
          break;
        }

        if (isDateDescriptor(descriptor)) {
          builder = Date.builder();
          break;
        }
        String jsonName = descriptor.getJsonName();
        builder = SchemaBuilder.struct().name(jsonName.substring(0, 1).toUpperCase() + jsonName.substring(1));
        for (Descriptors.FieldDescriptor fieldDescriptor : descriptor.getMessageType().getFields()) {
          builder.field(getConnectFieldName(fieldDescriptor), toConnectSchema(fieldDescriptor));
        }

        break;
      }

      default:
        throw new DataException("Unknown Connect schema type: " + descriptor.getType());
    }

    builder.optional();
    Schema schema = builder.build();

    if (descriptor.isRepeated()) {
      final SchemaBuilder arrayBuilder = SchemaBuilder.array(schema);
      arrayBuilder.optional();
      schema = arrayBuilder.build();
    }

    return schema;
  }

  private boolean isProtobufTimestamp(Schema schema) {
    return Timestamp.SCHEMA.name().equals(schema.name());
  }

  private boolean isProtobufDate(Schema schema) {
    return Date.SCHEMA.name().equals(schema.name());
  }

  private void setStructField(Schema schema, Message message, Struct result, Descriptors.FieldDescriptor fieldDescriptor) {
    final String fieldName = getConnectFieldName(fieldDescriptor);
    final Field field = schema.field(fieldName);
    Object obj = null;
    if (fieldDescriptor.getType() != MESSAGE || fieldDescriptor.isRepeated() || fieldDescriptor.isMapField() || message.hasField(fieldDescriptor)) {
      obj = toConnectData(field.schema(), message.getField(fieldDescriptor));
    }
    result.put(fieldName, obj);
  }

  Object toConnectData(Schema schema, Object value) {
    try {
      if (isProtobufTimestamp(schema)) {
        com.google.protobuf.Timestamp timestamp = (com.google.protobuf.Timestamp) value;
        return Timestamp.toLogical(schema, Timestamps.toMillis(timestamp));
      }

      if (isProtobufDate(schema)) {
        com.google.type.Date date = (com.google.type.Date) value;
        return ProtobufUtils.convertFromGoogleDate(date);
      }

      Object converted = null;
      switch (schema.type()) {
        // Pass through types
        case INT32: {
          Integer intValue = (Integer) value; // Validate type
          converted = value;
          break;
        }

        case INT64: {
          try {
            Long longValue = (Long) value; // Validate type
            converted = value;
          } catch (ClassCastException e) {
            Integer intValue = (Integer) value; // Validate type
            converted = Integer.toUnsignedLong(intValue);
          }

          break;
        }

        case FLOAT32: {
          Float floatValue = (Float) value; // Validate type
          converted = value;
          break;
        }

        case FLOAT64: {
          Double doubleValue = (Double) value; // Validate type
          converted = value;
          break;
        }

        case BOOLEAN: {
          Boolean boolValue = (Boolean) value; // Validate type
          converted = value;
          break;
        }

        // TODO - Do we need to support byte or short?
        /*case INT8:
          // Encoded as an Integer
          converted = value == null ? null : ((Integer) value).byteValue();
          break;
        case INT16:
          // Encoded as an Integer
          converted = value == null ? null : ((Integer) value).shortValue();
          break;*/

        case STRING:
          if (value instanceof String) {
            converted = value;
          } else if (value instanceof CharSequence
            || value instanceof Enum
            || value instanceof Descriptors.EnumValueDescriptor) {
            converted = value.toString();
          } else {
            throw new DataException("Invalid class for string type, expecting String or "
              + "CharSequence but found " + value.getClass());
          }
          break;

        case BYTES:
          if (value instanceof byte[]) {
            converted = ByteBuffer.wrap((byte[]) value);
          } else if (value instanceof ByteString) {
            final byte[] valueBytes = ((ByteString) value).toByteArray();
            converted = ByteBuffer.wrap(valueBytes);
          } else if (value instanceof ByteBuffer) {
            converted = value;
          } else {
            throw new DataException("Invalid class for bytes type, expecting byte[], ByteString, "
              + "or ByteBuffer but found " + value.getClass());
          }
          break;

        // Used for repeated types
        case ARRAY: {
          final Schema valueSchema = schema.valueSchema();
          final Collection<Object> original = (Collection<Object>) value;
          final List<Object> result = new ArrayList<Object>(original.size());
          for (Object elem : original) {
            result.add(toConnectData(valueSchema, elem));
          }
          converted = result;
          break;
        }

        case STRUCT: {
          final Message message = (Message) value; // Validate type

          final Struct result = new Struct(schema.schema());
          final Descriptors.Descriptor descriptor = message.getDescriptorForType();

          for (OneofDescriptor oneOfDescriptor : descriptor.getOneofs()) {
            final Descriptors.FieldDescriptor fieldDescriptor = message.getOneofFieldDescriptor(oneOfDescriptor);
            setStructField(schema, message, result, fieldDescriptor);
          }

          for (Descriptors.FieldDescriptor fieldDescriptor : descriptor.getFields()) {
            Descriptors.OneofDescriptor oneOfDescriptor = fieldDescriptor.getContainingOneof();
            if (oneOfDescriptor != null) {
              continue;
            }

            setStructField(schema, message, result, fieldDescriptor);
          }

          converted = result;
          break;
        }

        default:
          throw new DataException("Unknown Connect schema type: " + schema.type());
      }

      return converted;
    } catch (ClassCastException e) {
      throw new DataException("Invalid type for " + schema.type() + ": " + value.getClass());
    }
  }

  byte[] fromConnectData(Object value) {
    final com.google.protobuf.GeneratedMessageV3.Builder builder = getBuilder();
    final Struct struct = (Struct) value;

    for (Field field : this.schema.fields()) {
      fromConnectData(builder, field, struct.get(field));
    }

    return builder.build().toByteArray();
  }

  private void fromConnectData(com.google.protobuf.GeneratedMessageV3.Builder builder, Field field, Object value) {
    final String protobufFieldName = getProtoFieldName(builder.getDescriptorForType().getFullName(), field.name());
    final Descriptors.FieldDescriptor fieldDescriptor = builder.getDescriptorForType().findFieldByName(protobufFieldName);
    if (fieldDescriptor == null) {
      // Ignore unknown fields
      return;
    }

    final Schema schema = field.schema();
    final Schema.Type schemaType = schema.type();

    try {
      switch (schemaType) {
        case INT32: {
          if (isProtobufDate(schema)) {
            final java.util.Date date = (java.util.Date) value;
            builder.setField(fieldDescriptor, ProtobufUtils.convertToGoogleDate(date));
            return;
          }

          final Integer intValue = (Integer) value; // Check for correct type
          builder.setField(fieldDescriptor, intValue);
          return;
        }

        case INT64: {
          if (isProtobufTimestamp(schema)) {
            final java.util.Date timestamp = (java.util.Date) value;
            builder.setField(fieldDescriptor, Timestamps.fromMillis(Timestamp.fromLogical(schema, timestamp)));
            return;
          }

          final Long longValue = (Long) value; // Check for correct type
          builder.setField(fieldDescriptor, longValue);
          return;
        }

        case FLOAT32: {
          final Float floatValue = (Float) value; // Check for correct type
          builder.setField(fieldDescriptor, floatValue);
          return;
        }

        case FLOAT64: {
          final Double doubleValue = (Double) value; // Check for correct type
          builder.setField(fieldDescriptor, doubleValue);
          return;
        }

        case BOOLEAN: {
          final Boolean boolValue = (Boolean) value; // Check for correct type
          builder.setField(fieldDescriptor, boolValue);
          return;
        }

        case STRING: {
          final String stringValue = (String) value; // Check for correct type
          builder.setField(fieldDescriptor, stringValue);
          return;
        }

        case BYTES: {
          final ByteBuffer bytesValue = value instanceof byte[] ? ByteBuffer.wrap((byte[]) value) :
            (ByteBuffer) value;
          builder.setField(fieldDescriptor, ByteString.copyFrom(bytesValue));
          return;
        }

        default:
          throw new DataException("Unknown schema type: " + schema.type());
      }
    } catch (ClassCastException e) {
      throw new DataException("Invalid type for " + schema.type() + ": " + value.getClass());
    }
  }
}
