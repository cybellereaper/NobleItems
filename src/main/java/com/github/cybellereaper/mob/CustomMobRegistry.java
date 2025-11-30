package com.github.cybellereaper.mob;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CustomMobRegistry {
    private final Map<String, CustomMob> mobs = new HashMap<>();

    public Optional<CustomMob> get(String id) {
        return Optional.ofNullable(mobs.get(id.toLowerCase()));
    }

    public void replaceAll(Collection<CustomMob> definitions) {
        mobs.clear();
        definitions.forEach(mob -> mobs.put(mob.id().toLowerCase(), mob));
    }

    public int size() {
        return mobs.size();
    }

    public Collection<CustomMob> values() {
        return mobs.values();
    }
}
