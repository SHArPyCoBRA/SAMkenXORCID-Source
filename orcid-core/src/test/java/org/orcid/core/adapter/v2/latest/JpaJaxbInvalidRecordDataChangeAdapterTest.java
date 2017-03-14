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
package org.orcid.core.adapter.v2.latest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.orcid.core.adapter.JpaJaxbInvalidRecordDataChangeAdapter;
import org.orcid.model.record_correction.RecordCorrection;
import org.orcid.persistence.jpa.entities.InvalidRecordDataChangeEntity;
import org.orcid.test.OrcidJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OrcidJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:orcid-core-context.xml" })
public class JpaJaxbInvalidRecordDataChangeAdapterTest {

    @Resource
    private JpaJaxbInvalidRecordDataChangeAdapter adapter;
    
    private Date date = new Date();
    
    @Test
    public void fromEntityTest() {
        InvalidRecordDataChangeEntity entity = getEntity();
        RecordCorrection element = adapter.toInvalidRecordDataChange(entity);
        assertNotNull(element);
        assertEquals(element.getSequence(), entity.getId());
        assertEquals(element.getDateCreated(), entity.getDateCreated());
        assertEquals(element.getDescription(), entity.getDescription());
        assertEquals(element.getLastModified(), entity.getLastModified());
        assertEquals(element.getNumChanged(), entity.getNumChanged());
        assertEquals(element.getSqlUsedToUpdate(), entity.getSqlUsedToUpdate());
        assertEquals(element.getType(), entity.getType());
    }
    
    private InvalidRecordDataChangeEntity getEntity() {
        InvalidRecordDataChangeEntity entity = new InvalidRecordDataChangeEntity();
        entity.setDateCreated(date);
        entity.setDescription("description");
        entity.setId(1234L);
        entity.setLastModified(date);
        entity.setNumChanged(24816L);
        entity.setSqlUsedToUpdate("update table set data = 'value' where key = key");
        entity.setType("type");
        return entity;
    }
}
