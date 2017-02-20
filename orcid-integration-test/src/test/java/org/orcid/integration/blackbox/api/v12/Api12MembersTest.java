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
package org.orcid.integration.blackbox.api.v12;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jettison.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orcid.integration.api.helper.APIRequestType;
import org.orcid.integration.api.t2.T2OAuthAPIService;
import org.orcid.integration.blackbox.api.v2.release.BlackBoxBaseV2Release;
import org.orcid.jaxb.model.message.Affiliation;
import org.orcid.jaxb.model.message.AffiliationType;
import org.orcid.jaxb.model.message.CitationType;
import org.orcid.jaxb.model.message.ContributorRole;
import org.orcid.jaxb.model.message.CreditName;
import org.orcid.jaxb.model.message.Email;
import org.orcid.jaxb.model.message.ExternalIdCommonName;
import org.orcid.jaxb.model.message.ExternalIdReference;
import org.orcid.jaxb.model.message.ExternalIdentifier;
import org.orcid.jaxb.model.message.ExternalIdentifiers;
import org.orcid.jaxb.model.message.FamilyName;
import org.orcid.jaxb.model.message.Funding;
import org.orcid.jaxb.model.message.FundingContributorRole;
import org.orcid.jaxb.model.message.FundingExternalIdentifierType;
import org.orcid.jaxb.model.message.FundingType;
import org.orcid.jaxb.model.message.GivenNames;
import org.orcid.jaxb.model.message.Iso3166Country;
import org.orcid.jaxb.model.message.OrcidBio;
import org.orcid.jaxb.model.message.OrcidMessage;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.message.OrcidWork;
import org.orcid.jaxb.model.message.PersonalDetails;
import org.orcid.jaxb.model.message.ScopePathType;
import org.orcid.jaxb.model.message.SequenceType;
import org.orcid.jaxb.model.message.Visibility;
import org.orcid.jaxb.model.message.WorkExternalIdentifierType;
import org.orcid.jaxb.model.message.WorkType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sun.jersey.api.client.ClientResponse;

/**
 * 
 * @author Angel Montenegro
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-publicV2-context.xml" })
public class Api12MembersTest extends BlackBoxBaseV2Release {

    @Resource(name = "t2OAuthClient_1_2")
    protected T2OAuthAPIService<ClientResponse> t2OAuthClient_1_2;                       
    
    @Test
    public void addWorkTest() throws Exception {
        String clientId = getClient1ClientId();
        String clientRedirectUri = getClient1RedirectUri();
        String clientSecret = getClient1ClientSecret();
        String userId = getUser1OrcidId();
        String password = getUser1Password();

        String accessToken = getAccessToken(userId, password, Arrays.asList("/orcid-works/read-limited", "/activities/update"), clientId, clientSecret, clientRedirectUri,
                true);
        
        String title = "Work " + System.currentTimeMillis();
        
        Long putCode = null;
        Api12Helper.addWork(userId, accessToken, title, t2OAuthClient_1_2);

        ClientResponse response = t2OAuthClient_1_2.viewWorksDetailsXml(userId, accessToken);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("application/vnd.orcid+xml; charset=UTF-8; qs=5", response.getType().toString());
        OrcidMessage orcidMessageWithNewWork = response.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessageWithNewWork);
        assertNotNull(orcidMessageWithNewWork.getOrcidProfile());
        assertNotNull(orcidMessageWithNewWork.getOrcidProfile().getOrcidActivities());
        assertNotNull(orcidMessageWithNewWork.getOrcidProfile().getOrcidActivities().getOrcidWorks());
        assertNotNull(orcidMessageWithNewWork.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork());
        boolean found = false;
        for(OrcidWork work : orcidMessageWithNewWork.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork()) {
            if(title.equals(work.getWorkTitle().getTitle().getContent())) {                
                assertNotNull(work.getPutCode());
                putCode = Long.valueOf(work.getPutCode());
                found = true;
            }
        }
        assertTrue(found);
        
        // Delete it
        ClientResponse deleteResponse = memberV2ApiClient.deleteWorkXml(this.getUser1OrcidId(), putCode, accessToken);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
    }

    @Test
    public void addFundingTest() throws InterruptedException, JSONException {
        String clientId = getClient1ClientId();
        String clientRedirectUri = getClient1RedirectUri();
        String clientSecret = getClient1ClientSecret();
        String userId = getUser1OrcidId();
        String password = getUser1Password();

        String accessToken = getAccessToken(userId, password, Arrays.asList("/funding/read-limited", "/activities/update"), clientId, clientSecret, clientRedirectUri,
                true);
        String fundingTitle = "Funding " + System.currentTimeMillis();
        Long putCode = null;
        Api12Helper.addFunding(userId, accessToken, fundingTitle, t2OAuthClient_1_2);

        ClientResponse response = t2OAuthClient_1_2.viewFundingDetailsXml(userId, accessToken);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("application/vnd.orcid+xml; charset=UTF-8; qs=5", response.getType().toString());
        OrcidMessage orcidMessage = response.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessage);
        assertNotNull(orcidMessage.getOrcidProfile());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidActivities());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidActivities().getFundings());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings());
        
        boolean found = false;
        for(Funding funding : orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings()) {
            if(fundingTitle.equals(funding.getTitle().getTitle().getContent())) {
                assertNotNull(funding.getPutCode());
                putCode = Long.valueOf(funding.getPutCode());
                found = true;
            }
        }
        assertTrue(found);
        // Delete it
        ClientResponse deleteResponse = memberV2ApiClient.deleteFundingXml(this.getUser1OrcidId(), putCode, accessToken);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
    }

    @Test
    public void addAffiliationTest() throws InterruptedException, JSONException {
        String clientId = getClient1ClientId();
        String clientRedirectUri = getClient1RedirectUri();
        String clientSecret = getClient1ClientSecret();
        String userId = getUser1OrcidId();
        String password = getUser1Password();

        String accessToken = getAccessToken(userId, password, Arrays.asList("/affiliations/read-limited", "/activities/update"), clientId, clientSecret,
                clientRedirectUri, true);
        String orgName = "Org_" + System.currentTimeMillis();
        Long putCode = null;
        Api12Helper.addAffiliation(userId, accessToken, orgName, t2OAuthClient_1_2);

        ClientResponse response = t2OAuthClient_1_2.viewAffiliationDetailsXml(userId, accessToken);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("application/vnd.orcid+xml; charset=UTF-8; qs=5", response.getType().toString());
        OrcidMessage orcidMessage = response.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessage);
        assertNotNull(orcidMessage.getOrcidProfile());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidActivities());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliation());
        boolean found = false;
        for(Affiliation affiliation : orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliation()) {
            if(orgName.equals(affiliation.getOrganization().getName())) {
                assertNotNull(affiliation.getPutCode());
                putCode = Long.valueOf(affiliation.getPutCode());
                found = true;
            }
        }
        assertTrue(found);
        
        //Delete it
        ClientResponse deleteResponse = memberV2ApiClient.deleteEducationXml(this.getUser1OrcidId(), putCode, accessToken);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
    }

    @Test
    public void personUpdateTest() throws InterruptedException, JSONException {
        String clientId = getClient1ClientId();
        String clientRedirectUri = getClient1RedirectUri();
        String clientSecret = getClient1ClientSecret();
        String userId = getUser1OrcidId();
        String password = getUser1Password();
        String giveName = getUser1GivenName();
        String familyName = getUser1FamilyNames();
        String creditName = getUser1CreditName();        
        
        String accessToken = getAccessToken(userId, password, Arrays.asList("/person/update", "/orcid-bio/read-limited"), clientId, clientSecret, clientRedirectUri,
                true);
        OrcidMessage orcidMessage = new OrcidMessage();
        orcidMessage.setMessageVersion(OrcidMessage.DEFAULT_VERSION);
        OrcidProfile orcidProfile = new OrcidProfile();
        orcidMessage.setOrcidProfile(orcidProfile);
        OrcidBio orcidBio = new OrcidBio();
        PersonalDetails personalDetails = new PersonalDetails();
        personalDetails.setGivenNames(new GivenNames("My given name"));
        personalDetails.setFamilyName(new FamilyName("My family name"));
        CreditName creditNameElement = new CreditName("My credit name");
        creditNameElement.setVisibility(Visibility.LIMITED);
        personalDetails.setCreditName(creditNameElement);
        orcidBio.setPersonalDetails(personalDetails);
        orcidProfile.setOrcidBio(orcidBio);

        ClientResponse clientResponse = t2OAuthClient_1_2.updateBioDetailsXml(userId, orcidMessage, accessToken);
        assertEquals(200, clientResponse.getStatus());
        ClientResponse response = t2OAuthClient_1_2.viewBioDetailsXml(userId, accessToken);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        OrcidMessage orcidMessageWithBio = response.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessageWithBio);
        assertNotNull(orcidMessageWithBio.getOrcidProfile());
        assertNotNull(orcidMessageWithBio.getOrcidProfile().getOrcidBio());
        assertNotNull(orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails());
        assertNotNull(orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getGivenNames());
        assertEquals("My given name", orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getGivenNames().getContent());
        assertNotNull(orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getFamilyName());
        assertEquals("My family name", orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getFamilyName().getContent());
        assertNotNull(orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getCreditName());
        assertEquals("My credit name", orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getCreditName().getContent());
        assertEquals(Visibility.LIMITED, orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getCreditName().getVisibility());
        
        //Rollback changes
        orcidMessage = new OrcidMessage();
        orcidMessage.setMessageVersion(OrcidMessage.DEFAULT_VERSION);
        orcidProfile = new OrcidProfile();
        orcidMessage.setOrcidProfile(orcidProfile);
        orcidBio = new OrcidBio();
        personalDetails = new PersonalDetails();
        personalDetails.setGivenNames(new GivenNames(giveName));
        personalDetails.setFamilyName(new FamilyName(familyName));
        creditNameElement = new CreditName(creditName);
        creditNameElement.setVisibility(Visibility.PUBLIC);
        personalDetails.setCreditName(creditNameElement);
        orcidBio.setPersonalDetails(personalDetails);
        orcidProfile.setOrcidBio(orcidBio);

        clientResponse = t2OAuthClient_1_2.updateBioDetailsXml(userId, orcidMessage, accessToken);
        assertEquals(200, clientResponse.getStatus());
        
        response = t2OAuthClient_1_2.viewBioDetailsXml(userId, accessToken);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        orcidMessageWithBio = response.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessageWithBio);
        assertNotNull(orcidMessageWithBio.getOrcidProfile());
        assertNotNull(orcidMessageWithBio.getOrcidProfile().getOrcidBio());
        assertNotNull(orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails());
        assertNotNull(orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getGivenNames());
        assertEquals(giveName, orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getGivenNames().getContent());
        assertNotNull(orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getFamilyName());
        assertEquals(familyName, orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getFamilyName().getContent());
        assertNotNull(orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getCreditName());
        assertEquals(creditName, orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getCreditName().getContent());
        assertEquals(Visibility.PUBLIC, orcidMessageWithBio.getOrcidProfile().getOrcidBio().getPersonalDetails().getCreditName().getVisibility());
        
    }

    @Test
    public void externalIdentifiersUpdateTest() throws InterruptedException, JSONException {
        String clientId = getClient1ClientId();
        String clientRedirectUri = getClient1RedirectUri();
        String clientSecret = getClient1ClientSecret();
        String userId = getUser1OrcidId();
        String password = getUser1Password();

        String accessToken = getAccessToken(userId, password, Arrays.asList("/person/update", "/orcid-bio/read-limited"), clientId, clientSecret, clientRedirectUri,
                true);
        OrcidMessage orcidMessage = new OrcidMessage();
        orcidMessage.setMessageVersion(OrcidMessage.DEFAULT_VERSION);
        OrcidProfile orcidProfile = new OrcidProfile();
        OrcidBio orcidBio = new OrcidBio();
        ExternalIdentifier extId = new ExternalIdentifier();
        Long time = System.currentTimeMillis();
        String commonName = "ext-id-common-name-" + time;
        String idReference = "ext-id-reference-" + time;
        extId.setExternalIdCommonName(new ExternalIdCommonName(commonName));
        extId.setExternalIdReference(new ExternalIdReference(idReference));
        ExternalIdentifiers extIds = new ExternalIdentifiers();
        extIds.getExternalIdentifier().add(extId);
        orcidBio.setExternalIdentifiers(extIds);
        orcidProfile.setOrcidBio(orcidBio);
        orcidMessage.setOrcidProfile(orcidProfile);
        ClientResponse clientResponse = t2OAuthClient_1_2.addExternalIdentifiersXml(userId, orcidMessage, accessToken);
        assertEquals(200, clientResponse.getStatus());
        ClientResponse response = t2OAuthClient_1_2.viewBioDetailsXml(userId, accessToken);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        OrcidMessage orcidMessageWithExtIds = response.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessageWithExtIds);
        assertNotNull(orcidMessageWithExtIds.getOrcidProfile());
        assertNotNull(orcidMessageWithExtIds.getOrcidProfile().getOrcidBio());
        assertNotNull(orcidMessageWithExtIds.getOrcidProfile().getOrcidBio().getExternalIdentifiers());
        boolean found = false;
        for(ExternalIdentifier newExtId : orcidMessageWithExtIds.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier()) {
            if(commonName.equals(newExtId.getExternalIdCommonName().getContent())) {
                assertEquals(idReference, newExtId.getExternalIdReference().getContent());
                found = true;
            }
        }
        assertTrue(found);
        
        //TODO: how to delete this guy?
    }

    @Test
    public void activitiesReadLimitedTest() throws InterruptedException, JSONException {
        String clientId = getClient1ClientId();
        String clientRedirectUri = getClient1RedirectUri();
        String clientSecret = getClient1ClientSecret();
        String userId = getUser1OrcidId();
        String password = getUser1Password();

        String workTitle = "Work " + System.currentTimeMillis();
        String fundingTitle = "Funding " + System.currentTimeMillis();
        String orgName = "Org_" + System.currentTimeMillis();
        
        String accessToken = getAccessToken(userId, password, Arrays.asList("/read-limited", "/activities/update"), clientId, clientSecret, clientRedirectUri, true);
        Long workPutCode = null;
        Api12Helper.addWork(userId, accessToken, workTitle, t2OAuthClient_1_2);        
        Long fundingPutCode = null; 
        Api12Helper.addFunding(userId, accessToken, fundingTitle, t2OAuthClient_1_2);
        Long affiliationPutCode = null;
        Api12Helper.addAffiliation(userId, accessToken, orgName, t2OAuthClient_1_2);

        ClientResponse worksResponse = t2OAuthClient_1_2.viewWorksDetailsXml(userId, accessToken);
        assertNotNull(worksResponse);
        assertEquals(200, worksResponse.getStatus());
        assertEquals("application/vnd.orcid+xml; charset=UTF-8; qs=5", worksResponse.getType().toString());
        OrcidMessage orcidMessageWithNewWork = worksResponse.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessageWithNewWork);
        assertNotNull(orcidMessageWithNewWork.getOrcidProfile());
        assertNotNull(orcidMessageWithNewWork.getOrcidProfile().getOrcidActivities());
        assertNotNull(orcidMessageWithNewWork.getOrcidProfile().getOrcidActivities().getOrcidWorks());
        assertNotNull(orcidMessageWithNewWork.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork());
        assertTrue(orcidMessageWithNewWork.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().size() > 0);

        boolean workFound = false;

        for (OrcidWork work : orcidMessageWithNewWork.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork()) {
            if (workTitle.equals(work.getWorkTitle().getTitle().getContent())) {
                // Default user visibility should be public
                assertEquals(Visibility.PUBLIC, work.getVisibility());
                assertNotNull(work.getPutCode());
                workPutCode = Long.valueOf(work.getPutCode());
                workFound = true;
            }
        }

        assertTrue(workFound);

        ClientResponse fundingResponse = t2OAuthClient_1_2.viewFundingDetailsXml(userId, accessToken);
        assertNotNull(fundingResponse);
        assertEquals(200, fundingResponse.getStatus());
        assertEquals("application/vnd.orcid+xml; charset=UTF-8; qs=5", fundingResponse.getType().toString());
        OrcidMessage orcidMessageWithNewFunding = fundingResponse.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessageWithNewFunding);
        assertNotNull(orcidMessageWithNewFunding.getOrcidProfile());
        assertNotNull(orcidMessageWithNewFunding.getOrcidProfile().getOrcidActivities());
        assertNotNull(orcidMessageWithNewFunding.getOrcidProfile().getOrcidActivities().getFundings());
        assertNotNull(orcidMessageWithNewFunding.getOrcidProfile().getOrcidActivities().getFundings().getFundings());
        assertTrue(orcidMessageWithNewFunding.getOrcidProfile().getOrcidActivities().getFundings().getFundings().size() > 0);

        boolean fundingFound = false;

        for (Funding funding : orcidMessageWithNewFunding.getOrcidProfile().getOrcidActivities().getFundings().getFundings()) {            
            if (fundingTitle.equals(funding.getTitle().getTitle().getContent())) {
                // Default user visibility should be public
                assertEquals(Visibility.PUBLIC, funding.getVisibility());
                assertNotNull(funding.getPutCode());
                fundingPutCode = Long.valueOf(funding.getPutCode());
                fundingFound = true;
            }
        }

        assertTrue(fundingFound);

        ClientResponse affiliationResponse = t2OAuthClient_1_2.viewAffiliationDetailsXml(userId, accessToken);
        assertNotNull(affiliationResponse);
        assertEquals(200, affiliationResponse.getStatus());
        assertEquals("application/vnd.orcid+xml; charset=UTF-8; qs=5", affiliationResponse.getType().toString());
        OrcidMessage orcidMessageWithNewAffiliation = affiliationResponse.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessageWithNewAffiliation);
        assertNotNull(orcidMessageWithNewAffiliation.getOrcidProfile());
        assertNotNull(orcidMessageWithNewAffiliation.getOrcidProfile().getOrcidActivities());
        assertNotNull(orcidMessageWithNewAffiliation.getOrcidProfile().getOrcidActivities().getAffiliations());
        assertNotNull(orcidMessageWithNewAffiliation.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliation());
        assertTrue(orcidMessageWithNewAffiliation.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliation().size() > 0);

        boolean affiliationFound = false;

        for (Affiliation affiliation : orcidMessageWithNewAffiliation.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliation()) {
            if (orgName.equals(affiliation.getOrganization().getName())) {
                // Default user visibility should be public
                assertEquals(Visibility.PUBLIC, affiliation.getVisibility());
                assertNotNull(affiliation.getPutCode());
                affiliationPutCode = Long.valueOf(affiliation.getPutCode());
                affiliationFound = true;
            }
        }

        assertTrue(affiliationFound);
        
        // Delete work
        ClientResponse deleteResponse = memberV2ApiClient.deleteWorkXml(this.getUser1OrcidId(), workPutCode, accessToken);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
        
        // Delete funding
        deleteResponse = memberV2ApiClient.deleteFundingXml(this.getUser1OrcidId(), fundingPutCode, accessToken);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
                
        // Delete affiliation
        deleteResponse = memberV2ApiClient.deleteEducationXml(this.getUser1OrcidId(), affiliationPutCode, accessToken);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
    }
    
    @Test
    public void testViewBioDetails() throws Exception {
        JAXBContext context = JAXBContext.newInstance(OrcidMessage.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        OrcidMessage record = (OrcidMessage) unmarshaller.unmarshal(Api12MembersTest.class.getResourceAsStream("/samples/small_orcid_profile.xml"));        
        record.getOrcidProfile().setOrcidHistory(null);
        String emailAddress = System.currentTimeMillis() + "_test@test.orcid.org";
        Email email = new Email(emailAddress);
        email.setPrimary(true);
        List<Email> emails = Arrays.asList(email);        
        record.getOrcidProfile().getOrcidBio().getContactDetails().setEmail(emails);
        
        String accessToken = getClientCredentialsAccessToken(ScopePathType.ORCID_PROFILE_CREATE, this.getClient1ClientId(), this.getClient1ClientSecret(), APIRequestType.MEMBER);
        
        String orcid = Api12Helper.createRecord(accessToken, record, t2OAuthClient_1_2);
        assertClientResponse401Details(t2OAuthClient_1_2.viewBioDetailsXml(orcid, null));
        
        ClientResponse clientResponse = t2OAuthClient_1_2.viewBioDetailsXml(orcid, accessToken);
        assertNotNull(clientResponse);
        assertEquals(200, clientResponse.getStatus());
        assertEquals("application/vnd.orcid+xml; charset=UTF-8; qs=5", clientResponse.getType().toString());
        OrcidMessage orcidMessage = clientResponse.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessage);
        assertEquals(orcid, orcidMessage.getOrcidProfile().getOrcidIdentifier().getPath());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidBio());
        assertEquals(1, orcidMessage.getOrcidProfile().getOrcidBio().getContactDetails().getEmail().size());
        assertEquals(emailAddress, orcidMessage.getOrcidProfile().getOrcidBio().getContactDetails().getEmail().get(0).getValue());
        assertEquals(Iso3166Country.CR, orcidMessage.getOrcidProfile().getOrcidBio().getContactDetails().getAddress().getCountry().getValue());                
        assertEquals("credit", orcidMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getCreditName().getContent());
        assertEquals("family", orcidMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getFamilyName().getContent());
        assertEquals("given", orcidMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getGivenNames().getContent());
        assertEquals(1, orcidMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getOtherNames().getOtherName().size());
        assertEquals("other", orcidMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getOtherNames().getOtherName().get(0).getContent());
        assertEquals("biography", orcidMessage.getOrcidProfile().getOrcidBio().getBiography().getContent());
        assertEquals(1, orcidMessage.getOrcidProfile().getOrcidBio().getResearcherUrls().getResearcherUrl().size());
        assertEquals("http://www.site.com", orcidMessage.getOrcidProfile().getOrcidBio().getResearcherUrls().getResearcherUrl().get(0).getUrl().getValue());
        assertEquals("The site", orcidMessage.getOrcidProfile().getOrcidBio().getResearcherUrls().getResearcherUrl().get(0).getUrlName().getContent());
        assertEquals(1, orcidMessage.getOrcidProfile().getOrcidBio().getKeywords().getKeyword().size());
        assertEquals("K1", orcidMessage.getOrcidProfile().getOrcidBio().getKeywords().getKeyword().get(0).getContent());
        assertEquals(1, orcidMessage.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier().size());
        assertEquals("extId#1", orcidMessage.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier().get(0).getExternalIdCommonName().getContent());
        assertEquals("extId#1", orcidMessage.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier().get(0).getExternalIdReference().getContent());
        assertEquals("http://orcid.org/extId#1", orcidMessage.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier().get(0).getExternalIdUrl().getValue());
    }
    
    @Test
    public void testViewRecord() throws Exception {
        JAXBContext context = JAXBContext.newInstance(OrcidMessage.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        OrcidMessage record = (OrcidMessage) unmarshaller.unmarshal(Api12MembersTest.class.getResourceAsStream("/samples/orcid_profile.xml"));        
        record.getOrcidProfile().setOrcidHistory(null);
        String emailAddress = System.currentTimeMillis() + "_test@test.orcid.org";
        Email email = new Email(emailAddress);
        email.setPrimary(true);
        List<Email> emails = Arrays.asList(email);        
        record.getOrcidProfile().getOrcidBio().getContactDetails().setEmail(emails);
        
        String accessToken = getClientCredentialsAccessToken(ScopePathType.ORCID_PROFILE_CREATE, this.getClient1ClientId(), this.getClient1ClientSecret(), APIRequestType.MEMBER);
        
        String orcid = Api12Helper.createRecord(accessToken, record, t2OAuthClient_1_2);
        assertClientResponse401Details(t2OAuthClient_1_2.viewFullDetailsXml(orcid, null));
        
        ClientResponse clientResponse = t2OAuthClient_1_2.viewFullDetailsXml(orcid, accessToken);
        assertNotNull(clientResponse);
        assertEquals(200, clientResponse.getStatus());
        assertEquals("application/vnd.orcid+xml; charset=UTF-8; qs=5", clientResponse.getType().toString());
        OrcidMessage orcidMessage = clientResponse.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessage);
        assertEquals(orcid, orcidMessage.getOrcidProfile().getOrcidIdentifier().getPath());
        //Bio
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidBio());
        assertEquals(1, orcidMessage.getOrcidProfile().getOrcidBio().getContactDetails().getEmail().size());
        assertEquals(emailAddress, orcidMessage.getOrcidProfile().getOrcidBio().getContactDetails().getEmail().get(0).getValue());
        assertEquals(Iso3166Country.US, orcidMessage.getOrcidProfile().getOrcidBio().getContactDetails().getAddress().getCountry().getValue());                
        assertEquals("credit name", orcidMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getCreditName().getContent());
        assertEquals("family name", orcidMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getFamilyName().getContent());
        assertEquals("given names", orcidMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getGivenNames().getContent());
        assertEquals(2, orcidMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getOtherNames().getOtherName().size());
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getOtherNames().getOtherName().get(0).getContent(), anyOf(is("Other 1"), is("Other 2")));
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getOtherNames().getOtherName().get(1).getContent(), anyOf(is("Other 1"), is("Other 2")));
        assertEquals("Lets test some crazy stuff! How about a 1000 chars in japanese 難フドょ三点93隊は比埼回へ美宝投トしレ手採シ領馬公ノネ氏細ケロウヌ係入ヲハヌヱ厚院ひゃお川的ばぴイぎ言提オサ所熱サ自通をぶちス後筆本在親も。仕退ニレト由界フぱし庫越トメキホ来転を半別どば鈴内ト全41秀かゅや果物心マオル討赤モナマフ参月スどこや座念コソ言社ぐ食理ラセ対演ぎの変理かラ広暮クスネテ上進傑ゆくス。16要べ南聖ウチサ認府ネ長悪たやふ能住とぜべ王是にど新断切年セテ不紀つドょ歳県オスニ質38碁れ容57雑スネモ老泉シヘヒ関裕よはラク更記兆ムタワラ投困ゅんぜ治木ぞほ細通ウヤ年覧ト運現ざイたね冊7績よゆ暮誉ソサト投46軽ロ終時イ必迫の触勇囲速ぐイす。特なイりド通結ま鳥寄育ぼべく拶奈拳ナコ回書マク道月スどばせ社的ケ探尽タエホ枝力トル害地べっ治電は際材ヨ別断ツカウ文国ラヒ応治ぴぶ戦75収否禁辞4帝おだあへ平悪由ごと禁者ヒワコタ長強ふて見際まをづ質相抽綱もえ。卓側68点宇どのゆ式長実52染な憶オ調浄もゆ高京セハヘミ世曄タニ合東らね約環リ理聞話学ほ心褒ぼどばこ補禁球かを運人ねの判言ネツユタ級投とそらぜ辺罪サ放唱セ請月試セツヲリ票監笑怠殊ぼスお。断きだて野重探がそょ会上ミヘ社69点ルラコ憶オ調浄もゆ高京セハヘミ世曄タニ合東らね約環リ理聞話学ほ心褒ぼどばこ補禁球かを運人ねの判言ネツユタ級投とそらぜ辺罪サ放唱セ請月試セツヲリ票監笑怠殊ぼスお。断きだて野重探がそょ会上ミヘ社69点ルラコ憶オ調浄もゆ高京セハヘミ世曄タニ合東らね約環リ理聞話学ほ心褒ぼどばこ補禁球かを運人ねの判言ネツユタ級投とそらぜ辺罪サ放唱セ請月試セツヲリ票監笑怠殊ぼスお。断きだて野重探がそょ会上ミヘ社69点ルラコv憶オ調浄もゆ高京セハヘミ世曄タニ合東らね約環リ理聞話学ほ心褒ぼどばこ補禁球かを運人ねの判言ネツユタ級投とそらぜ辺罪サ放唱セ請月試セツヲリ票監笑怠殊ぼスお。断きだて野重探がそょ会上ミヘ社69点ルラコ憶オ調浄もゆ高京セハヘミ世曄タニ合東らね約環リ理聞話学ほ心褒ぼどばこ補禁球かを運人ねの判言ネツユタ級投とそらぜ辺罪サ放唱セ請月試セツヲリ票監笑怠殊ぼスお。断きだて野重探がそょ会上ミヘ社69点ルラコ人ねの判言ネツユタ級投とそらぜ辺罪サ放唱セ請月試セツヲリ票監笑怠殊ぼスお。断きだ", orcidMessage.getOrcidProfile().getOrcidBio().getBiography().getContent());
        assertEquals(2, orcidMessage.getOrcidProfile().getOrcidBio().getResearcherUrls().getResearcherUrl().size());
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getResearcherUrls().getResearcherUrl().get(0).getUrl().getValue(), anyOf(is("http://www.wjrs.co.uk"), is("http://www.vvs.com")));        
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getResearcherUrls().getResearcherUrl().get(1).getUrl().getValue(), anyOf(is("http://www.wjrs.co.uk"), is("http://www.vvs.com")));
        assertEquals(2, orcidMessage.getOrcidProfile().getOrcidBio().getKeywords().getKeyword().size());
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getKeywords().getKeyword().get(0).getContent(), anyOf(is("Pavement Studies"), is("Advanced Tea Making")));
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getKeywords().getKeyword().get(1).getContent(), anyOf(is("Pavement Studies"), is("Advanced Tea Making")));
        assertEquals(2, orcidMessage.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier().size());
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier().get(0).getExternalIdCommonName().getContent(), anyOf(is("extId#1"), is("extId#2")));
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier().get(1).getExternalIdCommonName().getContent(), anyOf(is("extId#1"), is("extId#2")));        
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier().get(0).getExternalIdReference().getContent(), anyOf(is("extId#1"), is("extId#2")));
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier().get(1).getExternalIdReference().getContent(), anyOf(is("extId#1"), is("extId#2")));        
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier().get(0).getExternalIdUrl().getValue(), anyOf(is("http://orcid.org/extId#1"), is("http://orcid.org/extId#2")));
        assertThat(orcidMessage.getOrcidProfile().getOrcidBio().getExternalIdentifiers().getExternalIdentifier().get(1).getExternalIdUrl().getValue(), anyOf(is("http://orcid.org/extId#1"), is("http://orcid.org/extId#2")));
        
        //Affiliations
        assertEquals(2, orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliation().size());
        assertEquals(1, orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).size());
        assertEquals(AffiliationType.EDUCATION, orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getType());
        assertEquals("department-name", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getDepartmentName());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getEndDate().getDay().getValue());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getEndDate().getMonth().getValue());
        assertEquals("2018", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getEndDate().getYear().getValue());
        assertEquals("city", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getOrganization().getAddress().getCity());
        assertEquals(Iso3166Country.US, orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getOrganization().getAddress().getCountry());
        assertEquals("region", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getOrganization().getAddress().getRegion());        
        assertEquals("role-title", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getRoleTitle());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getStartDate().getDay().getValue());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getStartDate().getMonth().getValue());
        assertEquals("2017", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EDUCATION).get(0).getStartDate().getYear().getValue());                
        assertEquals(AffiliationType.EMPLOYMENT, orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getType());
        assertEquals("department-name", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getDepartmentName());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getEndDate().getDay().getValue());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getEndDate().getMonth().getValue());
        assertEquals("2018", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getEndDate().getYear().getValue());
        assertEquals("city", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getOrganization().getAddress().getCity());
        assertEquals(Iso3166Country.US, orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getOrganization().getAddress().getCountry());
        assertEquals("region", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getOrganization().getAddress().getRegion());        
        assertEquals("role-title", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getRoleTitle());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getStartDate().getDay().getValue());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getStartDate().getMonth().getValue());
        assertEquals("2017", orcidMessage.getOrcidProfile().getOrcidActivities().getAffiliations().getAffiliationsByType(AffiliationType.EMPLOYMENT).get(0).getStartDate().getYear().getValue()); 
        //Funding
        assertEquals(1, orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().size());
        assertEquals("1000", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getAmount().getContent());
        assertEquals("USD", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getAmount().getCurrencyCode());
        assertEquals("short-description", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getDescription());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getEndDate().getDay().getValue());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getEndDate().getMonth().getValue());
        assertEquals("2018", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getEndDate().getYear().getValue());
        assertEquals(FundingContributorRole.LEAD, orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getFundingContributors().getContributor().get(0).getContributorAttributes().getContributorRole());
        //Contributors email should be removed
        assertNull("test@test.orcid.org", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getFundingContributors().getContributor().get(0).getContributorEmail());
        assertEquals("credit-name", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getFundingContributors().getContributor().get(0).getCreditName().getContent());
        assertEquals(FundingExternalIdentifierType.GRANT_NUMBER, orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getFundingExternalIdentifiers().getFundingExternalIdentifier().get(0).getType());
        assertEquals("http://orcid.org", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getFundingExternalIdentifiers().getFundingExternalIdentifier().get(0).getUrl().getValue());
        assertEquals("funding-external-identifier-value", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getFundingExternalIdentifiers().getFundingExternalIdentifier().get(0).getValue());
        assertEquals("city", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getOrganization().getAddress().getCity());
        assertEquals(Iso3166Country.US, orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getOrganization().getAddress().getCountry());
        assertEquals("region", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getOrganization().getAddress().getRegion());
        assertEquals("name", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getOrganization().getName());
        assertEquals("organization-defined-type", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getOrganizationDefinedFundingType().getContent());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getStartDate().getDay().getValue());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getStartDate().getMonth().getValue());
        assertEquals("2017", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getStartDate().getYear().getValue());
        assertEquals("title", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getTitle().getTitle().getContent());
        assertEquals(FundingType.GRANT, orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getType());
        assertEquals("http://orcid.org", orcidMessage.getOrcidProfile().getOrcidActivities().getFundings().getFundings().get(0).getUrl().getValue());
        //Works
        assertEquals(1, orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().size());
        assertEquals(Iso3166Country.US, orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getCountry().getValue());
        assertEquals("journal title", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getJournalTitle().getContent());
        assertEquals("en", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getLanguageCode());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getPublicationDate().getDay().getValue());
        assertEquals("01", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getPublicationDate().getMonth().getValue());
        assertEquals("2017", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getPublicationDate().getYear().getValue());
        assertEquals("description", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getShortDescription());
        assertEquals("http://orcid.org", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getUrl().getValue());
        assertEquals("citation", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getWorkCitation().getCitation());
        assertEquals(CitationType.FORMATTED_UNSPECIFIED, orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getWorkCitation().getWorkCitationType());
        assertEquals(ContributorRole.AUTHOR, orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getWorkContributors().getContributor().get(0).getContributorAttributes().getContributorRole());
        assertEquals(SequenceType.FIRST, orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getWorkContributors().getContributor().get(0).getContributorAttributes().getContributorSequence());
        assertNull(orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getWorkContributors().getContributor().get(0).getContributorEmail());
        assertEquals("credit-name", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getWorkContributors().getContributor().get(0).getCreditName().getContent());
        assertEquals("10.5555/12345ABCDE", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getWorkExternalIdentifiers().getWorkExternalIdentifier().get(0).getWorkExternalIdentifierId().getContent());
        assertEquals(WorkExternalIdentifierType.DOI, orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getWorkExternalIdentifiers().getWorkExternalIdentifier().get(0).getWorkExternalIdentifierType());
        assertEquals("title", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getWorkTitle().getTitle().getContent());
        assertEquals("subtitle", orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getWorkTitle().getSubtitle().getContent());
        assertEquals(WorkType.BOOK, orcidMessage.getOrcidProfile().getOrcidActivities().getOrcidWorks().getOrcidWork().get(0).getWorkType());
    }
    
    protected void assertClientResponse401Details(ClientResponse clientResponse) throws Exception {
        // we've created client details but not tied them to an access token
        assertEquals(401, clientResponse.getStatus());
        assertTrue(clientResponse.getHeaders().containsKey("WWW-Authenticate"));
        List<String> authHeaders = clientResponse.getHeaders().get("WWW-Authenticate");
        assertTrue(authHeaders.contains("Bearer realm=\"ORCID T2 API\", error=\"invalid_token\", error_description=\"Invalid access token: null\""));
    }
    
}