package simulation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import physics.PhysicsObject2D;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SimulationParser {
    private static class SimIndex{ArrayList<String> paths;}

    public static ArrayList<Simulation> createFromIndex(String indexFile) {
        SimIndex simulationPaths;

        try (BufferedReader reader = new BufferedReader(new FileReader(indexFile))) {
            StringBuilder simJson = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                simJson.append(line);
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            simulationPaths = gson.fromJson(simJson.toString(), SimIndex.class);
        } catch (IOException e){
            java.lang.System.out.printf("Error: Index file '%s' was not found.\n", indexFile);
            return null;
        }

        ArrayList<Simulation> simulations = new ArrayList<>();
        for (String path : simulationPaths.paths) {
            simulations.add(createSimulation(path));
        }
        return simulations;
    }

    public static Simulation createSimulation(String simulationPath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(simulationPath))) {
            StringBuilder simJson = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                simJson.append(line);
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.fromJson(simJson.toString(), Simulation.class);
        } catch (IOException e){
            java.lang.System.out.printf("Error: Simulation file '%s' was not found.\n", simulationPath);
            return null;
        }
    }
}