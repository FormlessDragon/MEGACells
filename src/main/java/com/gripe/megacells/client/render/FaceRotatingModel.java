package com.gripe.megacells.client.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import ae2.api.orientation.BlockOrientation;
import ae2.client.render.DelegateBakedModel;

public class FaceRotatingModel extends DelegateBakedModel {
    private final BlockOrientation orientation;

    public FaceRotatingModel(IBakedModel base, BlockOrientation orientation) {
        super(base);
        this.orientation = orientation;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        EnumFacing rotatedSide = side == null ? null : orientation.resultingRotate(side);
        List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, rotatedSide, rand));

        for (int i = 0; i < quads.size(); i++) {
            BakedQuad quad = quads.get(i);
            quads.set(
                    i,
                    new BakedQuad(
                            quad.getVertexData(),
                            quad.getTintIndex(),
                            orientation.rotate(quad.getFace()),
                            quad.getSprite(),
                            quad.shouldApplyDiffuseLighting(),
                            quad.getFormat()));
        }

        return quads;
    }
}
