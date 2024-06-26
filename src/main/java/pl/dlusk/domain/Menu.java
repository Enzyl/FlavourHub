package pl.dlusk.domain;

import lombok.*;

import java.util.Set;
@With
@Value
@Builder
@EqualsAndHashCode(of = {"menuId", "name", "description"})
@ToString(of = {"menuId", "name", "description", "menuItems"})
public class Menu {
    Long menuId;
    String name;
    String description;
    Restaurant restaurant;
    Set<MenuItem> menuItems;
}
