package views;

import assets.ColorAssets;
import assets.FontAssets;
import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.objects.Text;
import org.joml.Vector3f;
import simulation.Simulation;
import utils.KeyboardInput;
import assets.SoundAssets;
import utils.MouseInput;

import static org.lwjgl.glfw.GLFW.*;

public class SimulationView implements StateView {
    private final Graphics2D graphics;
    private final SoundAssets audio;

    private Simulation currentSimulation;

    private KeyboardInput keyboard;
    private MouseInput cursor;

    private Text escapeText;

    private StateEnum nextState;

    public SimulationView(Graphics2D graphics, SoundAssets audio, Simulation defaultSim) {
        this.graphics = graphics;
        this.audio = audio;

        this.currentSimulation = defaultSim;
    }

    @Override
    public void initialize() {
        nextState = StateEnum.Simulation;

        this.escapeText = new Text(new Vector3f(-0.7f, -0.5125f, 1.0f), "BACK", FontAssets.robotoReg_OL, 0.06f, ColorAssets.menuEscapeColor);

        registerKeyboardCommands();
        registerCursorCommands();
    }

    public void setCurrentSimulation(Simulation sim) {
        this.currentSimulation = sim;
    }

    private void registerKeyboardCommands() {
        keyboard = new KeyboardInput(graphics.getWindow());
    }

    private void registerCursorCommands() {
        cursor = new MouseInput(graphics.getWindow(), graphics.getWidth(), graphics.getHeight());
        cursor.addHoverListener(escapeText, true, (double elapsedTime, double x, double y) -> {
            escapeText.setColor(ColorAssets.menuSelectedColor);
            cursor.setCursorType(GLFW_HAND_CURSOR);
        });
        cursor.addExitListener(escapeText, (double elapsedTime, double x, double y) -> {
            escapeText.setColor(ColorAssets.menuEscapeColor);
            cursor.setCursorType(GLFW_ARROW_CURSOR);
        });
        cursor.addLeftClickListener(escapeText, true, (double elapsedTime, double x, double y) -> {
            nextState = StateEnum.SimulationSelect;
        });
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
    }

    @Override
    public void render(double elapsedTime) {
        escapeText.draw(graphics);

        final String message = "You are seeing simulation " + this.currentSimulation.name;
        final float height = 0.075f;
        final float width = FontAssets.robotoReg.measureTextWidth(message, height);

        graphics.drawTextByHeight(FontAssets.robotoReg, message, 0.0f - width / 2, 0 - height / 2, height, Color.YELLOW);
    }
}
