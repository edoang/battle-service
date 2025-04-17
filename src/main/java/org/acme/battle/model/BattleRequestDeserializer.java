package org.acme.battle.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class BattleRequestDeserializer implements Deserializer<BattleRequest> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public BattleRequest deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, BattleRequest.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() {
    }
}
