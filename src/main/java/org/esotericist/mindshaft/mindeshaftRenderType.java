package org.esotericist.mindshaft;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import net.minecraft.client.renderer.RenderState.TransparencyState;

public class mindeshaftRenderType extends RenderState {

    public mindeshaftRenderType(String string, Runnable r, Runnable r1) {
        super(string, r, r1);
    }

    public static RenderType simple() {
        RenderType.State renderTypeState = RenderType.State.builder().setTransparencyState(TransparencyState.TRANSLUCENT_TRANSPARENCY).createCompositeState(true);
        return RenderType.create("", DefaultVertexFormats.POSITION, 0, 0, false, false, renderTypeState);
    }

}
