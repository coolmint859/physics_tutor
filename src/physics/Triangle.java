package physics;

import edu.usu.graphics.Color;
import org.jbox2d.dynamics.BodyType;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Triangle extends Polygon {
    public Triangle(PhysicsWorld world, Vector3f v1, Vector3f v2, Vector3f v3, Color color, BodyType type, boolean showLine) {
        super(world, new ArrayList<>(List.of(new Vector3f[]{v1, v2, v3})), color, type, showLine);
    }
}
