package com.guicedee.activitymaster.fsdm.client.services.rest;

import com.google.inject.name.Named;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.fsdm.client.services.rest.arrangements.ArrangementCreateDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.arrangements.ArrangementDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.arrangements.ArrangementFindDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.arrangements.ArrangementUpdateDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.events.EventCreateDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.events.EventDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.events.EventFindDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.events.EventUpdateDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.parties.PartyCreateDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.parties.PartyDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.parties.PartyFindDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.parties.PartySearchByClassificationDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.parties.PartySearchByIdentificationDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.parties.PartyUpdateDTO;
import com.guicedee.activitymaster.fsdm.client.services.rest.resourceitems.*;
import com.guicedee.rest.client.RestClient;
import com.guicedee.rest.client.annotations.Endpoint;
import io.smallrye.mutiny.Uni;

@SuppressWarnings("BindingAnnotationWithoutInject")
public class RestClients {

    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/arrangement/{systemName}/create",method = "POST")
    @Named("ArrangementCreateService")
    private RestClient<ArrangementCreateDTO, ArrangementDTO> arrangementCreate;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/arrangement/{systemName}/update",method = "PUT")
    @Named("ArrangementUpdateService")
    private RestClient<ArrangementUpdateDTO, ArrangementDTO> arrangementUpdate;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/arrangement/{systemName}/find",method = "POST")
    @Named("ArrangementFindService")
    private RestClient<ArrangementFindDTO, ArrangementDTO> arrangementFind;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/resource-item/{systemName}/create",method = "POST")
    @Named("ResourceItemCreateService")
    private RestClient<ResourceItemCreateDTO, ResourceItemDTO> resourceItemCreate;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/resource-item/{systemName}/update",method = "PUT")
    @Named("ResourceItemUpdateService")
    private RestClient<ResourceItemUpdateDTO, ResourceItemDTO> resourceItemUpdate;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/resource-item/{systemName}/find",method = "POST")
    @Named("ResourceItemFindService")
    private RestClient<ResourceItemFindDTO, ResourceItemDTO> resourceItemFind;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/resource-item/{systemName}/data",method = "PATCH")
    @Named("ResourceItemUpdateDataService")
    private RestClient<ResourceItemUpdateDataDTO, ResourceItemDTO> resourceItemDataUpdate;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/event/{systemName}/create",method = "POST")
    @Named("EventCreateService")
    private RestClient<EventCreateDTO, EventDTO> eventCreate;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/event/{systemName}/update",method = "PUT")
    @Named("EventUpdateService")
    private RestClient<EventUpdateDTO, EventDTO> eventUpdate;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/event/{systemName}/find",method = "POST")
    @Named("EventFindService")
    private RestClient<EventFindDTO, EventDTO> eventFind;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/party/{systemName}/create",method = "POST")
    @Named("PartyCreateService")
    private RestClient<PartyCreateDTO, PartyDTO> partyCreate;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/party/{systemName}/update",method = "PUT")
    @Named("PartyUpdateService")
    private RestClient<PartyUpdateDTO, PartyDTO> partyUpdate;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/party/{systemName}/find",method = "POST")
    @Named("PartyFindService")
    private RestClient<PartyFindDTO, PartyDTO> partyFind;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/party/{systemName}/search/classification",method = "POST")
    @Named("PartySearchByClassificationService")
    private RestClient<PartySearchByClassificationDTO, java.util.List<PartyDTO>> partySearchByClassification;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/party/{systemName}/search/identification",method = "POST")
    @Named("PartySearchByIdentificationService")
    private RestClient<PartySearchByIdentificationDTO, java.util.List<PartyDTO>> partySearchByIdentification;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/resource-item/{systemName}/search",method = "POST")
    @Named("ResourceItemSearchService")
    private RestClient<ResourceItemSearchDTO, java.util.List<ResourceItemDTO>> resourceItemSearch;


    @Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/resource-item/{systemName}/data/{resourceItemId}",method = "GET")
    @Named("ResourceItemGetDataService")
    private RestClient<Void, byte[]> resourceItemGetData;

    public Uni<ArrangementDTO> createArrangement(String systemName, ArrangementCreateDTO arrangementCreateDTO) {
        return arrangementCreate.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(arrangementCreateDTO);
    }

    public Uni<ArrangementDTO> updateArrangement(String systemName, ArrangementUpdateDTO arrangementUpdateDto) {
        return arrangementUpdate.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(arrangementUpdateDto);
    }

    public Uni<ArrangementDTO> findArrangement(String systemName, ArrangementFindDTO arrangementFindDto) {
        return arrangementFind.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(arrangementFindDto);
    }

    public Uni<ResourceItemDTO> createResourceItem(String systemName, ResourceItemCreateDTO resourceItemCreateDTO) {
        return resourceItemCreate.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(resourceItemCreateDTO);
    }

    public Uni<ResourceItemDTO> updateResourceItem(String systemName, ResourceItemUpdateDTO resourceItemUpdateDTO) {
        return resourceItemUpdate.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(resourceItemUpdateDTO);
    }

    public Uni<ResourceItemDTO> findResourceItem(String systemName, ResourceItemFindDTO resourceItemFindDTO) {
        return resourceItemFind.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(resourceItemFindDTO);
    }

    public Uni<java.util.List<ResourceItemDTO>> searchResourceItems(String systemName, ResourceItemSearchDTO resourceItemSearchDTO) {
        return resourceItemSearch.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(resourceItemSearchDTO);
    }

    public Uni<ResourceItemDTO> updateResourceItemData(String systemName, ResourceItemUpdateDataDTO resourceItemUpdateDataDTO) {
        return resourceItemDataUpdate.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(resourceItemUpdateDataDTO);
    }

    public Uni<byte[]> getResourceItemData(String systemName, java.util.UUID resourceItemId) {
        return resourceItemGetData.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .pathParam("resourceItemId", resourceItemId.toString())
                .send();
    }

    public Uni<EventDTO> createEvent(String systemName, EventCreateDTO eventCreateDTO) {
        return eventCreate.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(eventCreateDTO);
    }

    public Uni<EventDTO> updateEvent(String systemName, EventUpdateDTO eventUpdateDTO) {
        return eventUpdate.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(eventUpdateDTO);
    }

    public Uni<EventDTO> findEvent(String systemName, EventFindDTO eventFindDTO) {
        return eventFind.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(eventFindDTO);
    }

    public Uni<PartyDTO> createParty(String systemName, PartyCreateDTO partyCreateDTO) {
        return partyCreate.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(partyCreateDTO);
    }

    public Uni<PartyDTO> updateParty(String systemName, PartyUpdateDTO partyUpdateDTO) {
        return partyUpdate.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(partyUpdateDTO);
    }

    public Uni<PartyDTO> findParty(String systemName, PartyFindDTO partyFindDTO) {
        return partyFind.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(partyFindDTO);
    }

    public Uni<java.util.List<PartyDTO>> searchPartiesByClassification(String systemName, PartySearchByClassificationDTO searchDTO) {
        return partySearchByClassification.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(searchDTO);
    }

    public Uni<java.util.List<PartyDTO>> searchPartiesByIdentification(String systemName, PartySearchByIdentificationDTO searchDTO) {
        return partySearchByIdentification.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
                .pathParam("systemName", systemName)
                .send(searchDTO);
    }


}