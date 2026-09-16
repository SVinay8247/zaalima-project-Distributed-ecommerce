package com.zaalima.notificationservice.kafka;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Deserializer;

public class AvroDeserializer<T extends SpecificRecord> implements Deserializer<T> {

    private final Class<T> targetType;

    public AvroDeserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public T deserialize(String topic, byte[] data) {

        if (data == null) {
            return null;
        }

        try {
            T record = targetType.getDeclaredConstructor().newInstance();

            SpecificDatumReader<T> reader =
                    new SpecificDatumReader<>(record.getSchema());

            BinaryDecoder decoder =
                    DecoderFactory.get().binaryDecoder(data, null);

            return reader.read(null, decoder);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to deserialize Avro event", e);
        }
    }
}
