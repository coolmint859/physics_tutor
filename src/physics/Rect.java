package physics;

import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.objects.Rectangle;

import edu.usu.graphics.Texture;
import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import org.joml.Vector2f;

public class Rect implements PhysicsObject2D {
    private final PhysicsWorld world;
    private Body body;

    private Vector2f center;
    private final float width;
    private final float height;
    private Texture texture;

    public float renderOrder;
    public Color color;

    public float angle;
    public float friction;
    public float restitution;

    public Rect(PhysicsWorld world, Vector2f center, float width, float height, Color color, float renderOrder, float initRotation, Vector2f initVelocity, BodyType type, float density, float friction, float restitution) {
        this.world = world;
        this.color = color;
        this.center = center;
        this.width = width;
        this.height = height;
        this.renderOrder = renderOrder;

        this.friction = friction;
        this.restitution = restitution;

        this.createPhysicsObject(type, density, initVelocity, initRotation);
    }

    // draw a rectangle with an image overlay
    public Rect(PhysicsWorld world, Vector2f center, float width, float height, Color color, Texture texture, float renderOrder, float initRotation, Vector2f initVelocity, BodyType type, float density, float friction, float restitution) {
        this.world = world;
        this.color = color;
        this.center = center;
        this.width = width;
        this.height = height;
        this.texture = texture;
        this.renderOrder = renderOrder;

        this.friction = friction;
        this.restitution = restitution;

        this.createPhysicsObject(type, density, initVelocity, initRotation);
    }

    private void createPhysicsObject(BodyType type, float density, Vector2f initVelocity, float initRotation) {
        // create body definition;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(new Vec2(center.x, center.y));
        bodyDef.angle = initRotation;
        bodyDef.linearVelocity = new Vec2(initVelocity.x, initVelocity.y);

        // create body and place in the world
        this.body = world.addAndCreateBody(bodyDef);
        this.body.setLinearVelocity(new Vec2(initVelocity.x, initVelocity.y));

        // create shape - the width and height for JBox2D is relative to the center of the rectangle
        PolygonShape rectangle = new PolygonShape();
        rectangle.setAsBox(this.width/2, this.height/2);

        // create fixture
        FixtureDef fx = new FixtureDef();
        fx.shape = rectangle;
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

    @Override
    public void update(double elapsedTime) {
        Vec2 bodyCenter = this.body.getWorldCenter();
        this.center = new Vector2f(bodyCenter.x, bodyCenter.y);
        this.angle = this.body.getAngle();
    }

    @Override
    public void render(Graphics2D graphics, double elapsedTime) {
        // convert the center for the graphics API
        Vector2f centerCanvas = this.world.worldCoordsToCanvas(new Vec2(center.x, center.y));

        // convert the width and height for the graphics API
        float widthCanvas = this.world.scalarWorldToCanvas(this.width);
        float heightCanvas= this.world.scalarWorldToCanvas(this.height);
        float left = centerCanvas.x - widthCanvas/2;
        float top = centerCanvas.y - heightCanvas/2;

        // render the rectangle
        Rectangle rectangle = new Rectangle(left, top, widthCanvas, heightCanvas, renderOrder);
        if (this.texture == null)
            graphics.draw(rectangle, -this.angle, centerCanvas, this.color);
        else
            graphics.draw(this.texture, rectangle, -this.angle, centerCanvas, this.color);
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
        str.append(String.format("\n\tHeight: %s cu", this.world.scalarWorldToCanvas(this.height)));
        str.append(String.format("\n\tWidth: %s cu", this.world.scalarWorldToCanvas(this.width)));
        return str.toString();
    }
}
