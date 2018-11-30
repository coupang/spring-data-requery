package org.springframework.data.requery.domain.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.requery.domain.AbstractDomainTest;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DB에 저장된 엔티티를 JSON 으로 서비스하고, 변경된 정보를 JSON으로 받아 DB에 Update 하는 시나리오 테스트
 *
 * @author debop (Sunghyouk Bae)
 */
@Slf4j
public class DtoConversionTest extends AbstractDomainTest {

    protected ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        //requeryOperations.deleteAll(VendorItem.class);
        requeryOperations.deleteAll(Vendor.class);
    }

    @Test
    public void convert_to_dto_simple_entity() throws Exception {

        Vendor vendor = createVendor("vendor1");
        requeryOperations.insert(vendor);

        String json = mapper.writeValueAsString(vendor);
        log.debug("vendor={}", json);

        String updatedJson = json.replace("vendor1", "newVendorName");

        Vendor updatedVendor = mapper.readValue(updatedJson, Vendor.class);
        assertThat(updatedVendor).isNotNull();
        assertThat(updatedVendor.getId()).isEqualTo(vendor.getId());
        assertThat(updatedVendor.getName()).isEqualTo("newVendorName");

        // FIXME: Json deserized object는 requery entity로 보지 않아 upsert를 못한다 
        // requeryOperations.upsert(updatedVendor);

        // 명확히 저장된 값이라고 알고 있다면 update 를 호출한다
        // 안전을 위해 존재여부를 확인한 후 저장할 경우에는 existsBy 를 호출하도록 한다

        boolean exists = requeryOperations.existsBy(Vendor.class, Vendor.ID.eq(updatedVendor.getId()));
        if (exists) {
            updatedVendor = requeryOperations.update(updatedVendor);
        } else {
            requeryOperations.insert(updatedVendor);
        }

        int vendorCount = requeryOperations.count(Vendor.class).get().value();
        assertThat(vendorCount).isEqualTo(1);
    }

    @Test
    public void convert_to_dto_one_to_many_entity() throws Exception {
        Vendor vendor = createVendor("vendor1");
        VendorItem item1 = createVendorItem("item1", BigDecimal.valueOf(123.4));
        VendorItem item2 = createVendorItem("item2", BigDecimal.valueOf(567.8));

        vendor.getItems().add(item1);
        vendor.getItems().add(item2);

        requeryOperations.insert(vendor);

        String json = mapper.writeValueAsString(vendor);

        String updatedJson = json.replace("vendor1", "updatedVendorName");
        updatedJson = updatedJson.replace("item1", "vendorItem1");
        updatedJson = updatedJson.replace("567.8", "999.9");

        log.debug("Orignal={}", json);
        log.debug("Updated={}", updatedJson);

        Vendor updated = mapper.readValue(updatedJson, Vendor.class);

        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(vendor.getId());
        assertThat(updated.getItems()).hasSize(2);

        // NOTE: 외부에서 온 relation은 관계 정보에 해당하는 Proxy가 설정되어 있지 않기 때문에 cascading이 적용되지 않는다.
        // NOTE: Details 도 따로 저장해줘야 한다.
        requeryOperations.updateAll(updated.getItems());
        requeryOperations.update(updated);

        int vendorCount = requeryOperations.count(Vendor.class).get().value();
        assertThat(vendorCount).isEqualTo(1);
    }

    private Vendor createVendor(String name) {
        Vendor vendor = new Vendor();
        vendor.setName(name);
        vendor.setRegisteredAt(new Date());

        return vendor;
    }

    private VendorItem createVendorItem(String name, BigDecimal price) {
        VendorItem item = new VendorItem();
        item.setName(name);
        item.setPrice(price);

        return item;
    }
}
