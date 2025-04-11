package views;

import assets.ColorAssets;
import assets.FontAssets;
import assets.SoundAssets;
import edu.usu.graphics.Font;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.objects.Text;
import org.joml.Vector3f;
import org.lwjgl.system.windows.MOUSEINPUT;
import utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class MainMenuView implements StateView {
    private enum MenuState {
        Simulations,
        About,
        Quit
    }

    // stores position and text info for a menu item
    private record MenuItem(int pos, String text) {
        public static ArrayList<String> getAsStrings(ArrayList<MenuItem> items) {
            String[] itemText = new String[items.size()];
            for (var item : items) {
                itemText[item.pos] = item.text;
            }
            return new ArrayList<>(List.of(itemText));
        }
    }

    private final Graphics2D graphics;
    private final SoundAssets audio;

    private MenuState currentSelection = MenuState.Simulations;
    private StateEnum nextState = StateEnum.MainMenu;

    private KeyboardInput keyboard;
    private MouseInput cursor;

    private HashMap<MenuState, Text> menuData;
    private ArrayList<Text> textObjects;

    public MainMenuView(Graphics2D graphics, SoundAssets audio) {
        this.graphics = graphics;
        this.audio = audio;
    }

    @Override
    public void initialize() {
        this.nextState = StateEnum.MainMenu;

        this.textObjects = new ArrayList<>() {
            {
                add(new Text(new Vector3f(0.0f, -0.1f, 1.0f), "Simulations", FontAssets.robotoReg_OL, 0.1f, ColorAssets.menuTextColor));
                add(new Text(new Vector3f(0.0f, 0.0f, 1.0f), "About", FontAssets.robotoReg_OL, 0.1f, ColorAssets.menuTextColor));
                add(new Text(new Vector3f(0.0f, 0.1f, 1.0f), "Quit", FontAssets.robotoReg_OL, 0.1f, ColorAssets.menuTextColor));
            }
        };

        this.menuData = new HashMap<>();
        this.menuData.put(MenuState.Simulations, textObjects.get(0));
        this.menuData.put(MenuState.About, textObjects.get(1));
        this.menuData.put(MenuState.Quit, textObjects.get(2));

        this.registerCursorCommands();
        this.registerKeyboardCommands();
    }

    private void registerKeyboardCommands() {
        keyboard = new KeyboardInput(graphics.getWindow());
        // events handled by mouse
    }

    private void registerCursorCommands() {
        cursor = new MouseInput(this.graphics.getWindow(), this.graphics.getWidth(), this.graphics.getHeight());
        for (Text textObject : this.textObjects) {
            cursor.addHoverListener(textObject, true, (double elapsedTime, double x, double y) -> {
                textObject.setColor(ColorAssets.menuSelectedColor);
                cursor.setCursorType(GLFW_HAND_CURSOR);
            });
            cursor.addExitListener(textObject, (double elapsedTime, double x, double y) -> {
                textObject.setColor(ColorAssets.menuTextColor);
                cursor.setCursorType(GLFW_ARROW_CURSOR);
            });
            cursor.addLeftClickListener(textObject, true, (double elapsedTime, double x, double y) -> {
                nextState = switch (textObject.getTextStr()) {
                    case "Simulations" -> StateEnum.SimulationSelect;
                    case "About" -> StateEnum.About;
                    default -> StateEnum.Quit;
                };
            });
        }
    }

    @Override
    public StateEnum processInput(double elapsedTime) {
        keyboard.update(elapsedTime);
        cursor.update(elapsedTime);

        return nextState;
    }

    @Override
    public void update(double elapsedTime) {
        // no updates on main menu
    }

    @Override
    public void render(double elapsedTime) {
        for (Text textObject : this.textObjects) {
            textObject.draw(graphics);
        }
    }
}
