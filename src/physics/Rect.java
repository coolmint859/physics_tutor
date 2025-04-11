package physics;

import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.objects.Rectangle;

import edu.usu.graphics.Texture;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Rect implements PhysicsObject2D {
    private final PhysicsWorld world;
    private Body body;
    private Rectangle rect;

    private Vector2f center;
    private Texture texture;

    public float renderOrder;
    public Color color;
    public boolean showLine;

    public float angle;
    public float mass = 1.0f;
    public float density = 1.0f;
    public float friction = 0.5f;
    public float restitution = 0.5f;

    public Rect(PhysicsWorld world, Rectangle rect, Color color, BodyType type, boolean showLine) {
        this.world = world;
        this.color = color;
        this.showLine = showLine;

        this.createPhysicsObject(rect, type);
    }

    // draw a rectangle with an image overlay
    public Rect(PhysicsWorld world, Rectangle rect, Texture texture, BodyType type) {
        this.world = world;
        this.texture = texture;
        this.showLine = false;

        this.createPhysicsObject(rect, type);
    }

    private void createPhysicsObject(Rectangle rect, BodyType type) {
        this.center = new Vector2f(rect.left + rect.width/2, rect.top + rect.height / 2);
        this.rect = rect;

        // create body definition;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(this.world.canvasToWorldCoords(this.center));

        // create body and place in the world
        this.body = world.addAndCreateBody(bodyDef);
        this.body.m_mass = 100.0f;
        this.angle = this.body.getAngle();

        // create shape - the width and height for JBox2D is relative to the center of the rectangle
        PolygonShape rectangle = new PolygonShape();
        float rectWidth = this.world.scalarCanvasToWorld(rect.width / 2);
        float rectHeight = this.world.scalarCanvasToWorld(rect.height / 2);
        rectangle.setAsBox(rectWidth, rectHeight);

        // create fixture
        FixtureDef fx = new FixtureDef();
        fx.shape = rectangle;
        fx.density = 1;
        fx.friction = 0.3f;
        fx.restitution = 0.15f;

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

    @Override
    public void update(double elapsedTime) {
        this.center = this.world.getBodyCenter(this.body);
        this.angle = -this.body.getAngle();

        // adjust rectangle location
        this.rect.left = this.center.x - this.rect.width / 2.0f;
        this.rect.top = this.center.y - this.rect.height / 2.0f;
    }

    @Override
    public void render(Graphics2D graphics, double elapsedTime) {
        if (this.texture == null)
            graphics.draw(this.rect, this.angle, this.center, this.color);
        else
            graphics.draw(this.texture, this.rect, this.angle, this.center, Color.WHITE);

        if (this.showLine) {
            Vector3f lineCenter = new Vector3f(this.center.x, this.center.y, this.rect.z + 0.01f);
            float lineEdgeX = center.x + (this.rect.width/2) * (float) Math.cos(this.angle);
            float lineEdgeY = center.y + (this.rect.width/2) * (float) Math.sin(this.angle);
            Vector3f lineEdge = new Vector3f(lineEdgeX, lineEdgeY, this.rect.z + 0.01f);
            graphics.draw(lineCenter, lineEdge, Color.BLACK);
        }
    }
}
