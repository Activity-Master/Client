package com.guicedee.activitymaster.fsdm.client.services.deserializers;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import lombok.extern.log4j.Log4j2;
import java.io.IOException;
import java.util.UUID;
import io.smallrye.mutiny.Uni;
import com.guicedee.client.IGuiceContext;
import org.hibernate.reactive.mutiny.Mutiny;

@Log4j2
public class InvolvedPartyDeserializer extends JsonDeserializer<IInvolvedParty<?, ?>>
{
    @Override
    public IInvolvedParty<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
    {
        String value = p.getValueAsString();

        if (value == null || value.isBlank())
        {
            log.debug("InvolvedPartyDeserializer: Received blank or null input");
            return null;
        }

        if (!value.startsWith("{"))
        {
            log.debug("InvolvedPartyDeserializer: Deserializing UUID reference: {}", value);

            Mutiny.SessionFactory factory = IGuiceContext.get(Mutiny.SessionFactory.class);
            UUID uuid = UUID.fromString(value);

            try
            {
                return factory.withSession(session ->
                        session.withTransaction(tx -> {
                            log.debug("InvolvedPartyDeserializer: Opened transactional session for UUID {}", uuid);
                            IInvolvedPartyService<?> service = IGuiceContext.get(IInvolvedPartyService.class);
                            return service.find(session, uuid)
                                    .invoke(entity -> log.debug("InvolvedPartyDeserializer: Found entity for UUID {} - {}", uuid, entity));
                        })
                ).await().atMost(java.time.Duration.ofSeconds(50));
            }
            catch (Exception e)
            {
                log.error("InvolvedPartyDeserializer: Error retrieving IInvolvedParty for UUID {}", uuid, e);
                throw new IOException("Failed to fetch IInvolvedParty from DB", e);
            }
        }
        else
        {
            log.debug("InvolvedPartyDeserializer: Deserializing embedded object");
            try
            {
                return IGuiceContext.get(ObjectMapper.class)
                        .readerFor(IInvolvedParty.class)
                        .readValue(value);
            }
            catch (Exception e)
            {
                log.error("InvolvedPartyDeserializer: Failed to deserialize embedded JSON object", e);
                throw new IOException("Failed to deserialize embedded IInvolvedParty JSON", e);
            }
        }
    }
}
