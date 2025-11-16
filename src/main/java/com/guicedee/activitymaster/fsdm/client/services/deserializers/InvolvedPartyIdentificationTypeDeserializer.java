package com.guicedee.activitymaster.fsdm.client.services.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedPartyIdentificationType;
import com.guicedee.client.IGuiceContext;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.IOException;
import java.util.UUID;

@Log4j2
public class InvolvedPartyIdentificationTypeDeserializer extends JsonDeserializer<IInvolvedPartyIdentificationType<?, ?>>
{
    @Override
    public IInvolvedPartyIdentificationType<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
    {
        String value = p.getValueAsString();

        if (value == null || value.isBlank())
        {
            log.debug("InvolvedPartyIdentificationTypeDeserializer: Received blank or null input");
            return null;
        }

        if (!value.startsWith("{"))
        {
            log.debug("InvolvedPartyIdentificationTypeDeserializer: Resolving UUID reference: {}", value);

            Mutiny.SessionFactory factory = IGuiceContext.get(Mutiny.SessionFactory.class);
            UUID uuid = UUID.fromString(value);

            try
            {
                return factory.withSession(session ->
                        session.withTransaction(tx -> {
                            log.debug("InvolvedPartyIdentificationTypeDeserializer: Opened transaction for UUID {}", uuid);
                            IInvolvedPartyService<?> service = IGuiceContext.get(IInvolvedPartyService.class);
                            return service.findIdentificationType(session, uuid)
                                    .invoke(entity -> log.debug("InvolvedPartyIdentificationTypeDeserializer: Found entity for UUID {} - {}", uuid, entity));
                        })
                ).await().atMost(java.time.Duration.ofSeconds(50));
            }
            catch (Exception e)
            {
                log.error("InvolvedPartyIdentificationTypeDeserializer: Failed to resolve UUID {}", uuid, e);
                throw new IOException("Failed to fetch IInvolvedPartyIdentificationType from DB", e);
            }
        }
        else
        {
            log.debug("InvolvedPartyIdentificationTypeDeserializer: Deserializing embedded object");

            try
            {
                return IGuiceContext.get(ObjectMapper.class)
                        .readerFor(IInvolvedPartyIdentificationType.class)
                        .readValue(value);
            }
            catch (Exception e)
            {
                log.error("InvolvedPartyIdentificationTypeDeserializer: Failed to deserialize embedded object", e);
                throw new IOException("Failed to deserialize embedded IInvolvedPartyIdentificationType JSON", e);
            }
        }
    }
}

