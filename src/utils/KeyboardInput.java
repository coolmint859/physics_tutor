package utils;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class KeyboardInput {

    /**
     * The type of method to invoke when a keyboard event is invoked
     */
    public interface ICommand {
        void invoke(double elapsedTime);
    }

    public KeyboardInput(long window) {
        this.window = window;
    }

    public void registerKeyDown(int key, boolean keyPressOnly, ICommand callback) {
        keyDownCommands.put(key, new CommandEntry(key, keyPressOnly, callback));
        // Start out by assuming the key isn't currently pressed
        keysPressed.put(key, false);
    }

    public void registerKeyUp(int key, ICommand callback) {
        keyUpCommands.put(key, new CommandEntry(key, true, callback));
    }

    /**
     * Go through all the registered command and invoke the callbacks as appropriate
     */
    public void update(double elapsedTime) {
        for (var entry : keyDownCommands.entrySet()) {
            if (entry.getValue().keyPressOnly && isKeyNewlyPressed(entry.getValue().key)) {
                // key pressed and callback invoked once
                entry.getValue().callback.invoke(elapsedTime);
            } else if (!entry.getValue().keyPressOnly && glfwGetKey(window, entry.getKey()) == GLFW_PRESS) {
                // key pressed and callback invoked repeatedly
                entry.getValue().callback.invoke(elapsedTime);
            } else if (keyUpCommands.containsKey(entry.getValue().key) && keyJustReleased(entry.getValue().key)) {
                // key just released and key has release callback (only is called once)
                keyUpCommands.get(entry.getValue().key).callback.invoke(elapsedTime);
            }

            // For the next time around, remember the current state of the key (pressed or not)
            keysPressed.put(entry.getKey(), glfwGetKey(window, entry.getKey()) == GLFW_PRESS);
        }
    }

    /**
     * Returns true if the key is newly pressed.  If it was already pressed, then
     * it returns false
     */
    private boolean isKeyNewlyPressed(int key) {
        return (glfwGetKey(window, key) == GLFW_PRESS) && !keysPressed.get(key);
    }

    private boolean keyJustReleased(int key) {
        return (glfwGetKey(window, key) == GLFW_RELEASE) & keysPressed.get(key);
    }

    private final long window;

    // Tables of registered callbacks
    private final HashMap<Integer, CommandEntry> keyUpCommands = new HashMap<>();
    private final HashMap<Integer, CommandEntry> keyDownCommands = new HashMap<>();

    // Table of registered callback keys previous pressed state
    private final HashMap<Integer, Boolean> keysPressed = new HashMap<>();

    /**
     * Used to keep track of the details associated with a registered command
     */
    private record CommandEntry(int key, boolean keyPressOnly, ICommand callback) {}
}
