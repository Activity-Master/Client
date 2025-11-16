package com.guicedee.activitymaster.fsdm.client.services.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedPartyNameType;
import com.guicedee.client.IGuiceContext;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.IOException;
import java.util.UUID;

@Log4j2
public class InvolvedPartyNameTypeDeserializer extends JsonDeserializer<IInvolvedPartyNameType<?, ?>>
{
    @Override
    public IInvolvedPartyNameType<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
    {
        String value = p.getValueAsString();

        if (value == null || value.isBlank())
        {
            log.debug("InvolvedPartyNameTypeDeserializer: Received blank or null input");
            return null;
        }

        if (!value.startsWith("{"))
        {
            log.debug("InvolvedPartyNameTypeDeserializer: Resolving UUID reference: {}", value);

            Mutiny.SessionFactory factory = IGuiceContext.get(Mutiny.SessionFactory.class);
            UUID uuid = UUID.fromString(value);

            try
            {
                return factory.withSession(session ->
                        session.withTransaction(tx -> {
                            log.debug("InvolvedPartyNameTypeDeserializer: Opened transaction for UUID {}", uuid);
                            IInvolvedPartyService<?> service = IGuiceContext.get(IInvolvedPartyService.class);
                            return service.findNameType(session, uuid)
                                    .invoke(entity -> log.debug("InvolvedPartyNameTypeDeserializer: Found entity for UUID {} - {}", uuid, entity));
                        })
                ).await().atMost(java.time.Duration.ofSeconds(50));
            }
            catch (Exception e)
            {
                log.error("InvolvedPartyNameTypeDeserializer: Failed to fetch entity for UUID {}", uuid, e);
                throw new IOException("Failed to fetch IInvolvedPartyNameType from DB", e);
            }
        }
        else
        {
            log.debug("InvolvedPartyNameTypeDeserializer: Deserializing embedded JSON object");

            try
            {
                ObjectMapper mapper = IGuiceContext.get(ObjectMapper.class);
                return mapper.readerFor(IInvolvedPartyNameType.class).readValue(value);
            }
            catch (Exception e)
            {
                log.error("InvolvedPartyNameTypeDeserializer: Failed to deserialize embedded JSON", e);
                throw new IOException("Failed to deserialize embedded IInvolvedPartyNameType JSON", e);
            }
        }
    }
}

