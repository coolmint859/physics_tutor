package physics;

import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;

import edu.usu.graphics.objects.Triangle;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.dynamics.*;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

/** Used to render circles to the canvas, as well as do physics in the given physics world */
public class Circle implements PhysicsObject2D {
    private final PhysicsWorld world;
    private Body body;

    private ArrayList<Vector3f> circlePoints;
    private Vector3f center;

    public float renderOrder;
    public boolean showLine;
    public Color color;

    public float radius;
    public float angle;
    public float mass = 1.0f;
    public float density = 1.0f;
    public float friction = 0.5f;
    public float restitution = 0.5f;

    public Circle(PhysicsWorld world, Vector3f center, float radius, Color color, BodyType type, boolean showLine) {
        this.world = world;
        this.color = color;
        this.showLine = showLine;

        this.createPhysicsObject(center, radius, type);
        this.createCirclePoints(center, radius);
    }

    /** Creates a body object that the physics library can use to do physics with */
    private void createPhysicsObject(Vector3f center, float radius, BodyType type) {
        // create body definition;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(this.world.canvasToWorldCoords(new Vector2f(center.x, center.y)));

        // create body and place in the world
        this.body = world.addAndCreateBody(bodyDef);
        this.body.m_mass = mass;
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

    /** Creates a series of points that the graphics library can use to render a circle */
    private void createCirclePoints(Vector3f center, float radius) {
        this.circlePoints = new ArrayList<>();
        this.center = center;
        this.radius = radius;
        this.renderOrder = center.z;
        //TODO: change this to be dependent on the radius
        int numPoints = 30;
        for (int i = 0; i < numPoints; i++) {
            float angle = (float) (2.0 * Math.PI / numPoints * i) + this.angle;
            float nextX = this.center.x + radius * (float) Math.cos(angle);
            float nextY = this.center.y + radius * (float) Math.sin(angle);

            this.circlePoints.add(new Vector3f(nextX, nextY, this.renderOrder));
        }
        this.circlePoints.add(this.circlePoints.getFirst());
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    @Override
    public Vector2f getCenter() {
        return new Vector2f(this.center.x, this.center.y);
    }

    /** update the position of the points that represent the circle*/
    @Override
    public void update(double elapsedTime) {
        // retrieve the center point of the body from the world
        Vector2f bodyCenter = this.world.getBodyCenter(this.body);
        this.center = new Vector3f(bodyCenter.x, bodyCenter.y, this.renderOrder);
        this.angle = -this.body.getAngle(); // inverted because the y-axis on the canvas is flipped

        // update the vector points of the circle
        createCirclePoints(this.center, this.radius);
    }

    /** render this circle to the screen using the passed in graphics object*/
    @Override
    public void render(Graphics2D graphics, double elapsedTime) {
        Vector2f center = new Vector2f(this.center.x, this.center.y);
        for (int i = 0; i < this.circlePoints.size()-1; i++) {
            Vector3f v1 = this.circlePoints.get(i);
            Vector3f v2 = this.circlePoints.get(i+1);
            graphics.draw(new Triangle(this.center, v1, v2), this.angle, center, this.color);
        }

        Vector3f firstPoint = this.circlePoints.getFirst();
        if (this.showLine)
            graphics.draw(this.center, new Vector3f(firstPoint.x, firstPoint.y, this.renderOrder+0.01f), Color.BLACK);
    }
}
