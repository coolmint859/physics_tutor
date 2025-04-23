package views;

import assets.ColorAssets;
import assets.FontAssets;
import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.Texture;
import edu.usu.graphics.objects.Rectangle;
import edu.usu.graphics.objects.Text;
import org.joml.Vector2f;
import org.joml.Vector3f;
import physics.PhysicsObject2D;
import simulation.Simulation;
import utils.*;
import assets.SoundAssets;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class SimulationView implements StateView {
    private final Graphics2D graphics;
    private final SoundAssets audio;

    private Simulation currentSimulation;
    private ArrayList<PhysicsObject2D> physObjects;
    private ArrayList<RadioButton> solutionOptionsText;
    private boolean playSim;
    private String currentSelectedOption;

    private LLMRequest chatgpt;
    private String hint = "Loading...";
    private String submissionResponse = "Loading...";
    private DescriptionPanel hintPanel;
    private DescriptionPanel submitPanel;
    private Text descPanelCloseButton;

    private boolean renderHint;
    private boolean renderSubmitResponse;

    private KeyboardInput keyboard;
    private MouseInput cursor;

    private Text escapeButton;
    private Text playPauseButton;
    private Text resetSimButton;
    private Text hintButton;
    private Text submitButton;
    private final float HUDpanelWidth = 0.6f;
    private Rectangle HUDPanel;
    private Rectangle simPanel;

    private StateEnum nextState;

    public SimulationView(Graphics2D graphics, SoundAssets audio, Simulation defaultSim, String LLM_API_KEY) {
        this.graphics = graphics;
        this.audio = audio;

        this.currentSimulation = defaultSim;

        this.chatgpt = new LLMRequest(this.currentSimulation.description, this.currentSimulation.solutionOptions, LLM_API_KEY);
    }

    @Override
    public void initialize() {
        nextState = StateEnum.Simulation;

        this.descPanelCloseButton = new Text(new Vector3f(), "CLOSE", FontAssets.robotoReg, 0.05f, ColorAssets.menuTextColor);

        this.hintPanel = new DescriptionPanel(new Vector2f(), this.hint, 0.04f, descPanelCloseButton, InfoPanel.TextAlignment.CENTERED);
        this.hintPanel.setTexture(new Texture("./resources/images/simplebg.png"), 0.025f, RenderOrders.HUD2_z);
        this.submitPanel = new DescriptionPanel(new Vector2f(), this.submissionResponse, 0.04f, descPanelCloseButton, InfoPanel.TextAlignment.CENTERED);
        this.submitPanel.setTexture(new Texture("./resources/images/simplebg.png"), 0.025f, RenderOrders.HUD2_z);
        this.renderHint = false;
        this.renderSubmitResponse = false;

        this.physObjects = this.currentSimulation.create();
        this.playSim = false;

        float aspectRatio = (float) this.graphics.getHeight() / this.graphics.getWidth();
        this.HUDPanel = new Rectangle(-1.0f, -aspectRatio, HUDpanelWidth, 2*aspectRatio, RenderOrders.HUD1_z);
        this.simPanel = new Rectangle(-1.0f, aspectRatio - 0.1f, HUDpanelWidth, 0.1f, RenderOrders.HUD2_z);

        this.solutionOptionsText = new ArrayList<>();
        for (int i = 0; i < this.currentSimulation.solutionOptions.size(); i++) {
            String option = this.currentSimulation.solutionOptions.get(i);
            Text text = new Text(new Vector3f(-0.85f, -0.2f + 0.05f*i, 1.0f), option, FontAssets.robotoReg_OL, 0.04f, ColorAssets.simStaticTextColor);
            solutionOptionsText.add(new RadioButton(text));
        }
        this.currentSelectedOption = "";

        this.escapeButton = new Text(new Vector3f(-0.85f, -0.5125f, 1.0f), "BACK (ESC)", FontAssets.robotoReg_OL, 0.06f, ColorAssets.menuEscapeColor);
        this.playPauseButton = new Text(new Vector3f(-0.85f, 0.52f, 1.0f), "PLAY", FontAssets.robotoReg_OL, 0.06f, ColorAssets.simButtonTextColor2);
        this.resetSimButton = new Text(new Vector3f(-0.55f, 0.52f, 1.0f), "RESET", FontAssets.robotoReg_OL, 0.06f, ColorAssets.simButtonTextColor2);
        this.hintButton = new Text(new Vector3f(-0.85f, 0.42f, 1.0f), "HINT", FontAssets.robotoReg_OL, 0.06f, ColorAssets.simButtonTextColor1);
        this.submitButton = new Text(new Vector3f(-0.55f, 0.42f, 1.0f), "SUBMIT", FontAssets.robotoReg_OL, 0.06f, ColorAssets.simButtonTextColor1);

        registerKeyboardCommands();
        registerCursorCommands();
    }

    public void setCurrentSimulation(Simulation sim) {
        this.currentSimulation = sim;
    }

    private void registerKeyboardCommands() {
        keyboard = new KeyboardInput(graphics.getWindow());
        keyboard.registerKeyDown(GLFW_KEY_ESCAPE, true, (double elapsedTime) -> {
            nextState = StateEnum.SimulationSelect;
        });
    }

    private void registerCursorCommands() {
        cursor = new MouseInput(graphics.getWindow(), graphics.getWidth(), graphics.getHeight());
        cursor.setCursorType(GLFW_ARROW_CURSOR);

        // commands for the escape button
        cursor.addHoverListener(escapeButton, true, (double elapsedTime, double x, double y) -> {
            escapeButton.setColor(ColorAssets.menuSelectedColor);
            cursor.setCursorType(GLFW_HAND_CURSOR);
        });
        cursor.addExitListener(escapeButton, (double elapsedTime, double x, double y) -> {
            escapeButton.setColor(ColorAssets.menuEscapeColor);
            cursor.setCursorType(GLFW_ARROW_CURSOR);
        });
        cursor.addLeftClickListener(escapeButton, true, (double elapsedTime, double x, double y) -> {
            nextState = StateEnum.SimulationSelect;
        });

        // commands for the play/pause button
        cursor.addHoverListener(playPauseButton, true, (double elapsedTime, double x, double y) -> {
            playPauseButton.setColor(ColorAssets.simSelectedTextColor2);
            cursor.setCursorType(GLFW_HAND_CURSOR);
        });
        cursor.addExitListener(playPauseButton, (double elapsedTime, double x, double y) -> {
            playPauseButton.setColor(ColorAssets.simButtonTextColor2);
            cursor.setCursorType(GLFW_ARROW_CURSOR);
        });
        cursor.addLeftClickListener(playPauseButton, true, (double elapsedTime, double x, double y) -> {
            this.playSim = !playSim;
            this.playPauseButton.setText(playSim ? "PAUSE" : "PLAY");
        });

        // commands for the reset button
        cursor.addHoverListener(resetSimButton, true, (double elapsedTime, double x, double y) -> {
            resetSimButton.setColor(ColorAssets.simSelectedTextColor2);
            cursor.setCursorType(GLFW_HAND_CURSOR);
        });
        cursor.addExitListener(resetSimButton, (double elapsedTime, double x, double y) -> {
            resetSimButton.setColor(ColorAssets.simButtonTextColor2);
            cursor.setCursorType(GLFW_ARROW_CURSOR);
        });
        cursor.addLeftClickListener(resetSimButton, true, (double elapsedTime, double x, double y) -> {
            this.playSim = false;
            this.playPauseButton.setText("PLAY");
            this.physObjects = this.currentSimulation.create();
        });

        // commands for the hint button
        cursor.addHoverListener(hintButton, true, (double elapsedTime, double x, double y) -> {
            hintButton.setColor(ColorAssets.simSelectedTextColor1);
            cursor.setCursorType(GLFW_HAND_CURSOR);
        });
        cursor.addExitListener(hintButton, (double elapsedTime, double x, double y) -> {
            hintButton.setColor(ColorAssets.simButtonTextColor1);
            cursor.setCursorType(GLFW_ARROW_CURSOR);
        });
        cursor.addLeftClickListener(hintButton, true, (double elapsedTime, double x, double y) -> {
            requestHint();
            this.renderHint = true;
        });

        // commands for the submit button
        cursor.addHoverListener(submitButton, true, (double elapsedTime, double x, double y) -> {
            submitButton.setColor(ColorAssets.simSelectedTextColor1);
            cursor.setCursorType(GLFW_HAND_CURSOR);
        });
        cursor.addExitListener(submitButton, (double elapsedTime, double x, double y) -> {
            submitButton.setColor(ColorAssets.simButtonTextColor1);
            cursor.setCursorType(GLFW_ARROW_CURSOR);
        });
        cursor.addLeftClickListener(submitButton, true, (double elapsedTime, double x, double y) -> {
            // do nothing if student hasn't selected anything
            if (this.currentSelectedOption.isEmpty())
                return;

            requestSubmit();
            this.renderSubmitResponse = true;
        });

        // commands for the hint close button (only displayed if the student requested a hint/ or submitted a response)
        cursor.addHoverListener(descPanelCloseButton, true, (double elapsedTime, double x, double y) -> {
            descPanelCloseButton.setColor(ColorAssets.menuSelectedColor);
            cursor.setCursorType(GLFW_HAND_CURSOR);
        });
        cursor.addExitListener(descPanelCloseButton, (double elapsedTime, double x, double y) -> {
            descPanelCloseButton.setColor(ColorAssets.menuTextColor);
            cursor.setCursorType(GLFW_ARROW_CURSOR);
        });
        cursor.addLeftClickListener(descPanelCloseButton, true, (double elapsedTime, double x, double y) -> {
            this.renderHint = false;
            this.renderSubmitResponse = false;
        });

        for (RadioButton button: this.solutionOptionsText) {
            cursor.addHoverListener(button, true, (double elapsedTime, double x, double y) -> {
                button.hoverOver();
                cursor.setCursorType(GLFW_HAND_CURSOR);
            });
            cursor.addExitListener(button, (double elapsedTime, double x, double y) -> {
                button.exitHover();
                cursor.setCursorType(GLFW_ARROW_CURSOR);
            });
            cursor.addLeftClickListener(button, true, (double elapsedTime, double x, double y) -> {
                this.currentSelectedOption = button.getTextStr();
                button.select(this.solutionOptionsText);
            });
        }
    }

    private void requestHint() {
        this.hint = "Loading...";
        // run the request in a new thread as this is a https request
        Runnable t = () -> {
            String hint;
            do {
                hint = this.chatgpt.requestHint();
            } while (hint == null);

            this.hint = hint;
        };
        Thread thread = new Thread(t);
        thread.start();
    }

    private void requestSubmit() {
        this.submissionResponse = "Loading...";
        // run the request in a new thread, and only if the submission isn't empty
        Runnable t = () -> {
            String subResponse;
            do {
                subResponse = this.chatgpt.requestSubmission(this.currentSelectedOption);
            } while (subResponse == null);

            this.submissionResponse = subResponse;
        };
        Thread thread = new Thread(t);
        thread.start();
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
        for (PhysicsObject2D obj : this.physObjects) {
            obj.update(elapsedTime);
        }
        if (playSim) this.currentSimulation.stepForward(elapsedTime, 10);

        if (this.currentSimulation.simulationStopped()) {
            this.physObjects = this.currentSimulation.create();
            this.playPauseButton.setText("PLAY");
            this.playSim = false;
        }

        // constantly update each frame to make sure we display the response when it arrives
        this.hintPanel.setDescription(this.hint);
        this.submitPanel.setDescription(this.submissionResponse);
    }

    // used to fit the description text onto the display panel
    public ArrayList<String> splitDescription() {
        ArrayList<String> descStrings = new ArrayList<>();
        int descLength = this.currentSimulation.description.length();
        int maxLength = (int) (HUDpanelWidth * 55);
        int lastSplitIndex = 0;
        int currentSplitIndex = 0;

        for (int i = 0; i < descLength; i++) {
            char currentChar = this.currentSimulation.description.charAt(i);
            if (currentChar == ' ' && currentSplitIndex - maxLength >= 0) {
                descStrings.add(this.currentSimulation.description.substring(lastSplitIndex, i));
                lastSplitIndex = i+1;
                currentSplitIndex = 0;
                continue;
            }
            currentSplitIndex += 1;
        }
        if (lastSplitIndex <= descLength) {
            descStrings.add(this.currentSimulation.description.substring(lastSplitIndex, descLength));
        }
        return descStrings;
    }

    @Override
    public void render(double elapsedTime) {
        graphics.setClearColor(currentSimulation.bgColor);

        graphics.draw(HUDPanel, ColorAssets.HUDColor1);
        graphics.draw(simPanel, ColorAssets.HUDColor2);

        if (renderHint) this.hintPanel.render(graphics, FontAssets.robotoReg, RenderOrders.TEXT2_z);
        if (renderSubmitResponse) this.submitPanel.render(graphics, FontAssets.robotoReg, RenderOrders.TEXT2_z);

        escapeButton.draw(graphics);
        playPauseButton.draw(graphics);
        resetSimButton.draw(graphics);
        hintButton.draw(graphics);
        submitButton.draw(graphics);

        for (PhysicsObject2D obj : this.physObjects) {
            obj.render(graphics, elapsedTime);
        }

        InfoPanel panel = new InfoPanel(
                new Vector2f(-1.0f + HUDpanelWidth /2, -0.35f), splitDescription(), InfoPanel.TextAlignment.LEFT,
                Color.BLACK, 0.04f, 0.0f
        );
        panel.render(graphics, FontAssets.robotoReg, RenderOrders.HUD2_z, RenderOrders.TEXT1_z);

        for (RadioButton button : this.solutionOptionsText) {
            button.render(graphics, RenderOrders.HUD2_z);
        }
    }
}
