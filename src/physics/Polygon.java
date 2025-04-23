package physics;

import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.objects.Triangle;
import org.jbox2d.collision.shapes.MassData;
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

    private final ArrayList<Vector2f> vertices;
    private Vector2f centroid;
    public float renderOrder;
    public Color color;

    public float angle;
    public float friction;
    public float restitution;

    public Polygon(PhysicsWorld world, ArrayList<Vector2f> vertices, Color color, float renderOrder, float initRotation, Vector2f initVelocity, BodyType type, float density, float friction, float restitution) {
        assert vertices.size() <= maxPolygonVertices;
        this.vertices = vertices;
        this.centroid = calculateCentroid();

        this.world = world;
        this.color = color;
        this.renderOrder = renderOrder;

        this.friction = friction;
        this.restitution = restitution;

        this.createPhysicsObject(type, density, initVelocity, initRotation);
    }

    /** Creates a body object that the physics library can use to do physics with */
    private void createPhysicsObject(BodyType type, float density, Vector2f initVelocity, float initRotation) {
        // create body definition;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(new Vec2(this.centroid.x, this.centroid.y));
        bodyDef.angle = initRotation;
        bodyDef.linearVelocity = new Vec2(initVelocity.x, initVelocity.y);

        // create body and place in the world
        this.body = world.addAndCreateBody(bodyDef);
        this.angle = this.body.getAngle();

        Vec2[] box2d_vertices = createVec2vertices();

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
        this.body.createFixture(fx);
    }

    private Vec2[] createVec2vertices() {
        Vec2[] box2d_vertices = new Vec2[this.vertices.size()];

        for (int i = 0; i < this.vertices.size(); i++) {
            Vector2f nextVertex = this.vertices.get(i);
            box2d_vertices[i] = new Vec2(nextVertex.x-centroid.x, nextVertex.y-centroid.y);
        }
        return box2d_vertices;
    }

    private Vector2f calculateCentroid() {
        float sumX = 0.0f;
        float sumY = 0.0f;
        for (Vector2f vertex : this.vertices) {
            sumX += vertex.x;
            sumY += vertex.y;
        }
        float centerX = sumX / vertices.size();
        float centerY = sumY / vertices.size();

        return new Vector2f(centerX, centerY);
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    @Override
    public Vector2f getCenter() {
        return this.centroid;
    }

    public Vector2f getCenterCanvas() {
        return new Vector2f(this.world.worldCoordsToCanvas(new Vec2(centroid.x, centroid.y)));
    }

    @Override
    public void update(double elapsedTime) {
        // retrieve the center point of the body from the world
        Vec2 bodyCenter = this.body.getWorldCenter();
        this.centroid = new Vector2f(bodyCenter.x, bodyCenter.y);
        this.angle = this.body.getAngle(); // inverted because the y-axis on the canvas is flipped
    }

    /** calculates the points that make the polygon for rendering */
    private ArrayList<Vector3f> createPolygonPoints(Vector2f centroid) {
        ArrayList<Vector3f> polygonPoints = new ArrayList<>();

        // go through the vertices and transform them into points that the graphics API can use
        for (Vector2f vertex : this.vertices) {
            Vector2f canvasVertex = this.world.worldCoordsToCanvas(new Vec2(vertex.x, vertex.y));
            // translate point to be relative to origin
            float pointX_t = canvasVertex.x - centroid.x;
            float pointY_t = canvasVertex.y - centroid.y;

            // rotate point about origin
            float pointX_tr = pointX_t * (float) Math.cos(this.angle) - pointY_t * (float) Math.sin(this.angle);
            float pointY_tr = pointX_t * (float) Math.sin(this.angle) + pointY_t * (float) Math.cos(this.angle);

            // translate point to be relative to the centroid
            float pointX_r = pointX_tr + centroid.x;
            float pointY_r = pointY_tr + centroid.y;

            Vector2f rotatedVertex = new Vector2f(pointX_r, pointY_r);
            polygonPoints.add(new Vector3f(rotatedVertex.x, rotatedVertex.y, this.renderOrder));
        }
        polygonPoints.add(polygonPoints.getFirst());

        return polygonPoints;
    }

    @Override
    public void render(Graphics2D graphics, double elapsedTime) {
        // convert the center and radius for the graphics API
        Vector2f centerCanvas2f = this.world.worldCoordsToCanvas(new Vec2(this.centroid.x, this.centroid.y));
        Vector3f centerCanvas3f = new Vector3f(centerCanvas2f.x, centerCanvas2f.y, this.renderOrder);

        // display the polygon as a set of triangles
        ArrayList<Vector3f> polygonPoints = createPolygonPoints(centerCanvas2f);
        for (int i = 0; i < polygonPoints.size()-1; i++) {
            Vector3f v1 = polygonPoints.get(i);
            Vector3f v2 = polygonPoints.get(i+1);
            graphics.draw(new Triangle(centerCanvas3f, v1, v2), this.angle, centerCanvas2f, this.color);
        }
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("\nObject 'Polygon':");
        str.append(String.format("\n\tType: '%s'", this.getBody().m_type));
        str.append(String.format("\n\tMass: %f kg", this.getBody().m_mass));
        str.append(String.format("\n\tFriction: %f", this.friction));
        str.append(String.format("\n\tRotation: %f rad", this.angle));
        str.append(String.format("\n\tRestitution: %f", this.restitution));
        str.append(String.format("\n\tVelocity: %s m/s", this.getBody().getLinearVelocity()));
        str.append(String.format("\n\tCentroid: %s cu", this.getCenterCanvas()));
        str.append(String.format("\n\tVertices: %s cu", this.vertices));
        return str.toString();
    }
}