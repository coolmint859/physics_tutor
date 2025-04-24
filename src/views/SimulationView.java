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
    private final float aspectRatio;
    private final SoundAssets audio;

    private Simulation currentSimulation;
    private ArrayList<String> simDescription;
    private ArrayList<PhysicsObject2D> physObjects;
    private ArrayList<RadioButton> solutionOptionsText;
    private boolean playSim;
    private String currentSelectedOption;

    private LLMRequest chatgpt;
    private String hint = "Loading...";
    private String submissionResponse = "Loading...";
    private DescriptionPanel hintPanel;
    private DescriptionPanel submitPanel;
    private Text responseCloseButton;

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
    private InfoPanel descTextPanel;

    private StateEnum nextState;

    Texture cannon;
    Rectangle cannonRect;

    public SimulationView(Graphics2D graphics, SoundAssets audio, Simulation defaultSim, String LLM_API_KEY) {
        this.graphics = graphics;
        this.aspectRatio = (float) graphics.getHeight()/graphics.getWidth();
        this.audio = audio;

        this.currentSimulation = defaultSim;

        this.chatgpt = new LLMRequest(LLM_API_KEY);
    }

    @Override
    public void initialize() {
        nextState = StateEnum.Simulation;

        // this is a kinda poor way of doing this. Ideally, you could specify in the simulation schema "virtual" objects that should
        // just be rendered, but not actually used by the physics engine. This way they don't actually interact with anything.
        // maybe in the future I'll add that ability.
        this.cannon = new Texture("./resources/images/cannon.png");
        this.cannonRect = new Rectangle(-0.29f, -0.045f, 0.055f, 0.055f, 1.0f);

        float descTextHeight = 0.04f;
        float buttonTextHeight = 0.06f;

        this.simDescription = splitDescription();
        float descPanelCenterX = -1.0f + HUDpanelWidth /2;
        float descPanelCenterY = -aspectRatio + (simDescription.size() * descTextHeight)/2 + buttonTextHeight * 1.5f;
        Vector2f descPanelCenter = new Vector2f(descPanelCenterX, descPanelCenterY);

        float optionTextInitCenterY = descPanelCenterY + (simDescription.size() * descTextHeight)/2 + descTextHeight/2 + 0.02f;
        float optionTextLeftAlignment = -0.95f;

        this.solutionOptionsText = new ArrayList<>();
        for (int i = 0; i < this.currentSimulation.solutionOptions.size(); i++) {
            String option = this.currentSimulation.solutionOptions.get(i);
            float optionLength = FontAssets.robotoReg.measureTextWidth(option, descTextHeight);

            float optionTextCenterX = optionTextLeftAlignment + optionLength/2 + 0.022f;
            float optionTextCenterY = optionTextInitCenterY + (2 * i * descTextHeight/2);
            Vector3f optionTextCenter = new Vector3f(optionTextCenterX, optionTextCenterY, RenderOrders.TEXT1_z);

            Text text = new Text(optionTextCenter, option, FontAssets.robotoReg, 0.04f, ColorAssets.simStaticTextColor);
            solutionOptionsText.add(new RadioButton(text));
        }
        this.currentSelectedOption = "";

        this.descTextPanel = new InfoPanel(
                descPanelCenter, this.simDescription, InfoPanel.TextAlignment.LEFT,
                Color.BLACK, descTextHeight, 0.0f
        );

        this.chatgpt.createPrompt(this.currentSimulation.description, this.currentSimulation.solutionOptions);
        this.physObjects = this.currentSimulation.create();
        this.playSim = false;

        this.responseCloseButton = new Text(new Vector3f(), "CLOSE", FontAssets.robotoReg, 0.05f, ColorAssets.menuTextColor);

        this.hintPanel = new DescriptionPanel(new Vector2f(), this.hint, 0.04f, responseCloseButton, InfoPanel.TextAlignment.CENTERED);
        this.hintPanel.setTexture(new Texture("./resources/images/simplebg.png"), 0.025f, RenderOrders.HUD2_z);
        this.submitPanel = new DescriptionPanel(new Vector2f(), this.submissionResponse, 0.04f, responseCloseButton, InfoPanel.TextAlignment.CENTERED);
        this.submitPanel.setTexture(new Texture("./resources/images/simplebg.png"), 0.025f, RenderOrders.HUD2_z);
        this.renderHint = false;
        this.renderSubmitResponse = false;

        float aspectRatio = (float) this.graphics.getHeight() / this.graphics.getWidth();
        this.HUDPanel = new Rectangle(-1.0f, -aspectRatio, HUDpanelWidth, 2*aspectRatio, RenderOrders.HUD1_z);
        this.simPanel = new Rectangle(-1.0f, aspectRatio - 0.1f, HUDpanelWidth, 0.1f, RenderOrders.HUD2_z);

        this.escapeButton = new Text(new Vector3f(-0.85f, -0.5125f, RenderOrders.TEXT2_z), "BACK (ESC)", FontAssets.robotoReg_OL, buttonTextHeight, ColorAssets.menuEscapeColor);
        this.playPauseButton = new Text(new Vector3f(-0.85f, 0.52f, RenderOrders.TEXT2_z), "PLAY", FontAssets.robotoReg_OL, buttonTextHeight, ColorAssets.simButtonTextColor2);
        this.resetSimButton = new Text(new Vector3f(-0.55f, 0.52f, RenderOrders.TEXT2_z), "RESET", FontAssets.robotoReg_OL, buttonTextHeight, ColorAssets.simButtonTextColor2);
        this.hintButton = new Text(new Vector3f(-0.85f, 0.42f, RenderOrders.TEXT2_z), "HINT", FontAssets.robotoReg_OL, buttonTextHeight, ColorAssets.simButtonTextColor1);
        this.submitButton = new Text(new Vector3f(-0.55f, 0.42f, RenderOrders.TEXT2_z), "SUBMIT", FontAssets.robotoReg_OL, buttonTextHeight, ColorAssets.simButtonTextColor1);

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
        cursor.addHoverListener(responseCloseButton, true, (double elapsedTime, double x, double y) -> {
            responseCloseButton.setColor(ColorAssets.menuSelectedColor);
            cursor.setCursorType(GLFW_HAND_CURSOR);
        });
        cursor.addExitListener(responseCloseButton, (double elapsedTime, double x, double y) -> {
            responseCloseButton.setColor(ColorAssets.menuTextColor);
            cursor.setCursorType(GLFW_ARROW_CURSOR);
        });
        cursor.addLeftClickListener(responseCloseButton, true, (double elapsedTime, double x, double y) -> {
            this.renderHint = false;
            this.renderSubmitResponse = false;
        });

        for (RadioButton button: this.solutionOptionsText) {
            cursor.addHoverListener(button, true, (double elapsedTime, double x, double y) -> {
                button.setFont(FontAssets.robotoBold);
                button.hoverOver();
                cursor.setCursorType(GLFW_HAND_CURSOR);
            });
            cursor.addExitListener(button, (double elapsedTime, double x, double y) -> {
                button.setFont(FontAssets.robotoReg);
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
        graphics.setClearColor(this.currentSimulation.bgColor);

        // Reiterating what was said above, ideally in the simulation schema you could specify objects that are
        // just rendered, and don't interact with the physics engine. That way checks like these aren't needed.
        if (this.currentSimulation.name.equals("Cannon Ball"))
            graphics.draw(cannon, cannonRect, Color.WHITE);

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


        this.descTextPanel.render(graphics, FontAssets.robotoReg, RenderOrders.HUD2_z, RenderOrders.TEXT1_z);

        for (RadioButton button : this.solutionOptionsText) {
            button.render(graphics, RenderOrders.HUD2_z);
        }
    }
}
