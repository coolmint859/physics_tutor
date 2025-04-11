package physics;

import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.objects.Triangle;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.jbox2d.common.Settings.maxPolygonVertices;

public class Polygon implements PhysicsObject2D {
    private final PhysicsWorld world;
    private Body body;

    private ArrayList<Vector3f> polygonPoints;
    private Vector3f centroid;

    public float renderOrder;
    public Color color;
    public boolean showLine;

    public float angle;
    public float mass = 1.0f;
    public float density = 1.0f;
    public float friction = 0.5f;
    public float restitution = 0.5f;

    public Polygon(PhysicsWorld world, ArrayList<Vector3f> vertices, Color color, BodyType type, boolean showLine) {
        assert vertices.size() <= maxPolygonVertices;

        this.world = world;
        this.color = color;
        this.showLine = showLine;

        this.createPhysicsObject(vertices, type);
        this.createPolygonPoints(vertices);
    }

    /** Creates a body object that the physics library can use to do physics with */
    private void createPhysicsObject(ArrayList<Vector3f> vertices, BodyType type) {
        // create body definition;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(this.world.canvasToWorldCoords(new Vector2f(centroid.x, centroid.y)));

        // create body and place in the world
        this.body = world.addAndCreateBody(bodyDef);
        this.body.m_mass = mass;
        this.angle = this.body.getAngle();

        Vec2[] box2d_vertices = createVec2vertices(vertices);

        // create shape
        PolygonShape polygon = new PolygonShape();
        polygon.set(box2d_vertices, box2d_vertices.length);

        // create fixture
        FixtureDef fx = new FixtureDef();
        fx.shape = polygon;
        fx.density = density;
        fx.friction = friction;
        fx.restitution = restitution;

        // attach fixture to body
        body.createFixture(fx);
    }

    private Vec2[] createVec2vertices(ArrayList<Vector3f> vertices) {
        Vec2[] box2d_vertices = new Vec2[vertices.size()];

        for (int i = 0; i < vertices.size(); i++) {
            Vector3f nextVertex = vertices.get(i);
            box2d_vertices[i] = this.world.canvasToWorldCoords(new Vector2f(nextVertex.x, nextVertex.y));
        }
        return box2d_vertices;
    }

    private void createPolygonPoints(ArrayList<Vector3f> vertices) {
        this.centroid = this.calculateCentroid(vertices);

        this.renderOrder = centroid.z;
        this.polygonPoints = new ArrayList<>();
        for (Vector3f vertex : vertices) {
            this.polygonPoints.add(rotateAboutPoint(this.centroid, vertex, this.angle));
        }
        this.polygonPoints.add(this.polygonPoints.getFirst());
    }

    private Vector3f calculateCentroid(ArrayList<Vector3f> vertices) {
        float sumX = 0.0f;
        float sumY = 0.0f;
        for (Vector3f vertex : vertices) {
            sumX += vertex.x;
            sumY += vertex.y;
        }
        float centerX = sumX / vertices.size();
        float centerY = sumY / vertices.size();

        return new Vector3f(centerX, centerY, vertices.getFirst().z);
    }

    private Vector3f rotateAboutPoint(Vector3f center, Vector3f point, float angle) {
        // step 1: translate point to be relative to the origin;
        float pointX_t = point.x - center.x;
        float pointY_t = point.y - center.y;

        // step 2: apply rotation
        float pointX_tr = pointX_t * (float) Math.cos(angle) - pointY_t * (float) Math.sin(angle);
        float pointY_tr = pointX_t * (float) Math.sin(angle) + pointY_t * (float) Math.cos(angle);

        // step 3: re-translate point to be relative to center
        float pointX_r = pointX_tr + center.x;
        float pointY_r = pointY_tr + center.y;

        // create and return rotated point as a vector
        return new Vector3f(pointX_r, pointY_r, point.z);
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    @Override
    public Vector2f getCenter() {
        return new Vector2f(centroid.x, centroid.y);
    }

    @Override
    public void update(double elapsedTime) {
        // retrieve the center point of the body from the world
        Vector2f bodyCenter = this.world.getBodyCenter(this.body);
        this.centroid = new Vector3f(bodyCenter.x, bodyCenter.y, this.renderOrder);
        this.angle = -this.body.getAngle(); // inverted because the y-axis on the canvas is flipped

        // update the vector points of the ellipse
        this.createPolygonPoints(this.polygonPoints);
    }

    @Override
    public void render(Graphics2D graphics, double elapsedTime) {
        Vector2f center = new Vector2f(this.centroid.x, this.centroid.y);
        for (int i = 0; i < this.polygonPoints.size()-1; i++) {
            Vector3f v1 = this.polygonPoints.get(i);
            Vector3f v2 = this.polygonPoints.get(i+1);
            graphics.draw(new Triangle(this.centroid, v1, v2), this.angle, center, this.color);
        }

        Vector3f firstPoint = this.polygonPoints.getFirst();
        if (this.showLine)
            graphics.draw(this.centroid, new Vector3f(firstPoint.x, firstPoint.y, this.renderOrder+0.01f), Color.BLACK);
    }
}
