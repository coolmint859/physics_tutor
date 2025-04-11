package simulation;

import physics.PhysicsObject2D;

import java.util.ArrayList;

public class Simulation {
    private static int nextID = 0;
    private final int id;

    public ArrayList<PhysicsObject2D> physicsObjects;

    public String name;
    public float simulationTime;

    public Simulation(String name) {
        this.name = name;

        this.id = nextID;
        nextID++;
    }

    public int getID() {
        return this.id;
    }
}
