package com.gripe.megacells.client.render;

/**
 * Marker for the MEGA crafting-unit client model path.
 *
 * <p>AE2 Supergiant 1.12 uses package-private crafting baked models and a
 * Forge {@code IModel} loader path instead of the 1.21 model-provider API.
 * The real model hook will be reintroduced with the block model loader after
 * the crafting block registration is stable.
 */
public final class MEGACraftingUnitModelProvider {
    private MEGACraftingUnitModelProvider() {}
}
