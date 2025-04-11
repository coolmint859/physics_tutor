package edu.usu.graphics.objects;

/** Graphics objects to be interacted with by the mouse should implement this interface. */
public interface Clickable {
    /** determines if the mouse is hovering over the object. */
    boolean isMouseOver(double mouseX, double mouseY);
}
