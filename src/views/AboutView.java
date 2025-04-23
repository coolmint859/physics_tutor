package views;

import assets.ColorAssets;
import assets.FontAssets;
import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.objects.Text;
import org.joml.Vector3f;
import utils.KeyboardInput;
import assets.SoundAssets;
import utils.MouseInput;

import static org.lwjgl.glfw.GLFW.*;

public class AboutView implements StateView {
    private final Graphics2D graphics;
    private final SoundAssets audio;

    private KeyboardInput keyboard;
    private MouseInput cursor;
    
    private StateEnum nextState = StateEnum.About;
    private Text escapeText;

    public AboutView(Graphics2D graphics, SoundAssets audio) {
        this.graphics = graphics;
        this.audio = audio;
    }

    @Override
    public void initialize() {
        nextState = StateEnum.About;

        this.escapeText = new Text(new Vector3f(-0.85f, -0.5125f, 1.0f), "BACK (ESC)", FontAssets.robotoReg_OL, 0.06f, ColorAssets.menuEscapeColor);

        registerKeyboardCommands();
        registerCursorCommands();
    }

    private void registerKeyboardCommands() {
        keyboard = new KeyboardInput(graphics.getWindow());
        keyboard.registerKeyDown(GLFW_KEY_ESCAPE, true, (double elapsedTime) -> {
            nextState = StateEnum.MainMenu;
        });
    }

    private void registerCursorCommands() {
        this.cursor = new MouseInput(graphics.getWindow(), graphics.getWidth(), graphics.getHeight());
        cursor.setCursorType(GLFW_ARROW_CURSOR);
        cursor.addHoverListener(escapeText, true, (double elapsedTime, double x, double y) -> {
            escapeText.setColor(ColorAssets.menuSelectedColor);
            cursor.setCursorType(GLFW_HAND_CURSOR);
        });
        cursor.addExitListener(escapeText, (double elapsedTime, double x, double y) -> {
            escapeText.setColor(ColorAssets.menuEscapeColor);
            cursor.setCursorType(GLFW_ARROW_CURSOR);
        });
        cursor.addLeftClickListener(escapeText, true, (double elapsedTime, double x, double y) -> {
            nextState = StateEnum.MainMenu;
        });
    }

    @Override
    public StateEnum processInput(double elapsedTime) {
        // Updating the keyboard can change the nextGameState
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
        final String message = "Written by Preston Hall for CS 5620!";
        final float height = 0.075f;
        final float width = FontAssets.robotoReg.measureTextWidth(message, height);

        graphics.drawTextByHeight(FontAssets.robotoReg, message, 0.0f - width / 2, 0 - height / 2, height, Color.YELLOW);
    }
}
