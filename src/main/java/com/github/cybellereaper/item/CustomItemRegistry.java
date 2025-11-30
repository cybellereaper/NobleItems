package com.github.cybellereaper.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CustomItemRegistry {
    private final Map<String, CustomItem> items = new HashMap<>();

    public Optional<CustomItem> get(String id) {
        return Optional.ofNullable(items.get(id.toLowerCase()));
    }

    public void replaceAll(Collection<CustomItem> definitions) {
        items.clear();
        definitions.forEach(item -> items.put(item.id().toLowerCase(), item));
    }

    public int size() {
        return items.size();
    }

    public Collection<CustomItem> values() {
        return items.values();
    }
}
