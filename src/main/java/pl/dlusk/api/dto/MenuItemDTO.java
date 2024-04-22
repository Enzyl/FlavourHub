package pl.dlusk.api.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import pl.dlusk.domain.Menu;
import pl.dlusk.domain.MenuItem;

import java.math.BigDecimal;

@Data
public class MenuItemDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private MultipartFile image;

    public MenuItem convertDTOToMenuItem(MenuItemDTO dto, String imageUrl, Menu menu) {
        return MenuItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .imagePath(imageUrl)
                .menu(menu)
                .build();
    }
}
