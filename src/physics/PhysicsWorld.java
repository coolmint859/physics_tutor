package physics;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.joml.Vector2f;

/** wrapper class for a JBox2D world, has useful methods that convert world values and vectors
 * into values and vectors that the graphics engine can use. */
public class PhysicsWorld {
    private final World world;

    // 1 real life meter is this in canvas units
    private float canvasMeter = 0.05f;

    public PhysicsWorld(Vector2f gravity) {
        this.world = new World(new Vec2(gravity.x, gravity.y), false);
    }

    public void stepForward(double elapsedTime) {
        // the higher the number, the more accurate the physics, at the cost of frame rate.
        int calculationsPerFrame = 10;
        this.world.step((float) elapsedTime, calculationsPerFrame, calculationsPerFrame);
    }

    public void setGravity(Vector2f gravity) {
        this.world.setGravity(new Vec2(gravity.x, gravity.y));
    }

    public void setCanvasMeter(float meterLength) {
        this.canvasMeter = meterLength;
    }

    public Body addAndCreateBody(BodyDef def) {
        return this.world.createBody(def);
    }

    /** remove all references to the body */
    public void destroyObject(PhysicsObject2D object) {
        this.world.destroyBody(object.getBody());
    }

    public Vec2 canvasToWorldCoords(Vector2f pos) {
        return new Vec2(pos.x / canvasMeter, -pos.y / canvasMeter);
    }

    public Vector2f worldCoordsToCanvas(Vec2 pos) {
        return new Vector2f(pos.x * canvasMeter, -pos.y * canvasMeter);
    }

    public float scalarCanvasToWorld(float scalar) {
        return scalar / canvasMeter;
    }

    public float scalarWorldToCanvas(float scalar) {
        return scalar * canvasMeter;
    }

    public Vector2f getBodyCenter(Body body) {
        Vec2 center = body.getWorldCenter();
        return new Vector2f(center.x * canvasMeter, -center.y * canvasMeter);
    }
}
