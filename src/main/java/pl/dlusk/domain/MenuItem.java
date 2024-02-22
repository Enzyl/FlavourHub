package pl.dlusk.domain;

import lombok.*;

import java.math.BigDecimal;
@With
@Value
@Builder
@EqualsAndHashCode(of = "menuItemId")
@ToString(of = {"menuItemId", "name", "category","price"})
public class MenuItem {
    Long menuItemId;
    String name;
    String description;
    String category;
    BigDecimal price;
    String imagePath;
    Menu menu;
}