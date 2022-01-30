package com.blueapron.connect.protobuf;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.storage.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import com.google.protobuf.GeneratedMessageV3;


/**
 * Implementation of Converter that uses Protobufs.
 */
public class ProtobufConverter implements Converter {
  private static final Logger log = LoggerFactory.getLogger(ProtobufConverter.class);

  private static final String CONFIG_PROTO_CLASS_NAME = "protoClassName";
  private static final String CONFIG_LEGACY_FIELD_NAME = "legacyName";
  private static final String CONFIG_PROTO_MAP_CONVERSION_TYPE = "protoMapConversionType";
  private static final String CONFIG_MAP_FIELDS_BY = "mapFieldsBy";

  private enum MapBy {
    INDEX, NAME
  }

  private ProtobufData protobufData;
  private boolean mapByName;

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    String legacyName = (String) configs.get(CONFIG_LEGACY_FIELD_NAME);
    legacyName = legacyName == null ? "legacy_name" : legacyName; //Because you cannot do getOrDefault on a Map<String, ?>

    //The possible values for this config-property are not documented
    boolean useConnectSchemaMap = "map".equals(configs.get(CONFIG_PROTO_MAP_CONVERSION_TYPE));

    String protoClassName = (String) configs.get(CONFIG_PROTO_CLASS_NAME);
    if (protoClassName == null) {
      if (isKey) {
        return;
      } else {
        throw new ConnectException("Value converter must have a " + CONFIG_PROTO_CLASS_NAME + " configured");
      }
    }

    if (configs.containsKey(CONFIG_MAP_FIELDS_BY)) {
      MapBy mapBy = MapBy.valueOf((String) configs.get(CONFIG_MAP_FIELDS_BY));
      mapByName = mapBy == MapBy.NAME;
    } else {
      mapByName = false;
    }

    try {
      log.info("Initializing ProtobufData with args: [protoClassName={}, legacyName={}, useConnectSchemaMap={}]",
        protoClassName, legacyName, useConnectSchemaMap);
      protobufData = new ProtobufData(Class.forName(protoClassName).asSubclass(GeneratedMessageV3.class), legacyName, useConnectSchemaMap);
    } catch (ClassNotFoundException e) {
      throw new ConnectException("Proto class " + protoClassName + " not found in the classpath");
    } catch (ClassCastException e) {
      throw new ConnectException("Proto class " + protoClassName + " is not a valid proto3 message class");
    }
  }

  @Override
  public byte[] fromConnectData(String topic, Schema schema, Object value) {
    if (protobufData == null || schema == null || value == null) {
      return null;
    }

    if (mapByName) {
      return protobufData.fromConnectDataByName(value);
    } else {
      return protobufData.fromConnectData(value);
    }
  }

  @Override
  public SchemaAndValue toConnectData(String topic, byte[] value) {
    if (protobufData == null || value == null) {
      return SchemaAndValue.NULL;
    }

    return protobufData.toConnectData(value);
  }
}
