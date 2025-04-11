/*
Copyright (c) 2024 James Dean Mathias

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package edu.usu.graphics.objects;

import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.Texture;
import org.joml.Vector2f;

public class AnimatedSprite implements Clickable {

    private final Texture spriteSheet;
    private final float[] spriteTime;

    private double animationTime = 0;
    private int subImageIndex = 0;
    private final int subImageWidth;

    private final Vector2f size;
    protected Vector2f position;
    protected float rotation = 0;

    public AnimatedSprite(Texture spriteSheet, float[] spriteTime, Vector2f size, Vector2f position) {
        this.spriteSheet = spriteSheet;
        this.spriteTime = spriteTime;

        this.subImageWidth = spriteSheet.getWidth() / spriteTime.length;

        this.size = size;
        this.position = position;
    }

    public void update(double elapsedTime) {
        animationTime += elapsedTime;
        if (animationTime >= spriteTime[subImageIndex]) {
            animationTime -= spriteTime[subImageIndex];
            subImageIndex++;
            subImageIndex = subImageIndex % spriteTime.length;
        }
    }

    public void setPosition(Vector2f center) {
        this.position = new Vector2f(center.x, center.y);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean betweenX = mouseX >= position.x && mouseX <= position.x + size.x;
        boolean betweenY = mouseX >= position.y && mouseX <= position.y + size.y;

        return betweenX && betweenY;
    }

    public void draw(Graphics2D graphics, Color color, float renderOrder) {
        // Where to draw
        Rectangle destination = new Rectangle(position.x, position.y, size.x, size.y);

        float centerX = position.x + size.x / 2;
        float centerY = position.y - size.y / 2;
        Vector2f center = new Vector2f(centerX, centerY);
        // Which sub-rectangle of the sprite-sheet to draw
        Rectangle subImage = new Rectangle(
                subImageWidth * subImageIndex,
                0,
                subImageWidth,
                spriteSheet.getHeight(),
                renderOrder);

        graphics.draw(spriteSheet, destination, subImage, rotation, center, color);
    }
}
