package asteroid;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Aydın Bulut (ab@aydinbulut.com)
 */
public class AsteroidsApp extends Application {

    private static Stage window;

    private static AnimationTimer timer;

    private static Pane gameRoot, spacePane; // game pane and space pane

    private static GameObject spaceShip; // spaceship

    static Label scoreLabel, ammoLabel; // score and ammo label

    static VBox gameResult; // gameResult info holder

    static MediaPlayer IntroSound;

    static Timer linearTimer;

    private static List<GameObject> bullets = new ArrayList<>();
    private static List<GameObject> enemies = new ArrayList<>();

    private static BackgroundImage getSpaceBackgroundImage() {
        return new BackgroundImage(
                new Image("./assets/spaceBackground.png"),
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(
                        BackgroundSize.AUTO,
                        BackgroundSize.AUTO,
                        true,
                        false,
                        true,
                        false
                )
        );
    }

    private static void prepareTheGame() {

        // clear bullets and asteroids (enemies)
        bullets.clear();
        enemies.clear();

        // reset score
        Game.destroyed_target = 0;

        // reset te health
        Game.healthValue = 100;

        // reset ammo
        Game.ammo = 50;

        window.setScene(new Scene(createGameContent())); // create game scene

        window.getScene().setOnKeyPressed(e -> {

            // stop progress if game is paused
            if (!Game.isCreated() || Game.isPaused() || Game.isStoped())
                return;

            Bullet bullet; // buller instance

            // shift the space ship to left
            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.A) {
                spaceShip.shiftLeft();
            }

            // shift the space ship to right
            else if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.D) {
                spaceShip.shiftRight(gameRoot.getWidth());
            }

            // fire 1 bullet if press DIGIT1
            else if (e.getCode() == KeyCode.DIGIT1 || e.getCode() == KeyCode.NUMPAD1) {
                if (Game.outOfAmmo())
                    return;

                bullet = new Bullet();
                bullet.setVelocity(new Point2D(0, bullet.velocity));
                addBullet(bullet, bullet.getCenterPosition(spaceShip), spaceShip.getView().getTranslateY());

                Game.descreaseAmmoBy(1);
            }

            // fire 2 bullet if pressed DIGIT2
            else if (e.getCode() == KeyCode.DIGIT2 || e.getCode() == KeyCode.NUMPAD2) {

                // prevent firing if there is no ammo
                if (Game.outOfAmmo(true))
                    return;

                bullet = new Bullet();
                bullet.setVelocity(new Point2D(0, bullet.velocity));
                addBullet(bullet, bullet.getLeftPosition(spaceShip), spaceShip.getView().getTranslateY() + 25);
                bullet = new Bullet();
                bullet.setVelocity(new Point2D(0, bullet.velocity));
                addBullet(bullet, bullet.getRightPosition(spaceShip), spaceShip.getView().getTranslateY() + 25);

                Game.descreaseAmmoBy(2);
            }
        });
        window.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                // if window minimized
                if (window.getX() == -32000.0) {
                    if (Game.isCreated() && Game.isStarted()) {
                        Game.pause();
                    }
                }
                // if window maximized
                else {
                    if (Game.isCreated() && Game.isPaused()) {
                        Game.resume();
                    }
                }
            }
        });

        Game.created(); // signal that game is created to whom it may concerne

        linearTimer = new Timer();
        TimerTask increaseAmmo = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (!Game.isStoped()) {
                        Game.increaseHealtPerSecond();
                        Game.increaseAmmoPerSecond();
                    }
                });
            }
        };
        linearTimer.schedule(increaseAmmo, 1000, 1000);
    }

    private static Button getStartButton() {
        return new Button("START THE GAME", new ImageView(new Image("./assets/play-store.png")));
    }

    private static Button getReStartButton() {
        return new Button("PLAY AGAIN", new ImageView(new Image("./assets/play-store.png")));
    }

    private static Label descLabel(String text) {

        Label label = new Label(text);
        label.setTextFill(Color.rgb(238, 238, 238));
        label.setStyle("-fx-font-weight:bold");

        return label;
    }

    private Parent createEntryContent() {
        Pane entryRoot = new Pane();
        entryRoot.setPrefSize(600, 600);

        VBox descVbox = new VBox();
        descVbox.setTranslateX(128.5);
        descVbox.setTranslateY(100);
        descVbox.setAlignment(Pos.CENTER);

        ImageView title = new ImageView(new Image("./assets/asteroıd-game-title.png"));

        Label shiftLeft = descLabel("Sola Kaydırmak içi A veya Sol Ok");
        Label shiftRight = descLabel("Sağa Kaydırmak içi D veya Sağ Ok");
        Label signleFire = descLabel("Tekli atış için 1");
        Label doubleFire = descLabel("İkili atış için 2");
        descVbox.getChildren().addAll(title, shiftLeft, shiftRight, signleFire, doubleFire);

        Label author = descLabel("Developed By: AYDIN BULUT - Student No: 172802059, MCBU");
        author.setTextFill(Color.rgb(180, 180, 180));
        VBox authorVbox = new VBox();
        authorVbox.setTranslateX(20);
        authorVbox.setTranslateY(560);
        authorVbox.setAlignment(Pos.CENTER);
        authorVbox.getChildren().add(author);

        // start button
        Button start = getStartButton();
        start.setTranslateX(235);
        start.setTranslateY(380);
        start.setStyle("-fx-font-weight: bold");
        start.setCursor(Cursor.HAND);
        start.setOnAction((ActionEvent d) -> prepareTheGame());

        // create background image
        BackgroundImage bgImg = getSpaceBackgroundImage();

        spacePane = new Pane();
        spacePane.setPrefSize(600, 610);
        spacePane.setBackground(new Background(bgImg));
        spacePane.setTranslateX(0);
        spacePane.setTranslateY(0);

        entryRoot.getChildren().addAll(spacePane, descVbox, start, authorVbox);

        Media music = new Media(new File("./src/assets/intro-sound.mp3").toURI().toString());
        IntroSound = new MediaPlayer(music);
        IntroSound.setAutoPlay(true);
        IntroSound.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                IntroSound.seek(Duration.ZERO);
            }
        });

        return entryRoot;
    }

    private static Parent createGameContent() {
        gameRoot = new Pane();
        gameRoot.setPrefSize(600, 600);

        // create background image
        BackgroundImage bgImg = getSpaceBackgroundImage();

        spacePane = new Pane();
        spacePane.setPrefSize(600, 3600);
        spacePane.setBackground(new Background(bgImg));
        spacePane.setTranslateX(0);
        spacePane.setTranslateY(-3000);

        ammoLabel = descLabel("");
        Game.updateAmmoLabel(); // will set text
        ammoLabel.setTranslateX(520);
        ammoLabel.setTranslateY(570);

        scoreLabel = descLabel("");
        Game.updateScoreLabel(); // will set text
        scoreLabel.setTranslateX(20);
        scoreLabel.setTranslateY(570);

        Game.healthPane.setTranslateX(180);
        Game.healthPane.setTranslateY(572);

        gameRoot.getChildren().addAll(spacePane, ammoLabel, Game.healthPane, scoreLabel);

        spaceShip = new Player();
        spaceShip.setVelocity(new Point2D(1, 0));
        spaceShip.setWidth(64); // width of space ship image
        addGameObject(spaceShip, 268, 500);

        IntroSound.setVolume(.2);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                onUpdate();
            }
        };
        timer.start();

        Game.start(); // signal to inform recievers the game is status

        return gameRoot;
    }

    private static void addBullet(GameObject bullet, double x, double y) {
        bullets.add(bullet);
        addGameObject(bullet, x, y);
    }

    private static void addEnemy(GameObject enemy, double x, double y) {
        enemies.add(enemy);
        addGameObject(enemy, x, y);
    }

    private static void addGameObject(GameObject object, double x, double y) {
        object.getView().setTranslateX(x);
        object.getView().setTranslateY(y);
        gameRoot.getChildren().add(object.getView());
    }

    private static void onUpdate() {

        // set enenmies dead that asteroids out of gameview
        for (GameObject enemy : enemies) {
            if (enemy.getView().getTranslateY() > 600) {
                enemy.setAlive(false);
            }
        }

        // detect crash
        for (GameObject enemy : enemies) {
            if (spaceShip.isColliding(enemy)) {
                enemy.setAlive(false);

                Game.descreaseHealtForCrash(); // decrease healt

                gameRoot.getChildren().remove(enemy.getView());

                Game.finishIfShipIsCrashed();
            }
        }

        // detect collision
        for (GameObject bullet : bullets) {
            for (GameObject enemy : enemies) {
                if (bullet.isColliding(enemy) && enemy.getView().getTranslateY() > -28) {
                    bullet.setAlive(false);
                    enemy.setAlive(false);

                    Game.increaseScore();

                    gameRoot.getChildren().removeAll(bullet.getView(), enemy.getView());
                }
            }
        }

        // remove dead bullets and asteroids (enemies)
        bullets.removeIf(GameObject::isDead);
        enemies.removeIf(GameObject::isDead);

        // update location of each bullets and asteroids (enemies)
        bullets.forEach(GameObject::update);
        enemies.forEach(GameObject::update);

        // spawn new asteroids (enemies)
        if (Math.random() < 0.02) {
            Enemy enemy = new Enemy();
            enemy.setVelocity(new Point2D(0, .8));
            addEnemy(enemy, Math.random() * gameRoot.getPrefWidth(), Math.random() * (-1 * gameRoot.getPrefHeight() / 2));
        }

        // move space background gradually
        spacePane.setTranslateY(spacePane.getTranslateY() + 0.2);

    }

    private static class Game {
        private static boolean created = false;
        private static int status = 0;
        private static int ammo = 50;
        private static int increaseAmmoPerSecond = 3;
        private static int destroyed_target = 0;
        private static int increaseHealtByPerSecond = 2;
        private static int descreaseHealtByOnCrash = 20;
        private static int healthValue = 100;
        private static Pane healthPane = new Pane(healtBar());

        static boolean isCreated() {
            return created;
        }

        static void created() {
            created = true;
        }

        static void start() {
            status = 1;
        }

        static boolean isStarted() {
            return status == 1;
        }

        static void pause() {
            status = 2;
            timer.stop();
        }

        static boolean isPaused() {
            return status == 2;
        }

        static void resume() {
            status = 1;
            timer.start();
        }

        static void stop() {
            status = 3;
            timer.stop();
        }

        static boolean isStoped() {
            return status == 3;
        }

        static boolean outOfAmmo() {
            return ammo <= 0;
        }

        static boolean outOfAmmo(boolean multiBullet) {
            return ammo <= 1;
        }

        static void increaseAmmoPerSecond() {
            ammo += Game.increaseAmmoPerSecond;
            updateAmmoLabel();
        }

        static void descreaseAmmoBy(int subtract) {
            ammo -= subtract;
            updateAmmoLabel();
        }

        static void updateAmmoLabel() {
            ammoLabel.setText("AMMO: " + ammo);

            if (ammo < 10) {
                ammoLabel.setStyle("-fx-text-fill: red");
            } else {
                ammoLabel.setStyle(null);
            }
        }

        static void updateScoreLabel() {
            scoreLabel.setText("DESTROYED TARGET: " + getDesroyedTarget());
        }

        static int getDesroyedTarget() {
            return destroyed_target;
        }

        static void increaseScore() {
            destroyed_target += 1;
            updateScoreLabel();
        }

        private static Rectangle healtBar() {
            return new Rectangle((300 / 100) * Game.healthValue, 10, Color.web("#00FF00"));
        }

        static void setHealth() {
            Game.healthPane.getChildren().remove(0);
            Game.healthPane.getChildren().add(healtBar());
        }

        static void increaseHealtPerSecond() {
            healthValue = Math.min(healthValue + increaseHealtByPerSecond, 100); // get 100 if new healt value is over 100
            setHealth();
        }

        static void descreaseHealtForCrash() {
            healthValue = Math.max(healthValue - descreaseHealtByOnCrash, 0); // get 0 if new healt value is under 0
            setHealth();
        }

        static void finishIfShipIsCrashed() {
            if (shipIsCrashed()) {
                stop();

                linearTimer.cancel();

                sowResult();
            }
        }

        private static void sowResult() {

            gameResult = new VBox(10);

            ImageView game_over = new ImageView(new Image("./assets/game-over.png"));

            Label score = descLabel("DESTROYED TARGET: " + getDesroyedTarget());
            Button start = getReStartButton();
            start.setOnAction((ActionEvent e) -> prepareTheGame());


            gameResult.getChildren().addAll(game_over, score, start);

            gameResult.setTranslateX(176);
            gameResult.setTranslateY(200);
            gameResult.setAlignment(Pos.CENTER);

            gameRoot.getChildren().add(gameResult);


        }

        private static boolean shipIsCrashed() {
            return healthValue <= 0;
        }
    }

    private static class Player extends GameObject {
        Player() {
            super(new ImageView(new Image("./assets/spacecraft.png")));

            setWidth(64);
        }
    }

    private static class Enemy extends GameObject {
        Enemy() {
            super(new ImageView(new Image("./assets/meteorite.png")));
        }
    }

    private static class Bullet extends GameObject {

        private int velocity = -15;
        private int size = 4;

        Bullet() {
            super(new Circle(4, 4, 4, Color.web("#55FF55")));
        }

        double getLeftPosition(GameObject spaceShip) {
            return spaceShip.getView().getTranslateX() + 0;
        }

        double getCenterPosition(GameObject spaceShip) {
            return spaceShip.getView().getTranslateX() + ((spaceShip.getWidth() - Bullet.this.size) / 2) - 2;
        }

        double getRightPosition(GameObject spaceShip) {
            return spaceShip.getView().getTranslateX() + spaceShip.getWidth() - Bullet.this.size * 2;
        }

    }

    @Override
    public void start(Stage stage) throws Exception {
        window = stage;

        window.setTitle("ASTEROID GAME");
        window.getIcons().add(new Image("./assets/spacecraft.png"));
        window.setScene(new Scene(createEntryContent()));
        window.setWidth(600);
        window.setHeight(630);
        window.setResizable(false);
        window.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
