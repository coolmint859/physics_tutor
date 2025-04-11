package views;

import edu.usu.graphics.Color;
import edu.usu.graphics.Font;
import edu.usu.graphics.Graphics2D;
import utils.KeyboardInput;
import assets.SoundAssets;

import static org.lwjgl.glfw.GLFW.*;

public class AboutView implements StateView {
    private final Graphics2D graphics;
    private final SoundAssets audio;

    private KeyboardInput inputKeyboard;
    private StateEnum nextGameState = StateEnum.About;
    private Font font;

    public AboutView(Graphics2D graphics, SoundAssets audio) {
        this.graphics = graphics;
        this.audio = audio;
    }

    @Override
    public void initialize() {
        nextGameState = StateEnum.About;

        font = new Font("resources/fonts/Roboto-Regular.ttf", 48, false);

        registerCommands();
    }

    private void registerCommands() {
        inputKeyboard = new KeyboardInput(graphics.getWindow());
        inputKeyboard.registerKeyDown(GLFW_KEY_ESCAPE, true, (double elapsedTime) -> {
            nextGameState = StateEnum.MainMenu;
        });
    }

    @Override
    public StateEnum processInput(double elapsedTime) {
        // Updating the keyboard can change the nextGameState
        inputKeyboard.update(elapsedTime);
        return nextGameState;
    }

    @Override
    public void update(double elapsedTime) {
    }

    @Override
    public void render(double elapsedTime) {
        final String message = "*I* wrote this amazing game!";
        final float height = 0.075f;
        final float width = font.measureTextWidth(message, height);

        graphics.drawTextByHeight(font, message, 0.0f - width / 2, 0 - height / 2, height, Color.YELLOW);
    }
}
