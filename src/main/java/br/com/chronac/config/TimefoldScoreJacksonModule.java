package br.com.chronac.config;

import java.io.IOException;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.HardSoftScore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimefoldScoreJacksonModule {

    @Bean
    public Module timefoldScoreModule() {
        SimpleModule module = new SimpleModule("TimefoldScoreModule");
        module.addSerializer(HardSoftScore.class, new HardSoftScoreSerializer());
        module.addDeserializer(HardSoftScore.class, new HardSoftScoreDeserializer());
        module.addSerializer(HardMediumSoftScore.class, new HardMediumSoftScoreSerializer());
        module.addDeserializer(HardMediumSoftScore.class, new HardMediumSoftScoreDeserializer());
        return module;
    }

    static class HardMediumSoftScoreSerializer extends JsonSerializer<HardMediumSoftScore> {
        @Override
        public void serialize(HardMediumSoftScore score, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(score.toString());
        }
    }

    static class HardMediumSoftScoreDeserializer extends JsonDeserializer<HardMediumSoftScore> {
        @Override
        public HardMediumSoftScore deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getValueAsString();
            if (text == null || text.isEmpty()) {
                return null;
            }
            return HardMediumSoftScore.parseScore(text);
        }
    }

    static class HardSoftScoreSerializer extends JsonSerializer<HardSoftScore> {
        @Override
        public void serialize(HardSoftScore score, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(score.toString());
        }
    }

    static class HardSoftScoreDeserializer extends JsonDeserializer<HardSoftScore> {
        @Override
        public HardSoftScore deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getValueAsString();
            if (text == null || text.isEmpty()) {
                return null;
            }
            return HardSoftScore.parseScore(text);
        }
    }
}
