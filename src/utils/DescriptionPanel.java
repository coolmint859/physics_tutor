package utils;

import edu.usu.graphics.Color;
import edu.usu.graphics.Font;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.Texture;
import edu.usu.graphics.objects.Rectangle;
import edu.usu.graphics.objects.Text;
import org.joml.Vector2f;

import java.util.ArrayList;

/** An Description panel, displaying any amount of text with a close button.*/
public class DescriptionPanel {
    private Texture bgTexture;
    private float margin;
    private float panelRenderOrder;
    private InfoPanel.TextAlignment alignment;
    private Vector2f center;
    private float textHeight;

    private String description;
    private Text closeButton;

    private final float panelWidth = 0.5f;

    public DescriptionPanel(Vector2f center, String description, float textHeight, Text closeButton, InfoPanel.TextAlignment alignment) {
        this.center = center;
        this.description = description;
        this.textHeight = textHeight;
        this.closeButton = closeButton;
        this.alignment = alignment;
    }

    public void setTexture(Texture bgTexture, float margin, float renderOrder) {
        this.bgTexture = bgTexture;
        this.margin = margin;
        this.panelRenderOrder = renderOrder;
    }

    public void setDescription(String newDesc) {
        if (newDesc == null)
            return;

        this.description = newDesc;
    }

    public String getDescription() {
        return this.description;
    }

    // used to fit the description text onto the display panel
    public ArrayList<String> splitDescription() {
        ArrayList<String> descStrings = new ArrayList<>();
        int descLength = this.description.length();
        int maxLength = (int) (panelWidth * descLength/4);
        int lastSplitIndex = 0;
        int currentSplitIndex = 0;

        for (int i = 0; i < descLength; i++) {
            char currentChar = this.description.charAt(i);
            if (currentChar == ' ' && currentSplitIndex - maxLength >= 0) {
                descStrings.add(this.description.substring(lastSplitIndex, i));
                lastSplitIndex = i+1;
                currentSplitIndex = 0;
                continue;
            }
            currentSplitIndex += 1;
        }
        if (lastSplitIndex <= descLength) {
            descStrings.add(this.description.substring(lastSplitIndex, descLength));
        }
        return descStrings;
    }

    protected float longestTextWidth(ArrayList<String> textList, float textHeight, Font font) {
        if (textList.isEmpty())
            return this.closeButton.getWidth();

        float longest = 0.0f;
        for (String text : textList) {
            if (text.isEmpty())
                continue;

            text = text.replace("\\", " ");

            float width = font.measureTextWidth(text, textHeight);
            if (width > longest)
                longest = width;
        }
        return longest;
    }

    protected void renderBackground(Graphics2D graphics, float textWidth, float textHeight) {
        if (bgTexture == null)
            return;

        float panelWidth = textWidth + 2*margin;
        float panelHeight = textHeight + 2*margin;
        float panelLeft = center.x - panelWidth / 2;
        float panelTop = center.y - panelHeight / 2;

        Rectangle panel = new Rectangle(panelLeft, panelTop, panelWidth, panelHeight, this.panelRenderOrder);
        graphics.draw(this.bgTexture, panel, Color.WHITE);
    }

    protected float calculateTextLeft(float textWidth, float maxWidth) {
        return switch (alignment) {
            case CENTERED -> center.x - textWidth / 2;
            case LEFT -> center.x - maxWidth/2;
            case RIGHT -> center.x + maxWidth/2 - textWidth;
        };
    }

    public void render(Graphics2D graphics, Font font, float TEXT_z) {
        ArrayList<String> textList = this.splitDescription();
        float longestTextWidth = longestTextWidth(textList, this.textHeight, this.closeButton.getFont());
        float totalTextHeight = this.textHeight * textList.size() + this.closeButton.getHeight();
        this.renderBackground(graphics, longestTextWidth, totalTextHeight);

        float textInitTop = -totalTextHeight/2;

        // draw description text
        for (int i = 0; i < textList.size(); i++) {
            float textWidth = font.measureTextWidth(textList.get(i), textHeight);
            float maxWidth = longestTextWidth(textList, this.textHeight, font);

            float textTop = textInitTop + i * textHeight;
            float textLeft = calculateTextLeft(textWidth, maxWidth);

            graphics.drawTextByHeight(font, textList.get(i), textLeft, textTop, textHeight, TEXT_z, Color.WHITE);
        }

        // draw close button
        float buttonCenterX = this.center.x;
        float buttonCenterY = textInitTop + this.textHeight * textList.size() + this.closeButton.getHeight()/2;
        Vector2f buttonCenter = new Vector2f(buttonCenterX, buttonCenterY);

        this.closeButton.draw(graphics, buttonCenter, TEXT_z);
    }
}
