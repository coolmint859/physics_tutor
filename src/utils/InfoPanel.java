package utils;

import edu.usu.graphics.*;
import edu.usu.graphics.objects.Rectangle;
import org.joml.Vector2f;

import java.util.ArrayList;

/**
 * displays a series text with an optional texture background. Can be aligned.
 * */
public class InfoPanel {
    public enum TextAlignment {
        CENTERED,
        LEFT,
        RIGHT
    }

    protected float margin;
    protected final float padding;
    protected final float textHeight;
    protected ArrayList<String> textList;
    protected TextAlignment alignment;

    protected final Color textColor;

    protected Texture texture;
    protected Color bgColor;

    protected Vector2f center;

    public InfoPanel(Vector2f center, ArrayList<String> text, TextAlignment alignment, Color textColor, float textHeight, float padding) {
        this.alignment = alignment;
        this.textHeight = textHeight;
        this.textList = text;
        this.padding = padding;
        this.center = center;

        this.textColor = textColor;
        this.texture = null;
    }

    public void setBackgroundTexture(Texture bgTexture, Color bgColor, float margin) {
        this.texture = bgTexture;
        this.bgColor = bgColor;
        this.margin = margin;
    }

    public void setBackgroundTexture(Texture bgTexture, float margin) {
        this.texture = bgTexture;
        this.bgColor = Color.WHITE;
        this.margin = margin;
    }

    public void updateText(ArrayList<String> newText) {
        this.textList = newText;
    }

    public void setTextAt(int index, String text) {
        this.textList.set(index, text);
    }

    public void render(Graphics2D graphics, Font font, float HUD_z, float TEXT_z) {
        this.renderBackground(graphics, "", font, HUD_z);

        float textInitTop = center.y - (this.textHeight * this.textList.size()) / 2;
        for (int i = 0; i < textList.size(); i++) {
            float textWidth = font.measureTextWidth(textList.get(i), textHeight);
            float maxWidth = longestTextWidth(this.textList, this.textHeight, font);

            float pad = i == 0 ? 0.0f : padding;
            float textTop = textInitTop + i * textHeight + pad;
            float textLeft = calculateTextLeft(textWidth, maxWidth);

            graphics.drawTextByHeight(font, textList.get(i), textLeft, textTop, textHeight, TEXT_z, textColor);
        }
    }

    protected float longestTextWidth(ArrayList<String> textList, float textHeight, Font font) {
        float longest = 0.0f;
        for (String text : textList) {
            float width = font.measureTextWidth(text, textHeight);
            if (width > longest)
                longest = width;
        }
        return longest;
    }

    protected void renderBackground(Graphics2D graphics, String titleText, Font font, float HUD_z) {
        if (texture == null)
            return;

        ArrayList<String> displayText = new ArrayList<>();
        if (!titleText.isEmpty()) {
            displayText.add(titleText);
        }
        displayText.addAll(this.textList);

        float panelWidth = longestTextWidth(displayText, this.textHeight, font) + 2*margin;
        float panelHeight = this.textHeight * displayText.size() + 2*margin;
        float panelLeft = center.x - panelWidth / 2;
        float panelTop = center.y - panelHeight / 2;

        Rectangle panel = new Rectangle(panelLeft, panelTop, panelWidth, panelHeight, HUD_z);
        graphics.draw(texture, panel, bgColor);
    }

    protected float calculateTextLeft(float textWidth, float maxWidth) {
        return switch (alignment) {
            case CENTERED -> center.x - textWidth / 2;
            case LEFT -> center.x - maxWidth/2;
            case RIGHT -> center.x + maxWidth/2 - textWidth;
        };
    }
}