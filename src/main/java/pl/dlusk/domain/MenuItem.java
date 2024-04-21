package pl.dlusk.domain;

import lombok.*;

import java.math.BigDecimal;
@With
@Value
@Builder
@EqualsAndHashCode(of = {"menuItemId", "name","description","category","price","imagePath"})
@ToString(of = {"menuItemId", "name", "category","description","price","imagePath"})
public class MenuItem {
    Long menuItemId;
    String name;
    String description;
    String category;
    BigDecimal price;
    String imagePath;
    Menu menu;
}