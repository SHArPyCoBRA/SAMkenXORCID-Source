/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2013 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.persistence.jpa.entities;

import java.util.SortedSet;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.orcid.jaxb.model.message.CurrencyCode;
import org.orcid.jaxb.model.message.GrantType;
import org.orcid.jaxb.model.message.Visibility;
import org.orcid.utils.NullUtils;

/**
 * orcid-entities - Dec 6, 2011 - ProfileInstitutionEntity
 * 
 * @author Declan Newman (declan)
 */

@Entity
@Table(name = "profile_grant")
public class ProfileGrantEntity extends BaseEntity<Long> implements Comparable<ProfileGrantEntity>, ProfileAware, SourceAware {

    private static final long serialVersionUID = -3187757614938904392L;

    private static final String PROFILE_GRANT = "profileGrant";	

	private Long id;
    private OrgEntity org;
    private ProfileEntity profile;
    private String title;
    private String description;
    private GrantType type;
    private CurrencyCode currencyCode;
    private String amount;
    private String url;
    private String contributorsJson;
	private StartDateEntity startDate;
    private EndDateEntity endDate;
    private Visibility visibility;
    private SortedSet<GrantExternalIdentifierEntity> externalIdentifiers;
    private ProfileEntity source;
	
    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "profile_grant_seq")
    @SequenceGenerator(name = "profile_grant_seq", sequenceName = "profile_grant_seq")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.REFRESH })
    @JoinColumn(name = "org_id", nullable = false)
	public OrgEntity getOrg() {
		return org;
	}

	public void setOrg(OrgEntity org) {
		this.org = org;
	}

	@Override
	@ManyToOne(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER)
    @JoinColumn(name = "orcid", nullable = false)
	public ProfileEntity getProfile() {
		return profile;
	}

	public void setProfile(ProfileEntity profile) {
		this.profile = profile;
	}

	@Column(name = "title", nullable = false)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Basic
    @Enumerated(EnumType.STRING)
	@Column(name="type")
	public GrantType getType() {
		return type;
	}

	public void setType(GrantType type) {
		this.type = type;
	}

	@Basic
    @Enumerated(EnumType.STRING)
	@Column(name="currency_code")
	public CurrencyCode getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(CurrencyCode currencyCode) {
		this.currencyCode = currencyCode;
	}

	@Column(name = "amount")
	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "contributors_json")
	public String getContributorsJson() {
		return contributorsJson;
	}

	public void setContributorsJson(String contributorsJson) {
		this.contributorsJson = contributorsJson;
	}
	
	public StartDateEntity getStartDate() {
		return startDate;
	}

	public void setStartDate(StartDateEntity startDate) {
		this.startDate = startDate;
	}

	public EndDateEntity getEndDate() {
		return endDate;
	}

	public void setEndDate(EndDateEntity endDate) {
		this.endDate = endDate;
	}

	@Basic
    @Enumerated(EnumType.STRING)
	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = PROFILE_GRANT, orphanRemoval = true)
	@Fetch(FetchMode.SUBSELECT)
    @Sort(type = SortType.NATURAL)
	public SortedSet<GrantExternalIdentifierEntity> getExternalIdentifiers() {
		return externalIdentifiers;
	}

	public void setExternalIdentifiers(SortedSet<GrantExternalIdentifierEntity> externalIdentifiers) {
		this.externalIdentifiers = externalIdentifiers;
	}

	@ManyToOne(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "source_id", nullable = true, updatable = false)
	public ProfileEntity getSource() {
		return source;
	}

	public void setSource(ProfileEntity source) {
		this.source = source;
	}

	@Override
	public int compareTo(ProfileGrantEntity other) {
		if (other == null) {
            return 1;
        }
        int compareEnds = compareEnds(endDate, other.getEndDate());
        if (compareEnds != 0) {
            return compareEnds;
        }
        int compareStarts = compareStarts(startDate, other.getStartDate());
        if (compareStarts != 0) {
            return compareStarts;
        }
        return compareNames(org.getName(), other.getOrg().getName());
	}

	private int compareEnds(FuzzyDateEntity endDate, FuzzyDateEntity otherEndDate) {
        if (NullUtils.anyNull(endDate, otherEndDate)) {
            return -NullUtils.compareNulls(endDate, otherEndDate);
        }
        return -endDate.compareTo(otherEndDate);
    }

    private int compareStarts(FuzzyDateEntity startDate, FuzzyDateEntity otherStartDate) {
        if (NullUtils.anyNull(startDate, otherStartDate)) {
            return NullUtils.compareNulls(startDate, otherStartDate);
        }
        return -startDate.compareTo(otherStartDate);
    }

    private int compareNames(String name, String otherName) {
        if (NullUtils.anyNull(name, otherName)) {
            return NullUtils.compareNulls(name, otherName);
        }
        return name.compareTo(otherName);
    }
    
    /**
     * Clean simple fields so that entity can be reused.
     */
    public void clean() {
        type = null;
        title = null;        
        startDate = null;
        endDate = null;
        visibility = null;
        description = null;
        currencyCode = null;
        amount = null;
        contributorsJson = null;
        url = null;
    }
}
