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
  private static final String PROTO_MAP_CONVERSION_TYPE = "protoMapConversionType";
  private ProtobufData protobufData;

  private boolean isInvalidConfiguration(Object proto, boolean isKey) {
    return proto == null && !isKey;
  }

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    Object legacyName = configs.get(LEGACY_NAME_CONFIG);
    String legacyNameString = legacyName == null ? "legacy_name" : legacyName.toString();
    boolean useConnectSchemaMap = "map".equals(configs.get(PROTO_MAP_CONVERSION_TYPE));

    Object protoClassName = configs.get(PROTO_CLASS_NAME_CONFIG);
    if (isInvalidConfiguration(protoClassName, isKey)) {
      throw new ConnectException("Value converter must have a " + PROTO_CLASS_NAME_CONFIG + " configured");
    }

    if (protoClassName == null) {
      protobufData = null;
      return;
    }

    String protoClassNameString = protoClassName.toString();
    try {
      log.info("Initializing ProtobufData with args: [protoClassName={}, legacyName={}, useConnectSchemaMap={}]", protoClassNameString, legacyNameString, useConnectSchemaMap);
      protobufData = new ProtobufData(Class.forName(protoClassNameString).asSubclass(com.google.protobuf.GeneratedMessageV3.class), legacyNameString, useConnectSchemaMap);
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

    return protobufData.toConnectData(value);
  }
}
