package com.guicedee.activitymaster.fsdm.client.services.administration;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security.ISecurityToken;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.scopes.CallScopeProperties;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;

/**
 * The overall application configuration, root singleton
 */
@Log4j2
@Singleton
public class ActivityMasterConfiguration
{
	private static final ActivityMasterConfiguration configuration = new ActivityMasterConfiguration();

	public static String applicationEnterpriseName;

	private Set<IActivityMasterSystem<?>> allSystems;
	private final List<IActivityMasterProgressMonitor> progressMonitors = new CopyOnWriteArrayList<>();

	private boolean enterpriseReady;

	ActivityMasterConfiguration()
	{
		// no config required
	}

	public boolean isSecurityEnabled()
	{
		CallScopeProperties csp = IGuiceContext.get(CallScopeProperties.class);
		if (csp != null)
		{
			Boolean s = (Boolean) csp.getProperties().get("fsdm.securities");
			if (s != null)
			{
				return s;
			}
		}
		return true;
	}

	public void setSecurityEnabled(boolean securityEnabled)
	{
		CallScopeProperties csp = IGuiceContext.get(CallScopeProperties.class);
		if (csp != null)
		{
			csp.getProperties().put("fsdm.securities", securityEnabled);
		}
	}

	public String getApplicationEnterpriseName()
	{
		return applicationEnterpriseName;
	}

	@SuppressWarnings("UnusedReturnValue")
	public ActivityMasterConfiguration setApplicationEnterpriseName(String applicationEnterpriseName)
	{
		ActivityMasterConfiguration.applicationEnterpriseName = applicationEnterpriseName;
		return this;
	}

	public static ActivityMasterConfiguration get()
	{
		return configuration;
	}


	public void configureThread(ActivityMasterConfigurationDTO dto)
	{
		setApplicationEnterpriseName(dto.getEnterpriseName());
		setSecurityEnabled(dto.getSecurities());
	}

	public Set<IActivityMasterSystem<?>> getAllSystems()
	{
		if (allSystems == null)
		{
			allSystems = IActivityMasterSystem.allSystems();
			allSystems = new TreeSet<>(allSystems);
		}
		return allSystems;
	}

	/**
	 * Reactive version of isEnterpriseReady
	 * @return Uni<Boolean> indicating whether the enterprise is ready
	 */
	public Uni<IEnterprise<?,?>> isEnterpriseReady(Mutiny.Session session)
	{
		if (!Strings.isNullOrEmpty(applicationEnterpriseName))
		{
			IEnterpriseService<?> enterpriseService = com.guicedee.client.IGuiceContext.get(IEnterpriseService.class);
			return enterpriseService.isEnterpriseReady(session);
		}
		return Uni.createFrom().failure(new Exception("No application enterprise name set"));
	}

	public List<IActivityMasterProgressMonitor> getProgressMonitors()
	{
		if (progressMonitors == null || progressMonitors.isEmpty())
		{
			Set<IActivityMasterProgressMonitor> monitors = IGuiceContext.loaderToSet(ServiceLoader.load(IActivityMasterProgressMonitor.class));
			for (IActivityMasterProgressMonitor monitor : monitors)
			{
				progressMonitors.add(monitor);
			}
		}
		return progressMonitors;
	}

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
	public static class ActivityMasterConfigurationDTO
	{
		private String enterpriseName;
		private ISecurityToken<?, ?> token;
		private Boolean securities;

		public ActivityMasterConfigurationDTO()
		{
		}

		public ActivityMasterConfigurationDTO fromCurrentThread()
		{
			enterpriseName = ActivityMasterConfiguration.get()
			                                            .getApplicationEnterpriseName();
			securities = ActivityMasterConfiguration.get()
			                                        .isSecurityEnabled();
			return this;
		}

		public String getEnterpriseName()
		{
			return enterpriseName;
		}

		public ActivityMasterConfigurationDTO setEnterpriseName(String enterpriseName)
		{
			this.enterpriseName = enterpriseName;
			return this;
		}

		public ISecurityToken<?, ?> getToken()
		{
			return token;
		}

		public ActivityMasterConfigurationDTO setToken(ISecurityToken<?, ?> token)
		{
			this.token = token;
			return this;
		}

		public Boolean getSecurities()
		{
			return securities;
		}

		public ActivityMasterConfigurationDTO setSecurities(Boolean securities)
		{
			this.securities = securities;
			return this;
		}
	}
}
