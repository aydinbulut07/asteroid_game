package asteroid;

import javafx.geometry.Point2D;
import javafx.scene.Node;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class GameObject {

    private Node view;
    private Point2D velocity = new Point2D(0, 0);

    private boolean alive = true;

    private int shiftSpeed = 5;
    private int width = 0;

    public GameObject(Node view) {
        this.view = view;
    }

    public void update() {
        view.setTranslateX(view.getTranslateX() + velocity.getX());
        view.setTranslateY(view.getTranslateY() + velocity.getY());
    }

    public void setVelocity(Point2D velocity) {
        this.velocity = velocity;
    }
    
    public Node getView() {
        return view;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isDead() {
        return !alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public double getWidth() {
        return width;
    }

    public void shiftLeft() {
        double leftBoundary = view.getTranslateX() - shiftSpeed;

        if (leftBoundary >= -25)
            view.setTranslateX(leftBoundary);
    }

    public void shiftRight(double rootWidth) {
        double rightBoundary = view.getTranslateX() + shiftSpeed;

        if (rightBoundary <= rootWidth - getWidth() + 25)
            view.setTranslateX(view.getTranslateX() + shiftSpeed);
    }

    public boolean isColliding(GameObject other) {
        return getView().getBoundsInParent().intersects(other.getView().getBoundsInParent());
    }
}
