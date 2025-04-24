package simulation;

import edu.usu.graphics.Color;
import edu.usu.graphics.Texture;
import org.jbox2d.dynamics.BodyType;
import org.joml.Vector2f;
import physics.*;

import java.util.ArrayList;

public class Simulation {
    public float simulationTime;
    public Vector2f gravity;
    public float zoom;
    public String name;
    public String description;
    public ArrayList<String> solutionOptions;
    public Color bgColor;

    private final ArrayList<ObjectData> physicsObjects = new ArrayList<>();

    public transient PhysicsWorld world;
    public transient double timeElapsedSinceStart;

    public ArrayList<PhysicsObject2D> create() {
        this.world = new PhysicsWorld(gravity, this.zoom);
        this.timeElapsedSinceStart = 0;

        ArrayList<PhysicsObject2D> physObjects = new ArrayList<>();
        for (ObjectData data : this.physicsObjects) {
            switch (data.shape) {
                case "rectangle" -> physObjects.add(createPhysRectangle(data, world));
                case "polygon" -> physObjects.add(createPhysPolygon(data, world));
                case "triangle" -> physObjects.add(createPhysTriangle(data, world));
                case "circle" -> physObjects.add(createPhysCircle(data, world));
            }
        }
        return physObjects;
    }

    public void stepForward(double elapsedTime, int iterations) {
        if (timeElapsedSinceStart <= simulationTime) {
            this.world.stepForward(elapsedTime, iterations);
        }
        timeElapsedSinceStart += elapsedTime;
    }

    public boolean simulationStopped() {
        return this.timeElapsedSinceStart >= simulationTime;
    }

    private Rect createPhysRectangle(ObjectData data, PhysicsWorld world) {
        float rotation = -data.rotation * (float) Math.PI / 180f;
        if (data.texture.isEmpty()) {
            return new Rect(
                    world, data.position, data.width, data.height, data.color, data.render_z, rotation, data.initial_velocity,
                    data.bodyType, data.density, data.friction, data.restitution
            );
        } else {
            Texture texture = new Texture(data.texture);
            return new Rect(
                    world, data.position, data.width, data.height, data.color, texture, data.render_z, rotation, data.initial_velocity,
                    data.bodyType, data.density, data.friction, data.restitution
            );
        }
    }

    private Circle createPhysCircle(ObjectData data, PhysicsWorld world) {
        float rotation = -data.rotation * (float) Math.PI / 180f;
        return new Circle(
                world, data.color, data.position, data.radius, data.render_z, rotation,
                data.initial_velocity, data.bodyType, data.density, data.friction, data.restitution
        );
    }

    private Triangle createPhysTriangle(ObjectData data, PhysicsWorld world) {
        float rotation = -data.rotation * (float) Math.PI / 180f;
        return new Triangle(
                world, data.vertices.get(0), data.vertices.get(1), data.vertices.get(2), data.color,
                data.render_z, rotation, data.initial_velocity, data.bodyType, data.density, data.friction, data.restitution
        );
    }

    private Polygon createPhysPolygon(ObjectData data, PhysicsWorld world) {
        float rotation = -data.rotation * (float) Math.PI / 180f;
        return new Polygon(
                world, data.vertices, data.color, data.render_z, rotation, data.initial_velocity,
                data.bodyType, data.density, data.friction, data.restitution
        );
    }

    public static class ObjectData {
        String shape;
        BodyType bodyType;
        float density;
        float friction;
        float rotation;
        float restitution;
        Vector2f initial_velocity;
        Vector2f position;

        float render_z;
        Color color;

        // for circle types
        float radius = 0;

        // for rect types
        float width = 0;
        float height = 0;
        String texture = "";

        // for polygon types
        ArrayList<Vector2f> vertices = new ArrayList<>();
    }
}

