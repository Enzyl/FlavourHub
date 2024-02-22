package pl.dlusk.infrastructure.database.repository.mapper;


import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.dlusk.domain.Owner;
import pl.dlusk.infrastructure.database.entity.OwnerEntity;

import static org.assertj.core.api.Assertions.assertThat;
public class OwnerEntityMapperTest {

    private final OwnerEntityMapper mapper = Mappers.getMapper(OwnerEntityMapper.class);

    @Test
    void shouldMapOwnerEntityToOwner() {
        // Given
        OwnerEntity entity = new OwnerEntity();
        entity.setId(1L);
        entity.setName("John");
        entity.setSurname("Doe");
        entity.setPhoneNumber("123456789");
        entity.setNip("1234567890");
        entity.setRegon("12345678901234");

        // When
        Owner owner = mapper.mapFromEntity(entity);

        // Then
        assertThat(owner).isNotNull();
        assertThat(owner.getOwnerId()).isEqualTo(entity.getId());
        assertThat(owner.getName()).isEqualTo(entity.getName());
        assertThat(owner.getSurname()).isEqualTo(entity.getSurname());
        assertThat(owner.getPhoneNumber()).isEqualTo(entity.getPhoneNumber());
        assertThat(owner.getNip()).isEqualTo(entity.getNip());
        assertThat(owner.getRegon()).isEqualTo(entity.getRegon());
        // The ignored property (user) should not be mapped and remain null
        assertThat(owner.getUser()).isNull();
    }

    @Test
    void shouldMapOwnerToOwnerEntity() {
        // Given
        Owner owner = Owner.builder()
                .ownerId(1L)
                .name("John")
                .surname("Doe")
                .phoneNumber("123456789")
                .nip("1234567890")
                .regon("12345678901234")
                .build();

        // When
        OwnerEntity entity = mapper.mapToEntity(owner);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(owner.getOwnerId());
        assertThat(entity.getName()).isEqualTo(owner.getName());
        assertThat(entity.getSurname()).isEqualTo(owner.getSurname());
        assertThat(entity.getPhoneNumber()).isEqualTo(owner.getPhoneNumber());
        assertThat(entity.getNip()).isEqualTo(owner.getNip());
        assertThat(entity.getRegon()).isEqualTo(owner.getRegon());
        // The ignored property (user) should not be mapped and remain null
        assertThat(entity.getUser()).isNull();
    }
}