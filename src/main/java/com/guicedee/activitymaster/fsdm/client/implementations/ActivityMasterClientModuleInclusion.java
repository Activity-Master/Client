package com.guicedee.activitymaster.fsdm.client.implementations;

import com.guicedee.client.services.config.IGuiceScanModuleInclusions;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Includes the module in the scanner
 */
public class ActivityMasterClientModuleInclusion implements IGuiceScanModuleInclusions<ActivityMasterClientModuleInclusion> {
    @Override
    public @NotNull Set<String> includeModules() {
        Set<String> set = new HashSet<>();
        set.add("com.guicedee.activitymaster.fsdm.client");
        return set;
    }
}
