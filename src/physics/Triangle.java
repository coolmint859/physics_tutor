package physics;

import edu.usu.graphics.Color;
import org.jbox2d.dynamics.BodyType;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Triangle extends Polygon {
    public Triangle(PhysicsWorld world, Vector2f v1, Vector2f v2, Vector2f v3, Color color, float renderOrder, float initRotation, Vector2f initVelocity, BodyType type, float mass, float friction, float restitution) {
        super(world, new ArrayList<>(List.of(new Vector2f[]{v1, v2, v3})), color, renderOrder, initRotation, initVelocity, type, mass, friction, restitution);
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("\nObject 'Triangle':");
        str.append(String.format("\n\tType: '%s'", this.getBody().m_type));
        str.append(String.format("\n\tMass: %f kg", this.getBody().m_mass));
        str.append(String.format("\n\tFriction: %f", this.friction));
        str.append(String.format("\n\tRotation: %f rad", this.angle));
        str.append(String.format("\n\tRestitution: %f", this.restitution));
        str.append(String.format("\n\tVelocity: %s m/s", this.getBody().getLinearVelocity()));
        str.append(String.format("\n\tCentroid: %s cu", this.getCenterCanvas()));
        return str.toString();
    }
}
