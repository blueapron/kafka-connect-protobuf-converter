package com.blueapron.connect.protobuf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;

public class SchemaUtils {
  static SchemaAndValue flattenNestedStructs(SchemaAndValue connectData, String fieldDelimiter) {
    SchemaBuilder flatSchemaBuilder = SchemaBuilder.struct();
    Map<String, Object> flatValuesMap = new HashMap<>();
    String fieldNamePrefix = "";
    Schema dataSchema = connectData.schema();
    Struct dataValue = (Struct) connectData.value();
    populateFlatSchema(flatSchemaBuilder, flatValuesMap, fieldDelimiter, fieldNamePrefix, dataSchema, dataValue);

    Schema flatSchema = flatSchemaBuilder.build();
    Struct flatStruct = new Struct(flatSchema);
    for (Entry<String, Object> entry : flatValuesMap.entrySet()) {
      flatStruct.put(entry.getKey(), entry.getValue());
    }

    return new SchemaAndValue(flatSchema, flatStruct);
  }

  private static void populateFlatSchema(SchemaBuilder flatSchemaBuilder, Map<String, Object> flatValuesMap, String fieldDelimiter, String fieldNamePrefix, Schema schema, Struct value) {
    List<Field> schemaFields = schema.fields();
    for (Field field : schemaFields) {
      Schema fieldSchema = field.schema();
      String fieldName = field.name();
      Object fieldValue = value.get(fieldName);

      if (fieldSchema.type() == Schema.Type.STRUCT) {
        String newFieldNamePrefix = fieldNamePrefix + fieldName + fieldDelimiter;
        populateFlatSchema(flatSchemaBuilder, flatValuesMap, fieldDelimiter, newFieldNamePrefix, fieldSchema, (Struct) fieldValue);
      } else {
        String fullFieldName = fieldNamePrefix + fieldName;
        flatSchemaBuilder.field(fullFieldName, fieldSchema);
        flatValuesMap.put(fullFieldName, fieldValue);
      }
    }
  }
}
