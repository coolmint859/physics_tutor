import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;

public class PhysicsTutor {
    public static void main(String[] args) {
//        try (Graphics2D graphics = new Graphics2D(1920, 1080, "Interactive Physics Tutor")) {
        try (Graphics2D graphics = new Graphics2D((int)(1920*0.75), (int)(1080*0.75), "Interactive Physics Tutor")) {
            graphics.initialize(Color.CORNFLOWER_BLUE);
            StateManager stateManager = new StateManager(graphics);
            stateManager.initialize();
            stateManager.run();
            stateManager.shutdown();
        }
    }
}