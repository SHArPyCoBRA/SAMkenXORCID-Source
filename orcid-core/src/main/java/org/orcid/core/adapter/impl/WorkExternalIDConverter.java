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
package org.orcid.core.adapter.impl;

import org.orcid.core.utils.JsonUtils;
import org.orcid.jaxb.model.common_rc1.Url;
import org.orcid.jaxb.model.record_rc1.Relationship;
import org.orcid.jaxb.model.record_rc1.WorkExternalIdentifier;
import org.orcid.jaxb.model.record_rc1.WorkExternalIdentifierId;
import org.orcid.jaxb.model.record_rc1.WorkExternalIdentifierType;
import org.orcid.jaxb.model.record_rc2.ExternalID;

import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

public class WorkExternalIDConverter extends BidirectionalConverter<ExternalID, String> {

    /** Uses rc1 as intermediary form
     * 
     */
    @Override
    public ExternalID convertFrom(String externalIdentifiersAsString, Type<ExternalID> arg1) {                
        WorkExternalIdentifier id = JsonUtils.readObjectFromJsonString(externalIdentifiersAsString, WorkExternalIdentifier.class);        
        return convertRC1toRC2(id);
    }

    /** Currently transforms into rc1 format
     * 
     */
    @Override
    public String convertTo(ExternalID externalID, Type<String> arg1) {
        WorkExternalIdentifier id = this.convertRC2toRC1(externalID);
        return JsonUtils.convertToJsonString(id);
    }
    
    /** Transforms RC1 into RC2
     * 
     * @param id
     * @return
     */
    protected ExternalID convertRC1toRC2(WorkExternalIdentifier id){
        ExternalID result = new ExternalID();        
        if (id.getWorkExternalIdentifierType() != null){
            result.setType(id.getWorkExternalIdentifierType().value());                
        }else{
            result.setType(WorkExternalIdentifierType.OTHER_ID.value());
        }
        if (id.getRelationship() !=null)
            result.setRelationship(org.orcid.jaxb.model.record_rc2.Relationship.fromValue(id.getRelationship().value()));
        if (id.getUrl() != null)
            result.setUrl(new org.orcid.jaxb.model.common_rc2.Url(id.getUrl().getValue()));
        if (id.getWorkExternalIdentifierId() !=null)
            result.setValue(id.getWorkExternalIdentifierId().getContent());
        else
            result.setValue("");
        return result;
    }
    
    protected WorkExternalIdentifier convertRC2toRC1(ExternalID externalID){
        WorkExternalIdentifier id = new WorkExternalIdentifier();
        try{
            id.setWorkExternalIdentifierType(WorkExternalIdentifierType.fromValue(externalID.getType()));            
        }catch(IllegalArgumentException e){
            id.setWorkExternalIdentifierType(WorkExternalIdentifierType.OTHER_ID); 
        }
        if (externalID.getValue() !=null)
            id.setWorkExternalIdentifierId(new WorkExternalIdentifierId(externalID.getValue()));
        else
            id.setWorkExternalIdentifierId(new WorkExternalIdentifierId(""));
        
        if (externalID.getUrl()!=null)
            id.setUrl(new Url(externalID.getUrl().getValue()));
        if (externalID.getRelationship() != null)
            try{
                id.setRelationship(Relationship.fromValue(externalID.getRelationship().value()));
            }catch (IllegalArgumentException e){
            }
        return id;
    }

}
