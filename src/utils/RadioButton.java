package utils;

import assets.ColorAssets;
import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.objects.Clickable;
import edu.usu.graphics.objects.Text;
import edu.usu.graphics.objects.Triangle;
import org.joml.Vector3f;

import java.util.ArrayList;

/**
 * Simple radio button for single option selections.
 * */
public class RadioButton implements Clickable {
    private final Text buttonText;
    private final float textHeight;
    private final float textWidth;

    private boolean renderHover;
    private boolean renderSelect;

    public RadioButton(Text buttonText) {
        this.buttonText = buttonText;
        this.textHeight = buttonText.getHeight();
        this.textWidth = buttonText.getWidth();
    }

    public void setText(String text) {
        this.buttonText.setText(text);
    }

    public String getTextStr() {
        return this.buttonText.getTextStr();
    }

    public void hoverOver() {
        this.renderHover = true;
    }

    public void exitHover() {
        this.renderHover = false;
    }

    public void select(ArrayList<RadioButton> buttonSet) {
        this.renderHover = false;
        this.renderSelect = true;

        for (RadioButton button : buttonSet) {
            if (this == button)
                continue;
            button.deselect();
        }
    }

    public void deselect() {
        this.renderSelect = false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {

        return this.buttonText.isMouseOver(mouseX, mouseY);
    }

    private void renderCircle(Graphics2D graphics, Vector3f center, float radius, Color color, float HUD_z, boolean drawOutline) {
        ArrayList<Vector3f> circlePoints = new ArrayList<>();
        int numPoints = 15; //TODO: change this to be dependent on the radius
        for (int i = 0; i < numPoints; i++) {
            float angle = (float) (2.0 * Math.PI / numPoints * i);
            float nextX = center.x + radius * (float) Math.cos(angle);
            float nextY = center.y + radius * (float) Math.sin(angle);

            circlePoints.add(new Vector3f(nextX, nextY, HUD_z));
        }
        circlePoints.add(circlePoints.getFirst());

        // render the circle as a set of triangles
        for (int i = 0; i < circlePoints.size()-1; i++) {
            Vector3f v1 = circlePoints.get(i);
            Vector3f v2 = circlePoints.get(i+1);
            graphics.draw(new Triangle(center, v1, v2), color);
            if (drawOutline) graphics.draw(v1, v2, Color.BLACK);
        }
    }

    public void render(Graphics2D graphics, float HUD_z) {
        this.buttonText.draw(graphics);

        float buttonRadius = textHeight / 2 - 0.005f;
        float innerRadius = textHeight / 2 - 0.01f;
        Vector3f textCenter = buttonText.getCenter();
        Vector3f center = new Vector3f(textCenter.x-this.textWidth/2 - 0.04f, textCenter.y, HUD_z);

        renderCircle(graphics, center, buttonRadius, Color.WHITE, HUD_z, true);
        if (renderHover) {
            renderCircle(graphics, center, innerRadius, ColorAssets.simButtonTextColor2, HUD_z+0.01f, false);
        } else if (renderSelect) {
            renderCircle(graphics, center, innerRadius, ColorAssets.simButtonTextColor1, HUD_z+0.01f, false);
        }
    }
}
