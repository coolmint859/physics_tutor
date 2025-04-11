package utils;

import edu.usu.graphics.objects.Clickable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Basic mouse event handling such as left and right click, left and right button release, hovering, and hover exit.
 * Assumes the aspect ratio of the window (height/width) is between 0 and 1.
 * */
public class MouseInput {
    private final long window;
    private final int windowWidth;
    private final int windowHeight;

    /**
     * Mouse event callback function.
     * */
    public interface MouseEvent {
        void invoke(double elapsedTime, double mouseX, double mouseY);
    }

    private double currentMouseX;
    private double currentMouseY;

    private boolean isLeftButtonDown;
    private boolean wasLeftButtonDown;

    private boolean isRightButtonDown;
    private boolean wasRightButtonDown;

    private final List<Clickable> registeredObjects = new ArrayList<>();
    private Clickable currentlyHoveredObject = null;
    private Clickable lastHoveredObject = null;

    private record EventEntry(Clickable object, boolean onceOnly, MouseEvent callback) {}

    private final HashMap<Clickable, EventEntry> leftClickCallbacks = new HashMap<>();
    private final HashMap<Clickable, EventEntry> rightClickCallbacks = new HashMap<>();
    private final HashMap<Clickable, EventEntry> leftReleaseCallbacks = new HashMap<>();
    private final HashMap<Clickable, EventEntry> rightReleaseCallbacks = new HashMap<>();
    private final HashMap<Clickable, EventEntry> hoverCallbacks = new HashMap<>();
    private final HashMap<Clickable, EventEntry> exitCallbacks = new HashMap<>();

    public MouseInput(long window, int windowWidth, int windowHeight) {
        this.window = window;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        GLFW.glfwSetCursorPosCallback(window, (windowHandle, mouseX, mouseY) -> {
            this.currentMouseX = mouseX;
            this.currentMouseY = mouseY;
        });
        GLFW.glfwSetMouseButtonCallback(window, (windowHandle, button, action, mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                isLeftButtonDown = (action == GLFW.GLFW_PRESS);
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                isRightButtonDown = (action == GLFW.GLFW_PRESS);
            }
        });
    }

    /**
     * Frees up memory space after mouse input is no longer needed.
     * This should be called at the end of this object's lifetime.
     * */
    public void cleanup() {
        GLFW.glfwSetMouseButtonCallback(window, null).free();
        GLFW.glfwSetCursorPosCallback(window, null).free();
    }

    /**
     * Registers an object to receive mouse events.
     *
     * @param object The object to register (e.g., a GUI element).
     */
    public void registerObject(Clickable object) {
        if (!registeredObjects.contains(object)) {
            registeredObjects.add(object);
        }
    }

    /**
     * Unregisters an object, so it no longer receives mouse events.
     *
     * @param object The object to unregister.
     */
    public void unregisterObject(Clickable object) {
        registeredObjects.remove(object);
        leftClickCallbacks.remove(object);
        leftReleaseCallbacks.remove(object);
        rightClickCallbacks.remove(object);
        rightReleaseCallbacks.remove(object);
        hoverCallbacks.remove(object);
        exitCallbacks.remove(object);

        if (currentlyHoveredObject == object) {
            currentlyHoveredObject = null;
        }
        if (lastHoveredObject == object) {
            lastHoveredObject = null;
        }
    }

    public void setCursorType(int cursorType) {
        long newCursor = GLFW.glfwCreateStandardCursor(cursorType);
        GLFW.glfwSetCursor(window, newCursor);
    }

    /**
     * Add a left click listener to a clickable object.
     *
     * @param object The object to add a listener to.
     * @param onceOnly Whether the listener should be invoked once per click or for as long as the left mouse button is down.
     * @param callback The callback function for when the left mouse button is clicked.
     */
    public void addLeftClickListener(Clickable object, boolean onceOnly, MouseEvent callback) {
        registerObject(object);
        leftClickCallbacks.put(object, new EventEntry(object, onceOnly, callback));
    }

    /**
     * Add a right click listener to a clickable object.
     *
     * @param object The object to add a listener to.
     * @param onceOnly Whether the listener should be invoked once per click or for as long as the right mouse button is down.
     * @param callback The callback function for when the right mouse button is clicked.
     */
    public void addRightClickListener(Clickable object, boolean onceOnly, MouseEvent callback) {
        registerObject(object);
        rightClickCallbacks.put(object, new EventEntry(object, onceOnly, callback));
    }

    /**
     * Add a hover over listener to a clickable object.
     *
     * @param object The object to add a listener to.
     * @param onceOnly Whether the listener should be invoked once when the mouse hovers over or for as long as it is hovering.
     * @param callback The callback function for when the mouse hoveres over.
     */
    public void addHoverListener(Clickable object, boolean onceOnly, MouseEvent callback) {
        registerObject(object);
        hoverCallbacks.put(object, new EventEntry(object, onceOnly, callback));
    }

    /**
     * Add a left mouse button release listener to a clickable object.
     *
     * @param object The object to add a listener to.
     * @param callback The callback function for when the left mouse button is released.
     */
    public void addLeftReleaseListener(Clickable object, MouseEvent callback) {
        registerObject(object);
        leftReleaseCallbacks.put(object, new EventEntry(object, true, callback));
    }

    /**
     * Add a right mouse button release listener to a clickable object.
     *
     * @param object The object to add a listener to.
     * @param callback The callback function for when the right mouse button is released.
     */
    public void addRightReleaseListener(Clickable object, MouseEvent callback) {
        registerObject(object);
        rightReleaseCallbacks.put(object, new EventEntry(object, true, callback));
    }

    /**
     * Add a hover exit listener to a clickable object.
     *
     * @param object The object to add a listener to.
     * @param callback The callback function for when the object ceases being hovered over.
     */
    public void addExitListener(Clickable object, MouseEvent callback) {
        registerObject(object);
        exitCallbacks.put(object, new EventEntry(object, true, callback));
    }

    /**
     * This function should be called once per frame to update the mouse state
     * and trigger any relevant callbacks.
     */
    public void update(double elapsedTime) {
        handleLeftClickCallbacks(elapsedTime, currentMouseX, currentMouseY);
        handleLeftReleaseCallbacks(elapsedTime, currentMouseX, currentMouseY);
        handleRightClickCallbacks(elapsedTime, currentMouseX, currentMouseY);
        handleRightReleaseCallbacks(elapsedTime, currentMouseX, currentMouseY);
        handleHoverCallbacks(elapsedTime, currentMouseX, currentMouseY);
        handleExitCallbacks(elapsedTime, currentMouseX, currentMouseY);

        wasLeftButtonDown = isLeftButtonDown;
        wasRightButtonDown = isRightButtonDown;
        lastHoveredObject = currentlyHoveredObject;
        currentlyHoveredObject = null;
    }

    private void handleLeftClickCallbacks(double elapsedTime, double mouseX, double mouseY) {
        for (HashMap.Entry<Clickable, EventEntry> entry : leftClickCallbacks.entrySet()) {
            if (!isMouseOverObject(entry.getValue().object, mouseX, mouseY))
                continue;

            if (entry.getValue().onceOnly && isLeftButtonDown && !wasLeftButtonDown) {
                entry.getValue().callback.invoke(elapsedTime, mouseX, mouseY);
            } else if (!entry.getValue().onceOnly && isLeftButtonDown) {
                entry.getValue().callback.invoke(elapsedTime, mouseX, mouseY);
            }
        }
    }

    private void handleLeftReleaseCallbacks(double elapsedTime, double mouseX, double mouseY) {
        for (HashMap.Entry<Clickable, EventEntry> entry : leftReleaseCallbacks.entrySet()) {
            if (!isMouseOverObject(entry.getValue().object, mouseX, mouseY))
                continue;

            if (!isLeftButtonDown && wasLeftButtonDown)
                entry.getValue().callback.invoke(elapsedTime, mouseX, mouseY);
        }
    }

    private void handleRightClickCallbacks(double elapsedTime, double mouseX, double mouseY) {
        for (HashMap.Entry<Clickable, EventEntry> entry : rightClickCallbacks.entrySet()) {
            if (!isMouseOverObject(entry.getValue().object, mouseX, mouseY))
                continue;

            if (entry.getValue().onceOnly && isRightButtonDown && !wasRightButtonDown) {
                entry.getValue().callback.invoke(elapsedTime, mouseX, mouseY);
            } else if (!entry.getValue().onceOnly && isRightButtonDown) {
                entry.getValue().callback.invoke(elapsedTime, mouseX, mouseY);
            }
        }
    }

    private void handleRightReleaseCallbacks(double elapsedTime, double mouseX, double mouseY) {
        for (HashMap.Entry<Clickable, EventEntry> entry : rightReleaseCallbacks.entrySet()) {
            if (!isMouseOverObject(entry.getValue().object, mouseX, mouseY))
                continue;

            if (!isRightButtonDown && wasRightButtonDown)
                entry.getValue().callback.invoke(elapsedTime, mouseX, mouseY);
        }
    }

    private void handleHoverCallbacks(double elapsedTime, double mouseX, double mouseY) {
        for (HashMap.Entry<Clickable, EventEntry> entry : hoverCallbacks.entrySet()) {
            Clickable object = entry.getValue().object;
            // unlike other events, we need to store this object if the mouse is hovering over it.
            if (isMouseOverObject(object, mouseX, mouseY)) {
                currentlyHoveredObject = object;
            }

            if (entry.getValue().onceOnly && currentlyHoveredObject == object && lastHoveredObject == null) {
                entry.getValue().callback.invoke(elapsedTime, mouseX, mouseY);
            } else if (!entry.getValue().onceOnly && currentlyHoveredObject == object) {
                entry.getValue().callback.invoke(elapsedTime, mouseX, mouseY);
            }
        }
    }

    private void handleExitCallbacks(double elapsedTime, double mouseX, double mouseY) {
        for (HashMap.Entry<Clickable, EventEntry> entry : exitCallbacks.entrySet()) {
            Clickable object = entry.getValue().object;
            if (!isMouseOverObject(object, currentMouseX, currentMouseY) && lastHoveredObject == object) {
                entry.getValue().callback.invoke(elapsedTime, mouseX, mouseY);
                currentlyHoveredObject = null;
            }
        }
    }

    private boolean isMouseOverObject(Clickable object, double mouseX, double mouseY) {
        // convert pixel coordinates of the mouse into canvas coordinates that the graphics library can use.
        double mouseX_canvas = ((2 * mouseX) / windowWidth) - 1;
        double mouseY_canvas = 2 * mouseY / windowWidth - (float) windowHeight / windowWidth;

        return object.isMouseOver(mouseX_canvas, mouseY_canvas);
    }
}
