// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kafka_message.proto

package com.blueapron.connect.protobuf;

/**
 * <pre>
 * This proto schema describes the simplified messages sent over Kafka
 * </pre>
 *
 * Protobuf type {@code blueapron.connect.protobuf.KafkaMessage}
 */
public final class KafkaMessage extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:blueapron.connect.protobuf.KafkaMessage)
    KafkaMessageOrBuilder {
private static final long serialVersionUID = 0L;
  // Use KafkaMessage.newBuilder() to construct.
  private KafkaMessage(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private KafkaMessage() {
    uuid_ = "";
    message_ = com.google.protobuf.ByteString.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new KafkaMessage();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private KafkaMessage(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();

            uuid_ = s;
            break;
          }
          case 16: {

            enqueueTime_ = input.readInt64();
            break;
          }
          case 26: {

            message_ = input.readBytes();
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.blueapron.connect.protobuf.KafkaMessageOuterClass.internal_static_blueapron_connect_protobuf_KafkaMessage_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.blueapron.connect.protobuf.KafkaMessageOuterClass.internal_static_blueapron_connect_protobuf_KafkaMessage_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.blueapron.connect.protobuf.KafkaMessage.class, com.blueapron.connect.protobuf.KafkaMessage.Builder.class);
  }

  public static final int UUID_FIELD_NUMBER = 1;
  private volatile java.lang.Object uuid_;
  /**
   * <pre>
   * unique id for this message
   * </pre>
   *
   * <code>string uuid = 1;</code>
   * @return The uuid.
   */
  @java.lang.Override
  public java.lang.String getUuid() {
    java.lang.Object ref = uuid_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      uuid_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * unique id for this message
   * </pre>
   *
   * <code>string uuid = 1;</code>
   * @return The bytes for uuid.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getUuidBytes() {
    java.lang.Object ref = uuid_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      uuid_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ENQUEUE_TIME_FIELD_NUMBER = 2;
  private long enqueueTime_;
  /**
   * <pre>
   * time this message was enqueued
   * </pre>
   *
   * <code>int64 enqueue_time = 2;</code>
   * @return The enqueueTime.
   */
  @java.lang.Override
  public long getEnqueueTime() {
    return enqueueTime_;
  }

  public static final int MESSAGE_FIELD_NUMBER = 3;
  private com.google.protobuf.ByteString message_;
  /**
   * <pre>
   * actual payload
   * </pre>
   *
   * <code>bytes message = 3;</code>
   * @return The message.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getMessage() {
    return message_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!getUuidBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, uuid_);
    }
    if (enqueueTime_ != 0L) {
      output.writeInt64(2, enqueueTime_);
    }
    if (!message_.isEmpty()) {
      output.writeBytes(3, message_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getUuidBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, uuid_);
    }
    if (enqueueTime_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(2, enqueueTime_);
    }
    if (!message_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(3, message_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof com.blueapron.connect.protobuf.KafkaMessage)) {
      return super.equals(obj);
    }
    com.blueapron.connect.protobuf.KafkaMessage other = (com.blueapron.connect.protobuf.KafkaMessage) obj;

    if (!getUuid()
        .equals(other.getUuid())) return false;
    if (getEnqueueTime()
        != other.getEnqueueTime()) return false;
    if (!getMessage()
        .equals(other.getMessage())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + UUID_FIELD_NUMBER;
    hash = (53 * hash) + getUuid().hashCode();
    hash = (37 * hash) + ENQUEUE_TIME_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getEnqueueTime());
    hash = (37 * hash) + MESSAGE_FIELD_NUMBER;
    hash = (53 * hash) + getMessage().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.blueapron.connect.protobuf.KafkaMessage parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.blueapron.connect.protobuf.KafkaMessage parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.blueapron.connect.protobuf.KafkaMessage parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.blueapron.connect.protobuf.KafkaMessage parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.blueapron.connect.protobuf.KafkaMessage parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.blueapron.connect.protobuf.KafkaMessage parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.blueapron.connect.protobuf.KafkaMessage parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.blueapron.connect.protobuf.KafkaMessage parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.blueapron.connect.protobuf.KafkaMessage parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.blueapron.connect.protobuf.KafkaMessage parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.blueapron.connect.protobuf.KafkaMessage parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.blueapron.connect.protobuf.KafkaMessage parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.blueapron.connect.protobuf.KafkaMessage prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * This proto schema describes the simplified messages sent over Kafka
   * </pre>
   *
   * Protobuf type {@code blueapron.connect.protobuf.KafkaMessage}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:blueapron.connect.protobuf.KafkaMessage)
      com.blueapron.connect.protobuf.KafkaMessageOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.blueapron.connect.protobuf.KafkaMessageOuterClass.internal_static_blueapron_connect_protobuf_KafkaMessage_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.blueapron.connect.protobuf.KafkaMessageOuterClass.internal_static_blueapron_connect_protobuf_KafkaMessage_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.blueapron.connect.protobuf.KafkaMessage.class, com.blueapron.connect.protobuf.KafkaMessage.Builder.class);
    }

    // Construct using com.blueapron.connect.protobuf.KafkaMessage.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      uuid_ = "";

      enqueueTime_ = 0L;

      message_ = com.google.protobuf.ByteString.EMPTY;

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.blueapron.connect.protobuf.KafkaMessageOuterClass.internal_static_blueapron_connect_protobuf_KafkaMessage_descriptor;
    }

    @java.lang.Override
    public com.blueapron.connect.protobuf.KafkaMessage getDefaultInstanceForType() {
      return com.blueapron.connect.protobuf.KafkaMessage.getDefaultInstance();
    }

    @java.lang.Override
    public com.blueapron.connect.protobuf.KafkaMessage build() {
      com.blueapron.connect.protobuf.KafkaMessage result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.blueapron.connect.protobuf.KafkaMessage buildPartial() {
      com.blueapron.connect.protobuf.KafkaMessage result = new com.blueapron.connect.protobuf.KafkaMessage(this);
      result.uuid_ = uuid_;
      result.enqueueTime_ = enqueueTime_;
      result.message_ = message_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.blueapron.connect.protobuf.KafkaMessage) {
        return mergeFrom((com.blueapron.connect.protobuf.KafkaMessage)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.blueapron.connect.protobuf.KafkaMessage other) {
      if (other == com.blueapron.connect.protobuf.KafkaMessage.getDefaultInstance()) return this;
      if (!other.getUuid().isEmpty()) {
        uuid_ = other.uuid_;
        onChanged();
      }
      if (other.getEnqueueTime() != 0L) {
        setEnqueueTime(other.getEnqueueTime());
      }
      if (other.getMessage() != com.google.protobuf.ByteString.EMPTY) {
        setMessage(other.getMessage());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      com.blueapron.connect.protobuf.KafkaMessage parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.blueapron.connect.protobuf.KafkaMessage) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object uuid_ = "";
    /**
     * <pre>
     * unique id for this message
     * </pre>
     *
     * <code>string uuid = 1;</code>
     * @return The uuid.
     */
    public java.lang.String getUuid() {
      java.lang.Object ref = uuid_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        uuid_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * unique id for this message
     * </pre>
     *
     * <code>string uuid = 1;</code>
     * @return The bytes for uuid.
     */
    public com.google.protobuf.ByteString
        getUuidBytes() {
      java.lang.Object ref = uuid_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        uuid_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * unique id for this message
     * </pre>
     *
     * <code>string uuid = 1;</code>
     * @param value The uuid to set.
     * @return This builder for chaining.
     */
    public Builder setUuid(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      uuid_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * unique id for this message
     * </pre>
     *
     * <code>string uuid = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearUuid() {
      
      uuid_ = getDefaultInstance().getUuid();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * unique id for this message
     * </pre>
     *
     * <code>string uuid = 1;</code>
     * @param value The bytes for uuid to set.
     * @return This builder for chaining.
     */
    public Builder setUuidBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      uuid_ = value;
      onChanged();
      return this;
    }

    private long enqueueTime_ ;
    /**
     * <pre>
     * time this message was enqueued
     * </pre>
     *
     * <code>int64 enqueue_time = 2;</code>
     * @return The enqueueTime.
     */
    @java.lang.Override
    public long getEnqueueTime() {
      return enqueueTime_;
    }
    /**
     * <pre>
     * time this message was enqueued
     * </pre>
     *
     * <code>int64 enqueue_time = 2;</code>
     * @param value The enqueueTime to set.
     * @return This builder for chaining.
     */
    public Builder setEnqueueTime(long value) {
      
      enqueueTime_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * time this message was enqueued
     * </pre>
     *
     * <code>int64 enqueue_time = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearEnqueueTime() {
      
      enqueueTime_ = 0L;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString message_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <pre>
     * actual payload
     * </pre>
     *
     * <code>bytes message = 3;</code>
     * @return The message.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getMessage() {
      return message_;
    }
    /**
     * <pre>
     * actual payload
     * </pre>
     *
     * <code>bytes message = 3;</code>
     * @param value The message to set.
     * @return This builder for chaining.
     */
    public Builder setMessage(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      message_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * actual payload
     * </pre>
     *
     * <code>bytes message = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearMessage() {
      
      message_ = getDefaultInstance().getMessage();
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:blueapron.connect.protobuf.KafkaMessage)
  }

  // @@protoc_insertion_point(class_scope:blueapron.connect.protobuf.KafkaMessage)
  private static final com.blueapron.connect.protobuf.KafkaMessage DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.blueapron.connect.protobuf.KafkaMessage();
  }

  public static com.blueapron.connect.protobuf.KafkaMessage getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<KafkaMessage>
      PARSER = new com.google.protobuf.AbstractParser<KafkaMessage>() {
    @java.lang.Override
    public KafkaMessage parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new KafkaMessage(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<KafkaMessage> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<KafkaMessage> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.blueapron.connect.protobuf.KafkaMessage getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

