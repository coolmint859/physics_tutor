import assets.ColorAssets;
import edu.usu.graphics.Graphics2D;

public class PhysicsTutor {
    public static void main(String[] args) {
//        try (Graphics2D graphics = new Graphics2D(1920, 1080, "Interactive Physics Tutor")) {
        try (Graphics2D graphics = new Graphics2D((int)(1920*0.75), (int)(1080*0.75), "Interactive Physics Tutor")) {
            String LLM_API_KEY;
            if (args.length < 1 || !args[0].startsWith("API_KEY=")) {
                LLM_API_KEY = "";
            } else {
                LLM_API_KEY = args[0].split("=")[1];
            }

            graphics.initialize(ColorAssets.menuBGColor);
            ContextManager context = new ContextManager(graphics);
            context.initialize(LLM_API_KEY);
            context.run();
            context.shutdown();
        }
    }
}