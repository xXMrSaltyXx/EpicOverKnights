package com.saltycodes.epicoverknights.items;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Defines the weapon types for which blade items exist.
 * Add a new entry here to automatically register blades,
 * models, creative tab entries, and recipe removals for all materials.
 *
 * To restrict a blade type to specific materials, pass them as varargs:
 *   MYTYPE("mytype", BladeMaterial.STEEL)
 * Omit the varargs to allow all materials (default).
 */
public enum BladeType {
    STYLET("stylet"),
    SHORTSWORD("shortsword"),
    KATZBALGER("katzbalger"),
    PIKE("pike"),
    AHLSPIESS("ahlspiess"),
    BASTARDSWORD("bastardsword"),
    ESTOC("estoc"),
    CLAYMORE("claymore"),
    ZWEIHANDER("zweihander"),
    LOCHABERAXE("lochaberaxe"),
    CONCAVEHALBERD("concavehalberd"),
    HEAVYMACE("heavymace"),
    HEAVYWARHAMMER("heavywarhammer"),
    LUCERNHAMMER("lucernhammer"),
    MORGENSTERN("morgenstern"),
    GUISARME("guisarme"),
    BLACKSMITH_HAMMER("blacksmith_hammer", BladeMaterial.STEEL);

    private final String name;
    private final Set<BladeMaterial> materials;

    BladeType(String name, BladeMaterial... allowedMaterials) {
        this.name = name;
        if (allowedMaterials.length == 0) {
            this.materials = Collections.unmodifiableSet(EnumSet.allOf(BladeMaterial.class));
        } else {
            this.materials = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(allowedMaterials)));
        }
    }

    public String getName() {
        return name;
    }

    public Set<BladeMaterial> getMaterials() {
        return materials;
    }
}
