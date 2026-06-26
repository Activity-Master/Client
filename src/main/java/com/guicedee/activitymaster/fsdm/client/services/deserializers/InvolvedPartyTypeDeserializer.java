package com.guicedee.activitymaster.fsdm.client.services.deserializers;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ObjectMapper;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedPartyType;
import com.guicedee.client.IGuiceContext;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.IOException;
import java.util.UUID;

@Log4j2
public class InvolvedPartyTypeDeserializer extends ValueDeserializer<IInvolvedPartyType<?, ?>>
{
    @Override
    public IInvolvedPartyType<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException
    {
        String value = p.getValueAsString();

        if (value == null || value.isBlank())
        {
            log.debug("InvolvedPartyTypeDeserializer: Received blank or null input");
            return null;
        }

        if (!value.startsWith("{"))
        {
            log.debug("InvolvedPartyTypeDeserializer: Resolving UUID reference: {}", value);

            Mutiny.SessionFactory factory = IGuiceContext.get(Mutiny.SessionFactory.class);
            UUID uuid = UUID.fromString(value);

            try
            {
                return factory.openSession()
                        .chain(session -> session.withTransaction(tx -> {
                            log.debug("InvolvedPartyTypeDeserializer: Opened transaction for UUID {}", uuid);
                            IInvolvedPartyService<?> service = IGuiceContext.get(IInvolvedPartyService.class);
                            return service.findType(session, uuid)
                                    .invoke(entity -> log.debug("InvolvedPartyTypeDeserializer: Found entity for UUID {} - {}", uuid, entity));
                        }).eventually(session::close))
                .await().atMost(java.time.Duration.ofSeconds(50));
            }
            catch (Exception e)
            {
                log.error("InvolvedPartyTypeDeserializer: Failed to fetch entity for UUID {}", uuid, e);
                throw new RuntimeException("Failed to fetch IInvolvedPartyType from DB", e);
            }
        }
        else
        {
            log.debug("InvolvedPartyTypeDeserializer: Deserializing embedded JSON object");

            try
            {
                ObjectMapper mapper = IGuiceContext.get(ObjectMapper.class);
                return mapper.readerFor(IInvolvedPartyType.class).readValue(value);
            }
            catch (Exception e)
            {
                log.error("InvolvedPartyTypeDeserializer: Failed to deserialize embedded JSON", e);
                throw new RuntimeException("Failed to deserialize embedded IInvolvedPartyType JSON", e);
            }
        }
    }
}
