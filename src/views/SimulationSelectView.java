package views;

import assets.ColorAssets;
import assets.FontAssets;
import assets.SoundAssets;
import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.objects.Text;
import org.joml.Vector3f;
import simulation.Simulation;
import utils.*;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class SimulationSelectView implements StateView {
    private final Graphics2D graphics;
    private final SoundAssets sounds;

    private final float maxTimeOut = 0.1f; // the amount of time that input is disabled (prevents premature selections)
    private float timeoutElapsed;

    private KeyboardInput keyboard;
    private MouseInput cursor;

    private final ArrayList<Simulation> simulations;
    private final SimulationView simulationView;
    private HashMap<String, Simulation> simulationMapping;
    private ArrayList<Text> simulationsNames;
    private final String escapeText = "Main Menu";

    private StateEnum nextState;

    public SimulationSelectView(Graphics2D graphics, SoundAssets sounds, ArrayList<Simulation> simulations, SimulationView simulationView) {
        this.graphics = graphics;
        this.sounds = sounds;

        this.simulations = simulations;
        this.simulationView = simulationView;
    }

    @Override
    public void initialize() {
        nextState = StateEnum.SimulationSelect;
        this.timeoutElapsed = 0.0f;

        simulationMapping = new HashMap<>();
        simulationsNames = new ArrayList<>();
        simulationsNames.add(new Text(new Vector3f(0.0f, -0.15f, RenderOrders.HUD2_z), escapeText, FontAssets.robotoReg_OL, 0.1f, ColorAssets.menuEscapeColor));
        for (int i = 0; i < simulations.size(); i++) {
            Simulation sim = simulations.get(i);

            simulationsNames.add(new Text(new Vector3f(0.0f, -0.05f + 0.1f*i, RenderOrders.HUD2_z), sim.name, FontAssets.robotoReg_OL, 0.1f, ColorAssets.menuTextColor));
            simulationMapping.put(sim.name, sim);
        }

        registerKeyboardCommands();
        registerCursorCommands();
    }

    private void registerKeyboardCommands() {
        keyboard = new KeyboardInput(graphics.getWindow());
        keyboard.registerKeyDown(GLFW_KEY_ESCAPE, true, (double elapsedTime) -> {
            // prevents the state from immediately switching to the main menu (if using keyboard)
            if (this.timeoutElapsed < this.maxTimeOut)
                return;

            nextState = StateEnum.MainMenu;
        });
    }

    private void registerCursorCommands() {
        cursor = new MouseInput(graphics.getWindow(), graphics.getWidth(), graphics.getHeight());
        cursor.setCursorType(GLFW_ARROW_CURSOR);
        for (Text simName : this.simulationsNames) {
            cursor.addHoverListener(simName, true, (double elapsedTime, double x, double y) -> {
                simName.setColor(ColorAssets.menuSelectedColor);
                cursor.setCursorType(GLFW_HAND_CURSOR);
            });
            cursor.addExitListener(simName, (double elapsedTime, double x, double y) -> {
                Color textColor = simName.getTextStr().equals(escapeText) ? ColorAssets.menuEscapeColor : ColorAssets.menuTextColor;
                simName.setColor(textColor);
                cursor.setCursorType(GLFW_ARROW_CURSOR);
            });
            cursor.addLeftClickListener(simName, true, (double elapsedTime, double x, double y) -> {
                if (simName.getTextStr().equals(escapeText)) {
                    nextState = StateEnum.MainMenu;
                    return;
                }
                this.simulationView.setCurrentSimulation(this.simulationMapping.get(simName.getTextStr()));
                nextState = StateEnum.Simulation;
            });
        }
    }

    @Override
    public StateEnum processInput(double elapsedTime) {
        // Updating the keyboard can change the state
        keyboard.update(elapsedTime);
        cursor.update(elapsedTime);
        return nextState;
    }

    @Override
    public void update(double elapsedTime) {
        this.timeoutElapsed += (float) elapsedTime;
    }

    @Override
    public void render(double elapsedTime) {
        graphics.setClearColor(ColorAssets.menuBGColor);
        for (Text simName : this.simulationsNames) {
            simName.draw(graphics);
        }
    }
}
