package com.blueapron.connect.protobuf;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.storage.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * Implementation of Converter that uses Protobufs.
 */
public class ProtobufConverter implements Converter {
  private static final Logger log = LoggerFactory.getLogger(ProtobufConverter.class);
  private static final String PROTO_CLASS_NAME_CONFIG = "protoClassName";
  private static final String LEGACY_NAME_CONFIG = "legacyName";

  private static final String FLAT_CONNECT_SCHEMA_CONFIG = "flatConnectSchema";
  private static final boolean FLAT_CONNECT_SCHEMA_DEFAULT = false;
  private static final String FLAT_CONNECT_SCHEMA_FIELD_DELIMITER_CONFIG = "flatConnectSchemaFieldDelimiter";
  private static final String FLAT_CONNECT_SCHEMA_FIELD_DELIMITER_DEFAULT = "__";

  private ProtobufData protobufData;
  private boolean flatConnectSchema = FLAT_CONNECT_SCHEMA_DEFAULT;
  private String flatConnectSchemaFieldDelimiter = FLAT_CONNECT_SCHEMA_FIELD_DELIMITER_DEFAULT;

  private boolean isInvalidConfiguration(Object proto, boolean isKey) {
    return proto == null && !isKey;
  }

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    Object legacyName = configs.get(LEGACY_NAME_CONFIG);
    String legacyNameString = legacyName == null ? "legacy_name" : legacyName.toString();

    Object protoClassName = configs.get(PROTO_CLASS_NAME_CONFIG);
    if (isInvalidConfiguration(protoClassName, isKey)) {
      throw new ConnectException("Value converter must have a " + PROTO_CLASS_NAME_CONFIG + " configured");
    }

    Object flatConnectSchemaConfig = configs.get(FLAT_CONNECT_SCHEMA_CONFIG);
    if (flatConnectSchemaConfig != null) {
      flatConnectSchema = Boolean.valueOf(flatConnectSchemaConfig.toString());
    }

    Object flatFieldDelimiterConfig = configs.get(FLAT_CONNECT_SCHEMA_FIELD_DELIMITER_CONFIG);
    if (flatFieldDelimiterConfig != null) {
      flatConnectSchemaFieldDelimiter = flatFieldDelimiterConfig.toString();
    }

    if (protoClassName == null) {
      protobufData = null;
      return;
    }

    String protoClassNameString = protoClassName.toString();
    try {
      protobufData = new ProtobufData(Class.forName(protoClassNameString).asSubclass(com.google.protobuf.GeneratedMessageV3.class), legacyNameString);
    } catch (ClassNotFoundException e) {
      throw new ConnectException("Proto class " + protoClassNameString + " not found in the classpath");
    } catch (ClassCastException e) {
      throw new ConnectException("Proto class " + protoClassNameString + " is not a valid proto3 message class");
    }
  }

  @Override
  public byte[] fromConnectData(String topic, Schema schema, Object value) {
    if (protobufData == null || schema == null || value == null) {
      return null;
    }

    return protobufData.fromConnectData(value);
  }

  @Override
  public SchemaAndValue toConnectData(String topic, byte[] value) {
    if (protobufData == null || value == null) {
      return SchemaAndValue.NULL;
    }

    SchemaAndValue connectData = protobufData.toConnectData(value);

    // If plugin has been configured to produce a flat schema and we have a Struct at the top level
    // (expected for protobufs), we will create a new SchemaAndValue that brings all fields nested
    // within Structs to the top level.
    if (flatConnectSchema && connectData.schema().type() == Schema.Type.STRUCT) {
      connectData = SchemaUtils.flattenNestedStructs(connectData, flatConnectSchemaFieldDelimiter);
    }

    return connectData;
  }
}
