package com.gripe.megacells.definition;

import ae2.core.localization.LocalizationEnum;

import com.gripe.megacells.MEGACells;

public enum MEGATranslations implements LocalizationEnum {
    ALot,
    Compression,
    Cutoff,
    Contains,
    ContainsTraceUnits,
    Disabled,
    Empty,
    Enabled,
    MismatchedFilter,
    ModName("gui"),
    PartitionedFor,
    Quantity,
    NotPartitioned,
    TraceUnits;

    private final String key;

    MEGATranslations(String root) {
        this.key = String.format("%s.%s.%s", root, MEGACells.MODID, name());
    }

    MEGATranslations() {
        this("gui.tooltips");
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
