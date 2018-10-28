package org.springframework.data.jpa.benchmark.model;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Diego on 2018. 10. 22..
 */
public class RandomTagHelper {

    private RandomTagHelper() {}

    private static Random rnd = new Random(System.currentTimeMillis());

    public static TagType randomTagType() {
        TagType tagType = new TagType();
        tagType.setTagTypeClass("TagClass#" + rnd.nextInt(100));
        tagType.setTagTypeName("TagType#" + rnd.nextInt(1000));
        tagType.setCreatedAt(new Date());
        tagType.setModifiedAt(new Date());

        return tagType;
    }

    public static TagValue randomTagValue(TagType tagType) {
        TagValue tagValue = new TagValue();
        tagValue.setRep("tagRep#" + rnd.nextInt(1000));
        tagValue.setSynonyms("tagValueSynonyms...");
        if (tagType != null) {
            tagValue.setTagType(tagType);
            tagType.addTagValue(tagValue);
        }
        tagValue.setCreatedAt(new Date());
        tagValue.setModifiedAt(new Date());

        return tagValue;
    }

    public static List<TagValue> randomTagValues(TagType tagType, int count) {
        return IntStream.range(0, count)
            .mapToObj(it -> randomTagValue(tagType))
            .collect(Collectors.toList());
    }

    public static List<TagType> randomTags(int count) {
        return IntStream.range(0, count)
            .mapToObj(it -> {
                TagType tagType = randomTagType();
                randomTagValues(tagType, 3);
                return tagType;
            })
            .collect(Collectors.toList());
    }
}
