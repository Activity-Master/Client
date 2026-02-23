package com.guicedee.activitymaster.fsdm.client.services.deserializers;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.client.IGuiceContext;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.IOException;
import java.util.UUID;

@Log4j2
public class EnterpriseDeserializer extends JsonDeserializer<IEnterprise<?, ?>>
{
    @Override
    public IEnterprise<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
    {
        String value = p.getValueAsString();
        
        log.debug("🚀 Starting Enterprise deserialization process");

        if (value == null || value.isBlank())
        {
            log.debug("📋 Received blank or null input - returning null Enterprise");
            return null;
        }

        if (!value.startsWith("{"))
        {
            log.info("🔍 Deserializing Enterprise UUID reference: '{}'", value);

            Mutiny.SessionFactory factory = IGuiceContext.get(Mutiny.SessionFactory.class);
            UUID uuid;
            
            try
            {
                uuid = UUID.fromString(value);
                log.debug("✅ Successfully parsed UUID: {}", uuid);
            }
            catch (IllegalArgumentException e)
            {
                log.error("❌ Invalid UUID format for Enterprise deserialization: '{}'", value, e);
                throw new IOException("Invalid UUID format: " + value, e);
            }

            try
            {
                log.debug("💾 Starting database session for Enterprise UUID lookup: {}", uuid);
                
                return factory.openSession()
                        .chain(session -> session.withTransaction(tx -> {
                            log.debug("🏛️ Opened transactional session for Enterprise UUID: {} (Session: {})", uuid, session.hashCode());
                            
                            IEnterpriseService<?> service = IGuiceContext.get(IEnterpriseService.class);
                            
                            return service.getEnterprise(session, uuid)
                                    .onItem().invoke(entity -> {
                                        if (entity != null) {
                                            log.info("✅ Successfully retrieved Enterprise for UUID {}: '{}'", uuid, entity.getName());
                                            log.debug("📤 Returning Enterprise entity with session: {}", session.hashCode());
                                        } else {
                                            log.warn("⚠️ No Enterprise found for UUID: {}", uuid);
                                        }
                                    })
                                    .onFailure().invoke(error -> 
                                        log.error("❌ Failed to retrieve Enterprise for UUID {} with session {}: {}", 
                                            uuid, session.hashCode(), error.getMessage(), error));
                        }).eventually(session::close))
                .await().atMost(java.time.Duration.ofSeconds(50));
            }
            catch (Exception e)
            {
                log.error("💥 Critical error retrieving Enterprise for UUID {}: {}", uuid, e.getMessage(), e);
                throw new IOException("Failed to fetch Enterprise from database for UUID: " + uuid, e);
            }
        }
        else
        {
            log.info("📋 Deserializing embedded Enterprise JSON object");
            log.debug("🔍 JSON content preview: {}", value.length() > 100 ? value.substring(0, 100) + "..." : value);
            
            try
            {
                IEnterprise<?, ?> result = IGuiceContext.get(ObjectMapper.class)
                        .readerFor(IEnterprise.class)
                        .readValue(value);
                        
                log.info("✅ Successfully deserialized embedded Enterprise JSON: '{}'", 
                    result != null ? result.getName() : "null");
                log.debug("📤 Returning deserialized Enterprise object");
                
                return result;
            }
            catch (Exception e)
            {
                log.error("💥 Failed to deserialize embedded Enterprise JSON: {}", e.getMessage(), e);
                log.debug("🔍 Failed JSON content: {}", value);
                throw new IOException("Failed to deserialize embedded Enterprise JSON", e);
            }
        }
    }
}
