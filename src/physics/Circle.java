package physics;

import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;

import edu.usu.graphics.objects.Triangle;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

/** Used to render circles to the canvas, as well as do physics in the given physics world */
public class Circle implements PhysicsObject2D {
    private final PhysicsWorld world;
    private Body body;

    private Vector3f center;
    public float renderOrder;
    public Color color;
    public float radius;

    public float angle;
    public float friction;
    public float restitution;

    public Circle(PhysicsWorld world, Color color, Vector2f center, float radius, float renderOrder, float initRotation, Vector2f initVelocity, BodyType type, float density, float friction, float restitution) {
        this.world = world;
        this.color = color;
        this.center = new Vector3f(center.x, center.y, renderOrder);
        this.radius = radius;
        this.renderOrder = renderOrder;

        this.friction = friction;
        this.restitution = restitution;

        this.createPhysicsObject(center, radius, type, density, initRotation, initVelocity);
    }

    /** Creates a body object that the physics library can use to do physics with */
    private void createPhysicsObject(Vector2f center, float radius, BodyType type, float density, float initRotation, Vector2f initVelocity) {
        // create body definition;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(new Vec2(center.x, center.y));
        bodyDef.angle = initRotation;
        bodyDef.linearVelocity = new Vec2(initVelocity.x, initVelocity.y);

        // create body and place in the world
        this.body = world.addAndCreateBody(bodyDef);
        this.angle = this.body.getAngle();

        // create shape
        CircleShape circle = new CircleShape();
        circle.m_radius = world.scalarCanvasToWorld(radius);

        // create fixture
        FixtureDef fx = new FixtureDef();
        fx.shape =circle;
        fx.density = density;
        fx.friction = friction;
        fx.restitution = restitution;

        // attach fixture to body
        body.createFixture(fx);
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    @Override
    public Vector2f getCenter() {
        return new Vector2f(this.center.x, this.center.y);
    }

    public Vector2f getCenterCanvas() {
        return new Vector2f(this.world.worldCoordsToCanvas(new Vec2(center.x, center.y)));
    }

    /** update the position of the points that represent the circle*/
    @Override
    public void update(double elapsedTime) {
        // retrieve the center and angle of the body from the world
        Vec2 bodyCenter = this.body.getWorldCenter();
        this.center = new Vector3f(bodyCenter.x, bodyCenter.y, this.renderOrder);
        this.angle = this.body.getAngle();
    }

    /** Creates a series of points that the graphics library can use to render a circle */
    private ArrayList<Vector3f> createCirclePoints(Vector3f center, float radius) {
        ArrayList<Vector3f> circlePoints = new ArrayList<>();
        int numPoints = 30; //TODO: change this to be dependent on the radius
        for (int i = 0; i < numPoints; i++) {
            float angle = (float) (2.0 * Math.PI / numPoints * i) + this.angle;
            float nextX = center.x + radius * (float) Math.cos(angle);
            float nextY = center.y + radius * (float) Math.sin(angle);

            circlePoints.add(new Vector3f(nextX, nextY, this.renderOrder));
        }
        circlePoints.add(circlePoints.getFirst());
        return circlePoints;
    }

    /** render this circle to the screen using the passed in graphics object */
    @Override
    public void render(Graphics2D graphics, double elapsedTime) {
        // convert the center and radius for the graphics API
        Vector2f centerCanvas2f = this.world.worldCoordsToCanvas(new Vec2(this.center.x, -this.center.y));
        Vector3f centerCanvas3f = new Vector3f(centerCanvas2f.x, centerCanvas2f.y, renderOrder);
        float canvasRadius = this.world.scalarWorldToCanvas(this.radius);

        // updates the points on the circle if the center changed position
        ArrayList<Vector3f> circlePoints = createCirclePoints(centerCanvas3f, canvasRadius);

        // render the circle as a set of triangles
        for (int i = 0; i < circlePoints.size()-1; i++) {
            Vector3f v1 = circlePoints.get(i);
            Vector3f v2 = circlePoints.get(i+1);
            graphics.draw(new Triangle(centerCanvas3f, v1, v2), -this.angle, centerCanvas2f, this.color);
        }
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("\nObject 'Rectangle':");
        str.append(String.format("\n\tType: '%s'", this.getBody().m_type));
        str.append(String.format("\n\tMass: %f kg", this.getBody().m_mass));
        str.append(String.format("\n\tFriction: %f", this.friction));
        str.append(String.format("\n\tRotation: %f rad", this.angle));
        str.append(String.format("\n\tRestitution: %f", this.restitution));
        str.append(String.format("\n\tVelocity: %s m/s", this.getBody().getLinearVelocity()));
        str.append(String.format("\n\tCenter: %s cu", this.getCenterCanvas()));
        str.append(String.format("\n\tRadius: %s cu", this.world.scalarWorldToCanvas(this.radius)));
        return str.toString();
    }
}
