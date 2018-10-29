package org.springframework.data.requery.benchmark;

import io.requery.sql.EntityDataStore;
import org.junit.Test;
import org.springframework.data.requery.benchmark.model.TagType;
import org.springframework.data.requery.benchmark.model.TagValue;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.requery.benchmark.model.RandomTagHelper.*;

/**
 * @author Diego on 2018. 10. 21..
 */
public class RequerySetupUtilsTest {

    @Test
    public void loadEntityModel() {
        assertNotNull(RequerySetupUtils.getDataSource());
        assertNotNull(RequerySetupUtils.getConfiguration().getModel());
    }

    @Test
    public void contextLoading() {
        assertNotNull(RequerySetupUtils.getDataStore());
    }

    @Test
    public void oneToManyTest() {
        EntityDataStore<Object> ds = RequerySetupUtils.getDataStore();
        TagType tagType = randomTagType();
        TagValue tagValue = randomTagValue(tagType);

        ds.insert(tagType);

        TagType retrieved = ds.findByKey(TagType.class, tagType.getTagTypeId());
        assertEquals(tagType.getTagTypeId(), retrieved.getTagTypeId());
        assertNotNull(tagValue.getTagValueId());
        assertEquals(retrieved.getTagValues().size(), 1);
    }
}