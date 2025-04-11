package assets;

import edu.usu.graphics.Texture;
import org.joml.Vector2f;

public class ImageAssets {
    // images
    public static final Texture panelBackgroundImg = new Texture("./resources/images/simplebg.png");

    // converts pixel coordinates to world coordinates. The pixel coordinates are assumed to be measured from
    // the right and bottom of an image with a resolution that matches the size of the window.
    public Vector2f pixelCoordsToWorld(Vector2f pixels, int windowHeight, int windowWidth) {
        float worldX = this.translateLeftPixels((int) pixels.x, windowWidth);
        float worldY = this.translateTopPixels((int) pixels.y, windowHeight, windowWidth);
        return new Vector2f(worldX, worldY);
    }

    // converts the left parameter in a rectangle object from pixels (as measured from the right window border)
    // into the corresponding left position in the canvas
    public float translateLeftPixels(int pixLeft, int windowWidth) {
        return (float)(1.0-((2.0 * pixLeft) / windowWidth));
    }

    // converts the top parameter in a rectangle object from pixels (as measured from the bottom window border)
    // into the corresponding top position in the canvas
    public float translateTopPixels(int pixTop, int windowHeight, int windowWidth) {
        return (float)((windowHeight - 2.0 * pixTop) / windowWidth);
    }

    // converts the width parameter in a rectangle object from pixels into the corresponding width value in the canvas
    public float translateWidthPixels(int pixWidth, int windowWidth) {
        return (float)((2.0 * pixWidth) / windowWidth);
    }

    // converts the width parameter in a rectangle object from pixels into the corresponding width value in the canvas
    public float translateHeightPixels(int pixHeight, int windowWidth) {
        return (float)((2.0 * pixHeight) / windowWidth);
    }
}
