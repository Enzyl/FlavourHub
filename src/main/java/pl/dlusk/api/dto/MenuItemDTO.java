package pl.dlusk.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import pl.dlusk.domain.Menu;
import pl.dlusk.domain.MenuItem;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private MultipartFile image;

}
