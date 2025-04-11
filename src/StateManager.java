import assets.SoundAssets;
import edu.usu.graphics.*;
import simulation.Simulation;
import simulation.SimulationParser;
import views.*;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class StateManager {
    private final Graphics2D graphics;
    private final SoundAssets sounds;

    private HashMap<StateEnum, StateView> states;
    private StateView currentState;
    StateEnum nextStateEnum = StateEnum.MainMenu;
    StateEnum prevStateEnum = StateEnum.MainMenu;

    public StateManager(Graphics2D graphics) {
        this.graphics = graphics;

        this.sounds = new SoundAssets();
    }

    public void initialize() {
        ArrayList<Simulation> simulations = SimulationParser.parse("./resources/simulations/simulation_index.json");

        states = new HashMap<>();
        this.states.put(StateEnum.Simulation, new SimulationView(graphics, sounds, simulations.getFirst()));
        this.states.put(StateEnum.SimulationSelect, new SimulationSelectView(
                graphics, sounds, simulations, (SimulationView) this.states.get(StateEnum.Simulation)));
        this.states.put(StateEnum.MainMenu, new MainMenuView(graphics, sounds));
        this.states.put(StateEnum.About, new AboutView(graphics, sounds));

        currentState = states.get(StateEnum.MainMenu);
        currentState.initialize();
    }

    public void shutdown() {
    }

    public void run() {
        // Grab the first time
        double previousTime = glfwGetTime();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!graphics.shouldClose()) {
            double currentTime = glfwGetTime();
            double elapsedTime = currentTime - previousTime;    // elapsed time is in seconds
            previousTime = currentTime;

            processInput(elapsedTime);
            update(elapsedTime);
            render(elapsedTime);
        }
    }

    private void processInput(double elapsedTime) {
        // Poll for window events: required in order for window, keyboard, etc events are captured.
        glfwPollEvents();

        nextStateEnum = currentState.processInput(elapsedTime);
    }

    private void update(double elapsedTime) {
        // Special case for exiting the game
        if (nextStateEnum == StateEnum.Quit) {
            glfwSetWindowShouldClose(graphics.getWindow(), true);
        } else {
            if (nextStateEnum == prevStateEnum) {
                currentState.update(elapsedTime);
            } else {
                currentState = states.get(nextStateEnum);
                currentState.initialize();
                currentState.update(elapsedTime);
                prevStateEnum = nextStateEnum;
            }
        }
    }

    private void render(double elapsedTime) {
        graphics.begin();

        currentState.render(elapsedTime);

        graphics.end();
    }
}
