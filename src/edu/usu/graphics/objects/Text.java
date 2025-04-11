package edu.usu.graphics.objects;

import edu.usu.graphics.Color;
import edu.usu.graphics.Font;
import edu.usu.graphics.Graphics2D;
import org.joml.Vector3f;

import java.util.ArrayList;

/** Wrapper class for a text element. Useful if the text should be interactable by the mouse. */
public class Text implements Clickable {
    private Vector3f center;
    private Font font;
    private String textStr;
    private Color color;

    private float left;
    private float top;
    private float height;
    private float width;

    public Text(Vector3f center, String textStr, Font font, float textHeight, Color color) {
        this.center = center;
        this.font = font;
        this.textStr = textStr;
        this.color = color;

        this.height = textHeight;
        resetParams();
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setColor(Color newColor) {
        this.color = newColor;
    }

    public void setText(String newTextStr) {
        this.textStr = newTextStr;
        resetParams();
    }

    public void setCenter(Vector3f newCenter) {
        this.center = new Vector3f(newCenter.x, newCenter.y, newCenter.z);
    }

    public String getTextStr() {
        return this.textStr;
    }

    public Vector3f getCenter() {
        return new Vector3f(center.x, center.y, center.z);
    }

    public static ArrayList<String> getTextStrings(ArrayList<Text> textObjects) {
        ArrayList<String> strings = new ArrayList<>();
        for (Text textObject : textObjects) {
            strings.add(textObject.getTextStr());
        }
        return strings;
    }

    private void resetParams() {
        this.width = font.measureTextWidth(textStr, height);
        this.left = center.x - width / 2;
        this.top = center.y - height / 2;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean betweenX = mouseX >= left && mouseX <= left + width;
        boolean betweenY = mouseY >= top && mouseY <= top + height;

        return betweenX && betweenY;
    }

    public void draw(Graphics2D graphics) {
        graphics.drawTextByHeight(font, textStr, left, top, height, center.z, color);
    }
}
