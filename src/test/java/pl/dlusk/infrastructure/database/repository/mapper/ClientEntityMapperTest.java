package pl.dlusk.infrastructure.database.repository.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.dlusk.domain.Client;
import pl.dlusk.infrastructure.database.entity.ClientEntity;
import static org.assertj.core.api.Assertions.assertThat;

public class ClientEntityMapperTest {
    private ClientEntityMapper mapper = Mappers.getMapper(ClientEntityMapper.class);

    @Test
    void shouldMapClientEntityToClient() {
        // Arrange
        ClientEntity entity = new ClientEntity();
        entity.setId(1L);
        entity.setFullName("John Doe");
        entity.setPhoneNumber("123456789");

        // Act
        Client client = mapper.mapFromEntity(entity);

        // Assert
        assertThat(client).isNotNull();
        assertThat(client.getClientId()).isEqualTo(entity.getId());
        assertThat(client.getFullName()).isEqualTo(entity.getFullName());
        assertThat(client.getPhoneNumber()).isEqualTo(entity.getPhoneNumber());
        assertThat(client.getUser()).isNull(); // Sprawdzenie ignorowania pola
        assertThat(client.getFoodOrders()).isNull(); // Sprawdzenie ignorowania pola
    }

    @Test
    void shouldMapClientToClientEntity() {
        // Arrange
        Client client = Client.builder()
                .clientId(1L)
                .fullName("John Doe")
                .phoneNumber("123456789")
                .build();

        // Act
        ClientEntity entity = mapper.mapToEntity(client);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(client.getClientId());
        assertThat(entity.getFullName()).isEqualTo(client.getFullName());
        assertThat(entity.getPhoneNumber()).isEqualTo(client.getPhoneNumber());
        // Nie można sprawdzić ignorowanych pól, ponieważ mapowanie odbywa się w jedną stronę
    }
}
