> :trident: This was forked from [Blue Apron](https://github.com/blueapron/kafka-connect-protobuf-converter) which 
> appears to no-longer be maintained.

# kafka-connect-protobuf-converter
Converter plugin for [Kafka Connect](https://docs.confluent.io/current/connect/). A converter
controls the format of the data that will be written to Kafka for source connectors or
read from Kafka for sink connectors.

## Compatibility

The 2.x release series is compatible with Kafka Connect 5.x. and up (older releases have been validated to work all the way back to Kafka Connect 3.2.0, although we highly recommend updating to the latest version).

*N.B. This converter only works with `proto3` protocol buffer schemas.*

## Usage

Copy the `kafka-connect-protobuf-converter` jar and the jar containing your compiled protocol buffers to
`/usr/share/java/kafka-serde-tools` on your Kafka Connect instance and restart Kafka Connect.

Converters can be specified on a per-connector basis.

To use the protobuf converter in Kafka Connect, specify the converter as your key and value converter and specify the
protocol buffer class you want to use to deserialize the message (ex: `com.google.protobuf.Int32Value`).

Note: Nested classes must be specified using the `$` notation, for example
`com.blueapron.connect.protobuf.NestedTestProtoOuterClass$NestedTestProto`

Example Kafka Connect JDBC source:
```
connector.class=io.confluent.connect.jdbc.JdbcSourceConnector
mode=bulk
topic.prefix=blueapron-protos-int
tasks.max=1
query=select * from int32value
name=blueapron-protos-source-int
connection.url=jdbc:postgresql://192.168.99.100:5432/test?user=postgres&password=mysecretpassword
value.converter=com.blueapron.connect.protobuf.ProtobufConverter
value.converter.protoClassName=com.google.protobuf.Int32Value
key.converter=com.blueapron.connect.protobuf.ProtobufConverter
```

Example Kafka Connect JDBC sink:
```
connector.class=io.confluent.connect.jdbc.JdbcSinkConnector
topics=blueapron-protos-int
topics.to.tables=blueapron-protos-int=int32output
auto.create=true
tasks.max=1
name=blueapron-protos-sink-int
connection.url=jdbc:postgresql://192.168.99.100:5432/test?user=postgres&password=mysecretpassword
value.converter=com.blueapron.connect.protobuf.ProtobufConverter
value.converter.protoClassName=com.google.protobuf.Int32Value
key.converter=com.blueapron.connect.protobuf.ProtobufConverter
```

## Supported Conversions

[List of proto3 types](https://developers.google.com/protocol-buffers/docs/proto3)

### From Connect

|Connect Schema Type|Protobuf Type|
| ----------------- | ----------- |
|INT32              |int32        |
|INT64              |int64        |
|FLOAT32            |float        |
|FLOAT64            |double       |
|BOOLEAN            |bool         |
|STRING             |string       |
|BYTES              |bytes        |


|Connect Logical Type|Protobuf Type|
| ------------------ | ----------- |
|Timestamp           |Timestamp    |
|Date                |Date         |

### To Connect

|Protobuf Type|Connect Schema Type|
| ----------- | ----------------- |
|int32        |INT32              |
|int64        |INT64              |
|sint32       |INT32              |
|sint64       |INT64              |
|uint32       |INT64              |
|float        |FLOAT32            |
|double       |FLOAT64            |
|bool         |BOOLEAN            |
|string       |STRING             |
|bytes        |BYTES              |
|repeated     |ARRAY              |
|map          |ARRAY of STRUCT*   |


|Protobuf Type|Connect Logical Type|
| ----------- | ------------------ |
|Timestamp    |Timestamp           |
|Date         |Date                |
|uint64       |Decimal             |

\* Protobuf map type fields are converted by default to an Array of Struct, 
but the converter supports also converting these fields into Connect Schema Map type.
To enable this conversion mode, set the configuration field `protoMapConversionType` to `map`:
 ```
value.converter.protoMapConversionType=map
```

## Handling field renames and deletes
Renaming and removing fields is supported by the proto IDL, but certain output formats (for example, BigQuery) do not
support renaming or removal. In order to support these output formats, we use a custom field option to specify the
original name and keep the Kafka Connect schema consistent.

You can specify the name for this field option using the `legacyName` configuration item. By default, the field option
used is `legacy_name`

Example: `value.converter.legacyName=blueapron.connect.protobuf.legacy_name`

This annotation provides a hint that allows renamed fields to be mapped to correctly to their output schema names.

```
// Before rename:
message TestMessage {
  string test_string = 1;
}

// After first rename:
import "path/to/LegacyName.proto";

message TestMessage {
  string renamed_string = 1 [(blueapron.connect.protobuf.legacy_name) = "test_string"];
}

// After subsequent rename:
import "path/to/LegacyName.proto";

message TestMessage {
  string yet_another_named_string = 1 [(blueapron.connect.protobuf.legacy_name) = "test_string"];
}

```

We can also use the same syntax to support removing fields. You can treat deleted fields as renamed fields, prefixing
the deprecated field name with `OBSOLETE_` and including a legacy_name field option as previously detailed.

```
// Before deprecation:
message TestMessage {
  string test_string = 1;
  int32 test_count = 2;
}

// After deprecation:
import "path/to/LegacyName.proto";

message TestMessage {
  string OBSOLETE_test_string = 1 [(blueapron.connect.protobuf.legacy_name) = "test_string"];
  int32 test_count = 2;
}
```

## Contributing

Contributions are warmly welcomed. If fixing a bug, please open an issue on the project before opening a PR. This helps with documentation for other users and will make code review go quicker 🙌

## Development

#### Run tests:
```
mvn test
```

#### Create the JAR:
```
mvn clean package
```

Copy the JAR with dependencies (`kafka-connect-protobuf-converter-*-jar-with-dependencies.jar`) to
`/usr/share/java/kafka-serde-tools` on your local Kafka Connect instance to make the
converter available in Kafka Connect.

#### Deploy to Maven Central:

*Note:* only internal Blue Apron employees can deploy to Maven Central
1. Update the version to include -SNAPSHOT
1. Prepare your local environment for the deploy. If you already have a custom `~/.m2/settings.xml`, you may want to back up or merge your existing file rather than overwrite it.
    ```
    cp settings.xml ~/.m2/settings.xml
    export GPG_PASSPHRASE=
    export OSSRH_JIRA_USERNAME=
    export OSSRH_JIRA_PASSWORD=
    ```
1. Confirm the tests run
    ```
    mvn test
    mvn release:clean
    ```
1. Prepare the release
    ```
    mvn release:prepare
    ```
1. Perform the release
    ```
    mvn release:perform
    ```

#### Generate protos:

    Run the below command from the repository root.
    ```
    protoc -I=src/test/java/com/blueapron/connect/protobuf/protos --java_out=src/test/java/ src/test/java/com/blueapron/connect/protobuf/protos/*.proto
    ```
