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
package org.orcid.persistence.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.orcid.jaxb.model.common_v2.Visibility;
import org.orcid.persistence.jpa.entities.PeerReviewEntity;

public interface PeerReviewDao extends GenericDao<PeerReviewEntity, Long> {

    /**
     * Find and retrieve a peer review that have the given id and belongs to the given user
     * 
     * @param userOrcid
     *            The owner of the peerReview
     * @param peerReviewId
     *            The id of the element
     * @return a peer review entity that have the give id and belongs to the given user 
     * */
    PeerReviewEntity getPeerReview(String userOrcid, Long peerReviewId);
    
    /**
     * Removes the relationship that exists between a peerReview and a profile.
     * 
     * @param peerReviewId
     *            The id of the peerReview that will be removed from the
     *            client profile
     * @param userOrcid
     *            The user orcid
     * @return true if the relationship was deleted
     * */
    boolean removePeerReview(String userOrcid, Long peerReviewId);     
    
    
    /**
     * Find and retrieve all peer reviews that belongs to a user
     * 
     * @param userOrcid
     *            The owner of the peerReview
     * @return a list will all peer reviews associated with the given user 
     * */
    List<PeerReviewEntity> getByUser(String userOrcid);
    
    boolean updateToMaxDisplay(String orcid, Long peerReviewId);
    
    boolean updateVisibilities(String orcid, ArrayList<Long> peerReviewIds, Visibility visibility);
    
    /**
     * Returns a list of  ids of peer reviews that still have old external identifiers
     * @param limit
     *          The batch number to fetch
     * @return a list of peer review ids with old ext ids          
     * */
    List<BigInteger> getPeerReviewWithOldExtIds(long limit);
    
    boolean increaseDisplayIndexOnAllElements(String orcid);
}
