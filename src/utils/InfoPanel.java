package utils;

import edu.usu.graphics.*;
import edu.usu.graphics.objects.Rectangle;
import org.joml.Vector2f;

import java.util.ArrayList;

/**
 * displays a series of highlight-able text, center aligned, with an optional texture background.
 * */
public class InfoPanel {
    private float margin;
    private float padding;
    private float textHeight;
    private ArrayList<String> textList;

    private Color highLightColor;
    private Color textColor;

    private Texture texture;

    private Vector2f center;

    public InfoPanel(ArrayList<String> text, Color textColor, Vector2f center, float margin, float padding, float textHeight) {
        this.margin = margin;
        this.padding = padding;
        this.textHeight = textHeight;
        this.textList = text;
        this.center = center;

        this.textColor = textColor;
        this.texture = null;
    }

    public InfoPanel(ArrayList<String> text, Color textColor, Vector2f center, float margin, float padding, float textHeight, Texture texture) {
        this(text, textColor, center, margin, padding, textHeight);
        this.texture = texture;
    }

    public void setHighLightColor(Color highLightColor) {
        this.highLightColor = highLightColor;
    }

    public void updateText(ArrayList<String> newText) {
        this.textList = newText;
    }

    private float longestTextWidth(ArrayList<String> textList, float textHeight, Font font) {
        float longest = 0.0f;
        for (String text : textList) {
            float width = font.measureTextWidth(text, textHeight);
            if (width > longest)
                longest = width;
        }
        return longest;
    }

    public void render(Graphics2D graphics, Font font, float HUD_z, float TEXT_z) {
        this.render(graphics, font, HUD_z, TEXT_z, null);
    }

    public void render(Graphics2D graphics, Font font, float HUD_z, float TEXT_z, String highLightText) {
        float panelWidth = longestTextWidth(this.textList, this.textHeight, font) + 2*margin;
        float panelHeight = this.textHeight * this.textList.size() + 2*margin;
        float panelLeft = center.x - panelWidth / 2;
        float panelTop = center.y - panelHeight / 2;

        Rectangle panel = new Rectangle(panelLeft, panelTop, panelWidth, panelHeight, HUD_z);
        if (texture != null)
            graphics.draw(texture, panel, Color.WHITE);
        else
            graphics.draw(panel, Color.WHITE);

        float textInitTop = center.y - (this.textHeight * this.textList.size()) / 2;
        for (int i = 0; i < textList.size(); i++) {
            float textWidth = font.measureTextWidth(textList.get(i), textHeight);

            Color color;
            if (this.highLightColor != null)
                color = textList.get(i).equals(highLightText) ? this.highLightColor : this.textColor;
            else
                color = textColor;

            float pad = i == 0 ? 0.0f : padding;
            float textTop = textInitTop + i * textHeight + pad;
            float textLeft = center.x - textWidth / 2;

            graphics.drawTextByHeight(font, textList.get(i), textLeft, textTop, textHeight, TEXT_z, color);
        }
    }
}