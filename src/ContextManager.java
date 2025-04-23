import assets.SoundAssets;
import edu.usu.graphics.*;
import simulation.Simulation;
import simulation.SimulationParser;
import views.*;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

/**
 * The context manager handles the switching between the different views, as the user selects the different views.
 * It does this by having the current active view communicate the next view that should be displayed in the next frame.
 * As this is tied directly to the main loop, that is handled here as well.
 * */
public class ContextManager {
    private final Graphics2D graphics;
    private final SoundAssets sounds;

    private HashMap<StateEnum, StateView> states;
    private StateView currentState;
    StateEnum nextStateEnum = StateEnum.MainMenu;
    StateEnum prevStateEnum = StateEnum.MainMenu;

    public ContextManager(Graphics2D graphics) {
        this.graphics = graphics;

        this.sounds = new SoundAssets();
    }

    /**
     * Initializes the views and the currently active view
     * */
    public void initialize(String LLM_API_KEY) {
        ArrayList<Simulation> simulations = SimulationParser.createFromIndex("./resources/simulations/index.json");
        assert simulations != null && !simulations.isEmpty();

        states = new HashMap<>();
        this.states.put(StateEnum.Simulation, new SimulationView(graphics, sounds, simulations.getFirst(), LLM_API_KEY));
        this.states.put(StateEnum.SimulationSelect, new SimulationSelectView(
                graphics, sounds, simulations, (SimulationView) this.states.get(StateEnum.Simulation)));
        this.states.put(StateEnum.MainMenu, new MainMenuView(graphics, sounds));
        this.states.put(StateEnum.About, new AboutView(graphics, sounds));

        currentState = states.get(StateEnum.MainMenu);
        currentState.initialize();
    }

    /**
     * shuts down the graphics, ending the current session.
     * */
    public void shutdown() {
        this.graphics.close();
    }

    /**
     * Executes the main program loop. This is run for as long as the window is active.
     * */
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

    /**
     * Processes events made by the user in the current context
     * */
    private void processInput(double elapsedTime) {
        // Poll for window events: required in order for window, keyboard, etc events are captured.
        glfwPollEvents();

        nextStateEnum = currentState.processInput(elapsedTime);
    }

    /**
     * Updates the current state/switches to other states if the user requests to in the current context
     * */
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

    /**
     * renders the current context to the window
     * */
    private void render(double elapsedTime) {
        graphics.begin();

        currentState.render(elapsedTime);

        graphics.end();
    }
}
