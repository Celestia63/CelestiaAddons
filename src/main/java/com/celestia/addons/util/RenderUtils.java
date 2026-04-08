package com.celestia.addons.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

public class RenderUtils {

    public static void fill(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y2, color);
    }

    public static void drawBorderedRect(DrawContext context, int x, int y, int width, int height, int borderWidth,
            int color, int borderColor) {
        fill(context, x, y, x + width, y + height, color);
        fill(context, x, y, x + width, y + borderWidth, borderColor); // Top
        fill(context, x, y + height - borderWidth, x + width, y + height, borderColor); // Bottom
        fill(context, x, y, x + borderWidth, y + height, borderColor); // Left
        fill(context, x + width - borderWidth, y, x + width, y + height, borderColor); // Right
    }

    // Scissor implementation using DrawContext
    public static void enableScissor(DrawContext context, int x, int y, int width, int height) {
        context.enableScissor(x, y, x + width, y + height);
    }

    public static void disableScissor(DrawContext context) {
        context.disableScissor();
    }

    /**
     * Draw a 3D box in world space without deprecated MatrixStack
     * Uses the world render context's matrices directly
     */
    public static void draw3DBox(Matrix4f positionMatrix, VertexConsumer consumer, 
            double x, double y, double z, float width, float height, int color) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = (float) (color >> 24 & 255) / 255.0F;
        if (a == 0) a = 1.0f;

        double w = width / 2.0;
        double h = height;

        // Draw box lines
        // Bottom face (X-Z plane at y=0)
        drawLine(consumer, positionMatrix, x - w, y, z - w, x + w, y, z - w, r, g, b, a);
        drawLine(consumer, positionMatrix, x + w, y, z - w, x + w, y, z + w, r, g, b, a);
        drawLine(consumer, positionMatrix, x + w, y, z + w, x - w, y, z + w, r, g, b, a);
        drawLine(consumer, positionMatrix, x - w, y, z + w, x - w, y, z - w, r, g, b, a);

        // Top face (X-Z plane at y=height)
        drawLine(consumer, positionMatrix, x - w, y + h, z - w, x + w, y + h, z - w, r, g, b, a);
        drawLine(consumer, positionMatrix, x + w, y + h, z - w, x + w, y + h, z + w, r, g, b, a);
        drawLine(consumer, positionMatrix, x + w, y + h, z + w, x - w, y + h, z + w, r, g, b, a);
        drawLine(consumer, positionMatrix, x - w, y + h, z + w, x - w, y + h, z - w, r, g, b, a);

        // Vertical edges
        drawLine(consumer, positionMatrix, x - w, y, z - w, x - w, y + h, z - w, r, g, b, a);
        drawLine(consumer, positionMatrix, x + w, y, z - w, x + w, y + h, z - w, r, g, b, a);
        drawLine(consumer, positionMatrix, x + w, y, z + w, x + w, y + h, z + w, r, g, b, a);
        drawLine(consumer, positionMatrix, x - w, y, z + w, x - w, y + h, z + w, r, g, b, a);
    }

    public static void draw3DLine(Matrix4f positionMatrix, VertexConsumer consumer,
            double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = (float) (color >> 24 & 255) / 255.0F;
        if (a == 0) a = 1.0f;
        drawLine(consumer, positionMatrix, x1, y1, z1, x2, y2, z2, r, g, b, a);
    }

    private static void drawLine(VertexConsumer consumer, Matrix4f positionMatrix,
            double x1, double y1, double z1, double x2, double y2, double z2, 
            float r, float g, float b, float a) {
        consumer.vertex(positionMatrix, (float) x1, (float) y1, (float) z1)
                .color(r, g, b, a);
        consumer.vertex(positionMatrix, (float) x2, (float) y2, (float) z2)
                .color(r, g, b, a);
    }
}
