package views;

public interface StateView {
    void initialize();

    StateEnum processInput(double elapsedTime);

    void update(double elapsedTime);

    void render(double elapsedTime);
}
