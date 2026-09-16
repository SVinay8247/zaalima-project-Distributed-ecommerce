package com.zaalima.orderservice.kafka;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;

public class AvroSerializer implements Serializer<SpecificRecord> {

    @Override
    public byte[] serialize(String topic, SpecificRecord data) {
        if (data == null) {
            return null;
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            BinaryEncoder encoder =
                    EncoderFactory.get().binaryEncoder(outputStream, null);

            SpecificDatumWriter<SpecificRecord> writer =
                    new SpecificDatumWriter<>(data.getSchema());

            writer.write(data, encoder);
            encoder.flush();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to serialize Avro event", e);
        }
    }
}
