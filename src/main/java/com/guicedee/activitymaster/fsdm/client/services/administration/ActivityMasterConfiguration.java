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
import com.guicedee.client.scopes.CallScoper;
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

	/**
	 * Call-scope property key under which the row-level security read-enforcement flag is stored.
	 */
	public static final String SCOPE_KEY_SECURITY = "fsdm.securities";
	/**
	 * Call-scope property key under which the per-request enterprise id is stored.
	 */
	public static final String SCOPE_KEY_ENTERPRISE_ID = "fsdm.enterpriseId";
	/**
	 * Call-scope property key under which the current caller identity token is stored.
	 */
	public static final String SCOPE_KEY_IDENTITY_TOKEN = "fsdm.identityToken";

	public static String applicationEnterpriseName;
	private static String _activityMasterHost = "http://localhost:8080";

	public static String activityMasterHost = _activityMasterHost;

	static {
		// Seed the system property so ${ACTIVITY_MASTER_HOST} resolves in @Endpoint URLs
		if (System.getProperty("ACTIVITY_MASTER_HOST") == null) {
			System.setProperty("ACTIVITY_MASTER_HOST", _activityMasterHost);
		} else {
			activityMasterHost = System.getProperty("ACTIVITY_MASTER_HOST");
		}
	}

	/**
	 * Sets the Activity Master host and keeps the ACTIVITY_MASTER_HOST system property in sync.
	 */
	public static void setActivityMasterHost(String host) {
		activityMasterHost = host;
		System.setProperty("ACTIVITY_MASTER_HOST", host);
	}

	private Set<IActivityMasterSystem<?>> allSystems;
	private final List<IActivityMasterProgressMonitor> progressMonitors = new CopyOnWriteArrayList<>();

	private boolean enterpriseReady;

	ActivityMasterConfiguration()
	{
		// no config required
	}

	public boolean isSecurityEnabled()
	{
		CallScoper callScoper = IGuiceContext.get(CallScoper.class);
		if (callScoper.isStartedScope())
		{
			CallScopeProperties csp = IGuiceContext.get(CallScopeProperties.class);
			if (csp != null)
			{
				Boolean s = (Boolean) csp.getProperties().get(SCOPE_KEY_SECURITY);
				if (s != null)
				{
					return s;
				}
			}
		}
		return true;
	}

	public void setSecurityEnabled(boolean securityEnabled)
	{
		CallScoper callScoper = IGuiceContext.get(CallScoper.class);
		if (callScoper.isStartedScope())
		{
			CallScopeProperties csp = IGuiceContext.get(CallScopeProperties.class);
			if (csp != null)
			{
				csp.getProperties().put(SCOPE_KEY_SECURITY, securityEnabled);
			}
		}
	}

	/**
	 * Reads a value stored on the active call scope, or {@code null} when there is no started scope
	 * (e.g. a plain thread with no Vert.x context) or the key is absent.
	 *
	 * @param key the call-scope property key
	 * @param <T> the expected value type
	 * @return the scoped value, or {@code null}
	 */
	@SuppressWarnings("unchecked")
	private <T> T getScopedProperty(Object key)
	{
		CallScoper callScoper = IGuiceContext.get(CallScoper.class);
		if (callScoper.isStartedScope())
		{
			CallScopeProperties csp = IGuiceContext.get(CallScopeProperties.class);
			if (csp != null)
			{
				return (T) csp.getProperties().get(key);
			}
		}
		return null;
	}

	/**
	 * Stores (or, when {@code value} is {@code null}, removes) a value on the active call scope.
	 * No-op when there is no started scope.
	 *
	 * @param key   the call-scope property key
	 * @param value the value to store, or {@code null} to clear it
	 */
	private void setScopedProperty(Object key, Object value)
	{
		CallScoper callScoper = IGuiceContext.get(CallScoper.class);
		if (callScoper.isStartedScope())
		{
			CallScopeProperties csp = IGuiceContext.get(CallScopeProperties.class);
			if (csp != null)
			{
				if (value == null)
				{
					csp.getProperties().remove(key);
				}
				else
				{
					csp.getProperties().put(key, value);
				}
			}
		}
	}

	/**
	 * Returns the call-scoped enterprise id for the active request, if one has been supplied
	 * (e.g. by a REST or event-bus entry point).
	 * <p>
	 * Unlike {@link #applicationEnterpriseName} — the process-wide enterprise <em>name</em> resolved
	 * once at application startup — this is the enterprise the <strong>current call</strong> is
	 * operating against and may differ per request. Incoming calls onto the REST/event-bus surface
	 * pass an enterprise id which is placed on the context here for use across Activity Master.
	 *
	 * @return the call-scoped enterprise id, or {@code null} when none is set on the active scope
	 */
	public UUID getEnterpriseId()
	{
		return getScopedProperty(SCOPE_KEY_ENTERPRISE_ID);
	}

	/**
	 * Sets the call-scoped enterprise id for the active request. Pass {@code null} to clear it.
	 *
	 * @param enterpriseId the enterprise id for the current call
	 * @return this configuration for chaining
	 */
	@SuppressWarnings("UnusedReturnValue")
	public ActivityMasterConfiguration setEnterpriseId(UUID enterpriseId)
	{
		setScopedProperty(SCOPE_KEY_ENTERPRISE_ID, enterpriseId);
		return this;
	}

	/**
	 * Returns the call-scoped identity token of the current caller (the authenticated user/system
	 * identity, an {@code Identity}-classification security token UUID). This is the token threaded
	 * into row-level access checks (e.g. {@code canRead}/{@code canWrite}) for the active request.
	 *
	 * @return the current identity token, or {@code null} when none is set on the active scope
	 */
	public UUID getIdentityToken()
	{
		return getScopedProperty(SCOPE_KEY_IDENTITY_TOKEN);
	}

	/**
	 * Sets the call-scoped identity token of the current caller. Pass {@code null} to clear it.
	 *
	 * @param identityToken the caller's identity token for the current call
	 * @return this configuration for chaining
	 */
	@SuppressWarnings("UnusedReturnValue")
	public ActivityMasterConfiguration setIdentityToken(UUID identityToken)
	{
		setScopedProperty(SCOPE_KEY_IDENTITY_TOKEN, identityToken);
		return this;
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
		setEnterpriseId(dto.getEnterpriseId());
		setIdentityToken(dto.getIdentityToken());
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
		private UUID enterpriseId;
		private UUID identityToken;
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
			enterpriseId = ActivityMasterConfiguration.get()
			                                          .getEnterpriseId();
			identityToken = ActivityMasterConfiguration.get()
			                                           .getIdentityToken();
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

		public UUID getEnterpriseId()
		{
			return enterpriseId;
		}

		public ActivityMasterConfigurationDTO setEnterpriseId(UUID enterpriseId)
		{
			this.enterpriseId = enterpriseId;
			return this;
		}

		public UUID getIdentityToken()
		{
			return identityToken;
		}

		public ActivityMasterConfigurationDTO setIdentityToken(UUID identityToken)
		{
			this.identityToken = identityToken;
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
