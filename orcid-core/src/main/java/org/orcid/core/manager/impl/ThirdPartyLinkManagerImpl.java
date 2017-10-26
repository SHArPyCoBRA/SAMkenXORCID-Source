/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2014 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.core.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.orcid.core.manager.ThirdPartyLinkManager;
import org.orcid.core.utils.JsonUtils;
import org.orcid.jaxb.model.clientgroup.OrcidClient;
import org.orcid.jaxb.model.clientgroup.RedirectUriType;
import org.orcid.jaxb.model.clientgroup.RedirectUris;
import org.orcid.jaxb.model.message.ScopePathType;
import org.orcid.jaxb.model.v3.dev1.client.Client;
import org.orcid.jaxb.model.v3.dev1.client.ClientRedirectUri;
import org.orcid.persistence.dao.ClientRedirectDao;
import org.orcid.persistence.dao.OrcidPropsDao;
import org.orcid.persistence.jpa.entities.ClientDetailsEntity;
import org.orcid.persistence.jpa.entities.ClientRedirectUriEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

public class ThirdPartyLinkManagerImpl implements ThirdPartyLinkManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPartyLinkManagerImpl.class);

    private long localCacheVersion = 0;

    @Resource(name = "clientRedirectDao")
    private ClientRedirectDao clientRedirectDao;

    @Resource
    private OrcidPropsDao orcidPropsDao;

    public static class CacheVersion {
        private String version;
        private String createdDate;

        public CacheVersion() {

        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }
    }

    public long getLocalCacheVersion() {
        return localCacheVersion;
    }

    public void setLocalCacheVersion(long localCacheVersionParam) {
        this.localCacheVersion = localCacheVersionParam;
    }

    /**
     * Get the latest database version.
     * */
    public long getDatabaseCacheVersion() {
        String version = orcidPropsDao.getValue(CACHE_VERSION_KEY);
        long result = 0;
        if (version != null) {
            CacheVersion dbCacheVersion = JsonUtils.readObjectFromJsonString(version, CacheVersion.class);
            result = Long.valueOf(dbCacheVersion.getVersion());
        } else {
            // This case will happen only the first time, it will indicate that
            // the cache key is not on database, so, it must be created
            CacheVersion newVersion = new CacheVersion();
            result = ++localCacheVersion;
            newVersion.setVersion(String.valueOf(result));
            newVersion.setCreatedDate(new Date().toString());
            String jsonVersion = JsonUtils.convertToJsonString(newVersion);
            orcidPropsDao.create(CACHE_VERSION_KEY, jsonVersion);
        }
        return result;
    }

    /**
     * Updates the cache version on database
     * */
    public void updateDatabaseCacheVersion() {
        long version = getDatabaseCacheVersion();
        CacheVersion newVersion = new CacheVersion();
        newVersion.setVersion(String.valueOf(++version));
        newVersion.setCreatedDate(new Date().toString());
        String jsonVersion = JsonUtils.convertToJsonString(newVersion);

        if (orcidPropsDao.exists(CACHE_VERSION_KEY)) {
            orcidPropsDao.update(CACHE_VERSION_KEY, jsonVersion);
        } else {
            orcidPropsDao.create(CACHE_VERSION_KEY, jsonVersion);
        }
    }

    /**
     * Updates the local cache version with the latest db version
     * */
    private void updateLocalCacheVersion() {
        setLocalCacheVersion(getDatabaseCacheVersion());
    }

    @Cacheable("import-works-clients")
    public List<Client> findOrcidClientsWithPredefinedOauthScopeWorksImport() {
        updateLocalCacheVersion();
        LOGGER.debug("Updating cache for import-works-clients, new version: " + this.localCacheVersion);
        return getClients(RedirectUriType.IMPORT_WORKS_WIZARD);
    }

    @Cacheable("import-funding-clients")
    public List<OrcidClient> findOrcidClientsWithPredefinedOauthScopeFundingImport() {
        updateLocalCacheVersion();
        LOGGER.debug("Updating cache for import-funding-clients, new version: " + this.localCacheVersion);
        return getClients(RedirectUriType.IMPORT_FUNDING_WIZARD);
    }

    @Override
    @Cacheable("read-access-clients")
    public List<OrcidClient> findOrcidClientsWithPredefinedOauthScopeReadAccess() {
        updateLocalCacheVersion();
        LOGGER.debug("Updating cache for read-access-clients, new version: " + this.localCacheVersion);
        return getClients(RedirectUriType.GRANT_READ_WIZARD);
    }
    
	@Override
	@Cacheable("import-peer-review-clients")
	public List<OrcidClient> findOrcidClientsWithPredefinedOauthScopePeerReviewImport() {
		updateLocalCacheVersion();
        return getClients(RedirectUriType.IMPORT_PEER_REVIEW_WIZARD);
	}

    @Override
    @CacheEvict(value = { "read-access-clients", "import-works-clients", "import-funding-clients", "import-peer-review-clients" }, allEntries = true)
    public void evictAll() {
        LOGGER.debug("read-access-clients and import-works-clients all keys  evicted");
    }

    private List<Client> getClients(RedirectUriType rut) {
        List<Client> clients = new ArrayList<Client>();
        List<ClientRedirectUriEntity> entitiesWithPredefinedScopes = clientRedirectDao.findClientDetailsWithRedirectScope();
        for (ClientRedirectUriEntity entity : entitiesWithPredefinedScopes) {
            if (rut.value().equals(entity.getRedirectUriType())) {
                if (rut.value().equals(entity.getRedirectUriType())) {
                    
                }
            }
        }
        return clients;
    }
    
    private List<OrcidClient> getClientsLegacy(RedirectUriType rut) {
        List<OrcidClient> orcidClients = new ArrayList<OrcidClient>();
        List<ClientRedirectUriEntity> entitiesWithPredefinedScopes = clientRedirectDao.findClientDetailsWithRedirectScope();

        for (ClientRedirectUriEntity entity : entitiesWithPredefinedScopes) {

            if (rut.value().equals(entity.getRedirectUriType())) {
                ClientDetailsEntity clientDetails = entity.getClientDetailsEntity();
                ClientRedirectUri redirectUri = new ClientRedirectUri();
                redirectUri.setRedirectUri(entity.getRedirectUri());
                String predefinedScopes = entity.getPredefinedClientScope();
                redirectUri.setPredefinedClientScopes(new HashSet<ScopePathType>(ScopePathType.getScopesFromSpaceSeparatedString(predefinedScopes)));
                redirectUri.setRedirectUriType(entity.getRedirectUriType());
                redirectUri.setUriActType(entity.getUriActType());
                redirectUri.setUriGeoArea(entity.getUriGeoArea());
                RedirectUris redirectUris = new RedirectUris();
                redirectUris.getRedirectUri().add(redirectUri);

                OrcidClient minimalClientDetails = new OrcidClient();
                minimalClientDetails.setDisplayName(clientDetails.getClientName());
                minimalClientDetails.setShortDescription(clientDetails.getClientDescription());
                minimalClientDetails.setClientId(clientDetails.getClientId());
                minimalClientDetails.setRedirectUris(redirectUris);
                orcidClients.add(minimalClientDetails);
            }

        }
        return orcidClients;
    }
}
