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
package org.orcid.core.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.orcid.core.BaseTest;
import org.orcid.jaxb.model.client_v2.Client;
import org.orcid.jaxb.model.client_v2.ClientRedirectUri;
import org.orcid.jaxb.model.clientgroup.ClientType;
import org.orcid.jaxb.model.clientgroup.RedirectUriType;
import org.orcid.jaxb.model.message.ScopePathType;
import org.orcid.persistence.dao.ClientDetailsDao;
import org.orcid.persistence.jpa.entities.ClientAuthorisedGrantTypeEntity;
import org.orcid.persistence.jpa.entities.ClientDetailsEntity;
import org.orcid.persistence.jpa.entities.ClientGrantedAuthorityEntity;
import org.orcid.persistence.jpa.entities.ClientRedirectUriEntity;
import org.orcid.persistence.jpa.entities.ClientResourceIdEntity;
import org.orcid.persistence.jpa.entities.ClientScopeEntity;
import org.orcid.persistence.jpa.entities.ClientSecretEntity;
import org.orcid.pojo.ajaxForm.PojoUtil;
import org.orcid.test.TargetProxyHelper;

public class ClientManagerTest extends BaseTest {
    private static final List<String> DATA_FILES = Arrays.asList("/data/SecurityQuestionEntityData.xml", "/data/SourceClientDetailsEntityData.xml");
    
    private static final String MEMBER_ID="5555-5555-5555-5558";
    
    @Resource
    private ClientManager clientManager;

    @Resource
    private ClientDetailsDao clientDetailsDao;
    
    @Resource
    private EncryptionManager encryptionManager;
    
    @Mock
    private SourceManager sourceManager;
        
    @BeforeClass
    public static void initDBUnitData() throws Exception {
        initDBUnitData(DATA_FILES);
    }    
    
    @AfterClass
    public static void removeDBUnitData() throws Exception {
        List<String> reversedDataFiles = new ArrayList<String>(DATA_FILES);
        Collections.reverse(reversedDataFiles);
        removeDBUnitData(reversedDataFiles);
    }
    
    @Before
    public void before() {        
        MockitoAnnotations.initMocks(this);
        TargetProxyHelper.injectIntoProxy(clientManager, "sourceManager", sourceManager);
        when(sourceManager.retrieveSourceOrcid()).thenReturn(MEMBER_ID);        
    }
    
    @Test
    public void createClientTest() {
        String seed = RandomStringUtils.randomAlphanumeric(15);
        Client client = getClient(seed, MEMBER_ID);
        assertFalse(client.getId().startsWith("APP-"));
        client = clientManager.create(client);
        assertTrue(client.getId().startsWith("APP-"));
        ClientDetailsEntity newEntity = clientDetailsDao.find(client.getId());
        assertNotNull(newEntity);
        assertNotNull(newEntity.getDateCreated());
        assertNotNull(newEntity.getLastModified());
        assertEquals(client.getId(), newEntity.getId());
        assertEquals(MEMBER_ID, newEntity.getGroupProfileId());
                
        assertNotNull(newEntity.getAccessTokenValiditySeconds());
        assertTrue(newEntity.isAllowAutoDeprecate());
        assertEquals("authentication-provider-id " + seed, newEntity.getAuthenticationProviderId());
        assertEquals("description " + seed, newEntity.getClientDescription());        
        assertEquals("client-name " + seed, newEntity.getClientName());
        assertEquals(ClientType.PREMIUM_CREATOR, newEntity.getClientType());
        assertEquals("client-website " + seed, newEntity.getClientWebsite());        
        assertEquals("email-access-reason " + seed, newEntity.getEmailAccessReason());                
        
        assertNotNull(newEntity.getClientRegisteredRedirectUris());   
        assertEquals(3, newEntity.getClientRegisteredRedirectUris().size());
        boolean found1 = false, found2 = false, found3 = false;
        for(ClientRedirectUriEntity rUri : newEntity.getClientRegisteredRedirectUris()) {
            assertNotNull(rUri.getRedirectUri());
            assertNotNull(rUri.getDateCreated());
            assertNotNull(rUri.getLastModified());            
            if(rUri.getRedirectUri().equals("redirect-uri-1 " + seed)) {
                assertEquals(ScopePathType.ACTIVITIES_READ_LIMITED.value(), rUri.getPredefinedClientScope());
                assertEquals("type-1 " + seed, rUri.getRedirectUriType());
                assertEquals("uri-act-type-1 " + seed, rUri.getUriActType());
                assertEquals("uri-geo-area-1 " + seed, rUri.getUriGeoArea());
                found1 = true;
            } else if(rUri.getRedirectUri().equals("redirect-uri-2 " + seed)) {
                assertEquals(ScopePathType.ACTIVITIES_UPDATE.value(), rUri.getPredefinedClientScope());
                assertEquals("type-2 " + seed, rUri.getRedirectUriType());
                assertEquals("uri-act-type-2 " + seed, rUri.getUriActType());
                assertEquals("uri-geo-area-2 " + seed, rUri.getUriGeoArea());
                found2 = true;
            } else if(rUri.getRedirectUri().equals("redirect-uri-3 " + seed)) {
                assertEquals(ScopePathType.AFFILIATIONS_CREATE.value(), rUri.getPredefinedClientScope());
                assertEquals("type-3 " + seed, rUri.getRedirectUriType());
                assertEquals("uri-act-type-3 " + seed, rUri.getUriActType());
                assertEquals("uri-geo-area-3 " + seed, rUri.getUriGeoArea());
                found3 = true;
            } else {
                fail("Invalid redirect uri: " + rUri.getRedirectUri());
            }
        }
        assertTrue(found1);
        assertTrue(found2);
        assertTrue(found3);
        validateClientConfigSettings(newEntity, null);
    }

    @Test
    public void edit() {
        String seed = RandomStringUtils.randomAlphanumeric(15);
        Client client = getClient(seed, MEMBER_ID);
        assertFalse(client.getId().startsWith("APP-"));
        // Create the client
        client = clientManager.create(client);
        String initialClientSecret = client.getDecryptedSecret();
        
        //Update some fields
        client.setAllowAutoDeprecate(false);
        client.setAuthenticationProviderId("updated-authentication-provider-id");
        client.setDescription("updated-desciption");
        client.setEmailAccessReason("updated-email-access-reason");
        client.setName("updated-client-name");
        client.setPersistentTokensEnabled(false);
        client.setWebsite("updated-website");        
        
        // Change group id, which should not be persisted
        client.setGroupProfileId("0000-0000-0000-0000");
        // Change client type, which should not be persisted
        client.setClientType(ClientType.UPDATER);
        
        // Add a new redirect uri
        ClientRedirectUri rUri = new ClientRedirectUri();
        Set<ScopePathType> scopes = new HashSet<ScopePathType>();
        scopes.add(ScopePathType.READ_LIMITED);
        scopes.add(ScopePathType.ACTIVITIES_UPDATE);
        rUri.setPredefinedClientScopes(scopes);
        rUri.setRedirectUri("new-redirect-uri");
        rUri.setRedirectUriType(RedirectUriType.IMPORT_WORKS_WIZARD.value());
        rUri.setUriActType("updated-uri-act-type");
        rUri.setUriGeoArea("updated-geo-area");
        client.getClientRedirectUris().add(rUri);
        
        //Edit the client
        Date editTime = new Date();
        clientManager.edit(client);
        
        //Verify new data is there
        ClientDetailsEntity entityClient = clientDetailsDao.find(client.getId());
        assertEquals(MEMBER_ID, entityClient.getGroupProfileId());
        assertEquals("updated-authentication-provider-id", entityClient.getAuthenticationProviderId());
        assertEquals("updated-desciption", entityClient.getClientDescription());
        assertEquals("updated-email-access-reason", entityClient.getEmailAccessReason());
        assertEquals("updated-client-name", entityClient.getClientName());
        assertEquals("updated-website", entityClient.getClientWebsite());
        assertEquals(initialClientSecret, encryptionManager.decryptForInternalUse(entityClient.getClientSecretForJpa()));
        assertFalse(entityClient.isAllowAutoDeprecate());
        assertFalse(entityClient.isPersistentTokensEnabled());
        
        //Verify config data doesn't changed
        validateClientConfigSettings(entityClient, editTime);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void editWithInvalidClientId() {
        Client client = new Client();
        client.setId("APP-0");
        clientManager.edit(client);
        fail();
    }

    @Test
    public void resetClientSecret() {
        
    }

    private void validateClientConfigSettings(ClientDetailsEntity entity, Date lastTimeEntityWasModified) {
        assertNotNull(entity.getAuthorizedGrantTypes());
        assertEquals(3, entity.getClientAuthorizedGrantTypes().size());
        boolean found1 = false, found2 = false, found3 = false;
        for(ClientAuthorisedGrantTypeEntity cagt : entity.getClientAuthorizedGrantTypes()) {
            assertNotNull(cagt.getDateCreated());
            assertTrue(lastTimeEntityWasModified == null ? true : lastTimeEntityWasModified.after(cagt.getDateCreated()));
            assertNotNull(cagt.getLastModified());
            assertTrue(lastTimeEntityWasModified == null ? true : lastTimeEntityWasModified.after(cagt.getLastModified()));
            if(cagt.getGrantType().equals("authorization_code")) {
                found1 = true;
            } else if(cagt.getGrantType().equals("client_credentials")) {
                found2 = true;
            } else if (cagt.getGrantType().equals("refresh_token")) {
                found3 = true;
            } else {
                fail("Invalid authorized grant type: " + cagt.getGrantType());
            }
        }
        assertTrue(found1);
        assertTrue(found2);
        assertTrue(found3);        
        
        assertNotNull(entity.getClientGrantedAuthorities());        
        for(ClientGrantedAuthorityEntity cga : entity.getClientGrantedAuthorities()) {
            assertNotNull(cga.getDateCreated());
            assertTrue(lastTimeEntityWasModified == null ? true : lastTimeEntityWasModified.after(cga.getDateCreated()));
            assertNotNull(cga.getLastModified());
            assertTrue(lastTimeEntityWasModified == null ? true : lastTimeEntityWasModified.after(cga.getLastModified()));
            assertEquals("ROLE_CLIENT", cga.getAuthority());
        }        
        
        assertNotNull(entity.getClientResourceIds());
        assertEquals(1, entity.getClientResourceIds().size());
        for(ClientResourceIdEntity cri : entity.getClientResourceIds()) {
            assertNotNull(cri.getDateCreated());
            assertTrue(lastTimeEntityWasModified == null ? true : lastTimeEntityWasModified.after(cri.getLastModified()));
            assertNotNull(cri.getLastModified());
            assertTrue(lastTimeEntityWasModified == null ? true : lastTimeEntityWasModified.after(cri.getLastModified()));
            assertEquals("orcid", cri.getResourceId());
        }
        
        Set<String> scopes = ClientType.getScopes(entity.getClientType());
        assertFalse(scopes.isEmpty());
        assertNotNull(entity.getClientScopes());
        for(ClientScopeEntity cs : entity.getClientScopes()) {
            assertNotNull(cs.getDateCreated());
            assertTrue(lastTimeEntityWasModified == null ? true : lastTimeEntityWasModified.after(cs.getLastModified()));
            assertNotNull(cs.getLastModified());
            assertTrue(lastTimeEntityWasModified == null ? true : lastTimeEntityWasModified.after(cs.getLastModified()));
            assertTrue(scopes.contains(cs.getScopeType()));
            // Remove it after finding it so we check there are no duplicates as well
            scopes.remove(cs.getScopeType());
        }
        assertTrue(scopes.isEmpty());
        
        assertNotNull(entity.getClientSecrets());
        assertEquals(1, entity.getClientSecrets().size());
        for(ClientSecretEntity cs : entity.getClientSecrets()) {
            assertNotNull(cs.getDateCreated());
            assertTrue(lastTimeEntityWasModified == null ? true : lastTimeEntityWasModified.after(cs.getLastModified()));
            assertNotNull(cs.getLastModified());
            assertTrue(lastTimeEntityWasModified == null ? true : lastTimeEntityWasModified.after(cs.getLastModified()));
            assertTrue(cs.isPrimary());
            assertFalse(PojoUtil.isEmpty(cs.getClientSecret()));
        }
        
        assertNotNull(entity.getCustomEmails());       
    }
    
    private Client getClient(String randomString, String memberId) {
        Client client = new Client();
        client.setAllowAutoDeprecate(true);
        client.setAuthenticationProviderId("authentication-provider-id " + randomString);
        client.setClientType(ClientType.CREATOR);
        client.setDescription("description " + randomString);
        client.setEmailAccessReason("email-access-reason " + randomString);
        client.setGroupProfileId(memberId);
        client.setId(randomString);
        client.setName("client-name " + randomString);
        client.setPersistentTokensEnabled(true);
        client.setWebsite("client-website " + randomString);

        Set<ClientRedirectUri> clientRedirectUris = new HashSet<ClientRedirectUri>();
        ClientRedirectUri rUri1 = new ClientRedirectUri();
        Set<ScopePathType> scopes1 = new HashSet<ScopePathType>();
        scopes1.add(ScopePathType.ACTIVITIES_READ_LIMITED);
        rUri1.setPredefinedClientScopes(scopes1);
        rUri1.setRedirectUri("redirect-uri-1 " + randomString);
        rUri1.setRedirectUriType("type-1 " + randomString);
        rUri1.setUriActType("uri-act-type-1 " + randomString);
        rUri1.setUriGeoArea("uri-geo-area-1 " + randomString);
        ClientRedirectUri rUri2 = new ClientRedirectUri();
        Set<ScopePathType> scopes2 = new HashSet<ScopePathType>();
        scopes2.add(ScopePathType.ACTIVITIES_UPDATE);
        rUri2.setPredefinedClientScopes(scopes2);
        rUri2.setRedirectUri("redirect-uri-2 " + randomString);
        rUri2.setRedirectUriType("type-2 " + randomString);
        rUri2.setUriActType("uri-act-type-2 " + randomString);
        rUri2.setUriGeoArea("uri-geo-area-2 " + randomString);
        ClientRedirectUri rUri3 = new ClientRedirectUri();
        Set<ScopePathType> scopes3 = new HashSet<ScopePathType>();
        scopes3.add(ScopePathType.AFFILIATIONS_CREATE);
        rUri3.setPredefinedClientScopes(scopes3);
        rUri3.setRedirectUri("redirect-uri-3 " + randomString);
        rUri3.setRedirectUriType("type-3 " + randomString);
        rUri3.setUriActType("uri-act-type-3 " + randomString);
        rUri3.setUriGeoArea("uri-geo-area-3 " + randomString);
        clientRedirectUris.add(rUri1);
        clientRedirectUris.add(rUri2);
        clientRedirectUris.add(rUri3);
        client.setClientRedirectUris(clientRedirectUris);        
        return client;
    }
}
