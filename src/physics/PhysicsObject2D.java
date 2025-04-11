package physics;

import edu.usu.graphics.Graphics2D;
import org.jbox2d.dynamics.Body;
import org.joml.Vector2f;

/** represents a generic game object to be used by JBox2D and the graphics engine */
public interface PhysicsObject2D {
    Body getBody();
    Vector2f getCenter();
    void update(double elapsedTime);
    void render(Graphics2D graphics, double elapsedTime);
}
