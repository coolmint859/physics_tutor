package simulation;

import java.util.ArrayList;

public class SimulationParser {
    public static ArrayList<Simulation> parse(String indexFile) {
        ArrayList<Simulation> simulations = new ArrayList<>();
        simulations.add(new Simulation("Projectiles"));
        simulations.add(new Simulation("Wedge"));
        simulations.add(new Simulation("Billiards"));

        return simulations;
    }
}
