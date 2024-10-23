package com.example.dfa.fxml_controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameController implements Initializable {
    @FXML
    private Pane btn_srfc_1, btn_srfc_2, btn_srfc_3, btn_srfc_4, btn_srfc_5, btn_srfc_6, btn_srfc_7, btn_srfc_8, btn_srfc_9,
            btn_start, btn_restart, btn_validate;

    @FXML
    private Circle led_indicator;

    @FXML
    private Text txt_message;

    private boolean isStarting = false;
    private AudioClip clickSound1, clickSound2, music, door_locked, door_unlocked;
    private ButtonPadHandler buttonPadHandler;
    private boolean accepted = false;

    // Time element
    private Timeline countdownTimer;
    private int timeRemaining = 10;

    // DFA TABLE
    private final int[][] transitionTable = {
            {0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 0, 0, 0, 0, 0, 2, 0},
            {0, 1, 0, 3, 0, 0, 0, 0, 0},
            {4, 1, 0, 0, 0, 0, 0, 0, 0},
            {4, 4, 4, 4, 4, 4, 4, 4, 4}
    };
    private final int acceptingState = 4;
    private int currentState = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load custom font
        loadCustomFont("src/main/resources/com/example/dfa/fonts/digital-7.ttf");
        led_indicator.setId("led-indicator");
        // Initialize buttons and sounds
        Pane[] buttons = getButtonsArray();
        List<String> imagePaths = getImagePaths();
        initializeSounds();

        // Handle buttons
        buttonPadHandler = new ButtonPadHandler();
        setupButtonClickHandlers(buttons);

        // Start and validate button click logic
        setupGameControlHandlers(imagePaths, buttons);
    }

    private Pane[] getButtonsArray() {
        return new Pane[]{
                btn_srfc_1, btn_srfc_2, btn_srfc_3, btn_srfc_4,
                btn_srfc_5, btn_srfc_6, btn_srfc_7, btn_srfc_8, btn_srfc_9
        };
    }

    private List<String> getImagePaths() {
        return Arrays.asList(
                "src/main/resources/com/example/dfa/image/shape1.png",
                "src/main/resources/com/example/dfa/image/shape2.png",
                "src/main/resources/com/example/dfa/image/shape3.png",
                "src/main/resources/com/example/dfa/image/shape4.png",
                "src/main/resources/com/example/dfa/image/shape5.png",
                "src/main/resources/com/example/dfa/image/shape6.png",
                "src/main/resources/com/example/dfa/image/shape7.png",
                "src/main/resources/com/example/dfa/image/shape8.png",
                "src/main/resources/com/example/dfa/image/shape9.png"
        );
    }

    private void initializeSounds() {
        clickSound1 = loadAudioClip("src/main/resources/com/example/dfa/audio/btn_click1.mp3");
        clickSound2 = loadAudioClip("src/main/resources/com/example/dfa/audio/btn_click2.mp3");
        door_unlocked = loadAudioClip("src/main/resources/com/example/dfa/audio/door_locked.mp3");
        door_locked = loadAudioClip("src/main/resources/com/example/dfa/audio/door_unlocked.mp3");
        music = loadAudioClip("src/main/resources/com/example/dfa/audio/60sec_countdown.mp3");
    }

    private AudioClip loadAudioClip(String filePath) {
        try {
            URL soundURL = new File(filePath).toURI().toURL();
            return new AudioClip(soundURL.toString());
        } catch (MalformedURLException e) {
            System.err.println("Failed to load audio: " + filePath);
            return null;
        }
    }

    private void loadCustomFont(String fontPath) {
        Font.loadFont(getClass().getResourceAsStream(fontPath), 24);
        txt_message.setStyle("-fx-font-family: 'digital-7'");
    }

    private void setupButtonClickHandlers(Pane[] buttons) {
        for (Pane button : buttons) {
            button.setOnMouseClicked(event -> {
                buttonPadHandler.clickAnimation(button, clickSound1);
                handleImageViewClick(button);
            });
        }
    }

    private void handleImageViewClick(Pane button) {
        for (Node node : button.getChildren()) {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                String imageUrl = imageView.getImage().getUrl();
                if (imageUrl != null) {
                    int extractedNumber = checkInput(imageUrl);
                    accepted = checkTransition(extractedNumber);
                    System.out.println(accepted ? "Accepted" : "Rejected" + " input from image: " + imageUrl);
                }
            }
        }
    }

    private void setupGameControlHandlers(List<String> imagePaths, Pane[] buttons) {
        btn_start.setOnMouseClicked(event -> {
            if (!isStarting) {
                buttonPadHandler.clickAnimation(btn_start, clickSound2);
                isStarting = true;
                startGame(imagePaths, buttons, music);
            }
        });

        btn_restart.setOnMouseClicked(event -> {
            buttonPadHandler.clickAnimation(btn_restart, clickSound2);
            led_indicator.setId("led-indicator");
            resetGame(buttons, txt_message);
        });

        btn_validate.setOnMouseClicked(event -> {
            buttonPadHandler.clickAnimation(btn_validate, clickSound2);
            if (accepted) {
                countdownTimer.stop();
                led_indicator.setId("led-indicator-win");
                door_locked.play();
                txt_message.setText("DOOR UNLOCKED!");
                handleGameOver();
                music.stop();
                countdownTimer = null;
            }
        });
    }

    public void startGame(List<String> imagePaths, Pane[] buttons, AudioClip soundFX) {
        timeRemaining = 10;
        Collections.shuffle(imagePaths);
        distributeImagesToButtons(imagePaths, buttons);
        soundFX.play();
        startCountdownTimer(txt_message);
    }

    private void distributeImagesToButtons(List<String> imagePaths, Pane[] buttons) {
        for (int i = 0; i < buttons.length; i++) {
            String imagePath = imagePaths.get(i);
            try {
                Image image = new Image(new File(imagePath).toURI().toURL().toString());
                ImageView imageView = new ImageView(image);
                setImageViewProperties(buttons[i], imageView);
                buttons[i].setDisable(false);
                btn_validate.setDisable(false);
            } catch (MalformedURLException e) {
                System.err.println("Failed to load image: " + imagePath);
            }
        }
    }

    private void setImageViewProperties(Pane button, ImageView imageView) {
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(button.getPrefWidth() - 40);
        imageView.setFitHeight(button.getPrefHeight() - 40);
        imageView.setLayoutX((button.getPrefWidth() - imageView.getFitWidth()) / 2);
        imageView.setLayoutY((button.getPrefHeight() - imageView.getFitHeight()) / 2);
        button.getChildren().add(imageView);
    }

    public boolean checkTransition(int input) {
        if (input < 1 || input > 9) {
            System.out.println("Invalid input: " + input);
            return false;
        }
        currentState = transitionTable[currentState][input - 1];
        return currentState == acceptingState;
    }

    public int checkInput(String url) {
        Matcher matcher = Pattern.compile("shape(\\d+)\\.png").matcher(url);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    private void startCountdownTimer(Text txt_message) {
        // Reset time remaining
        timeRemaining = 10;

        // Countdown timer
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeRemaining--;
            txt_message.setText("TIME LEFT: " + timeRemaining);

            if (timeRemaining <= 0) {
                countdownTimer.stop();
                led_indicator.setId("led-indicator-lose");
                music.stop();
                door_unlocked.play();
                txt_message.setText("GAME OVER!");
                handleGameOver();

                countdownTimer = null;
            }
        }));

        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    public void resetGame(Pane[] buttons, Text txt_message) {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        currentState = 0;
        accepted = false;
        isStarting = false;

        txt_message.setText("");

        for (Pane button : buttons) {
            button.getChildren().clear();
        }

        if (music.isPlaying()) {
            music.stop();
        }

        System.out.println("Game has been reset.");
    }

    private void handleGameOver() {
        // Disable buttons
        for (Pane button : new Pane[]{btn_srfc_1, btn_srfc_2, btn_srfc_3, btn_srfc_4, btn_srfc_5, btn_srfc_6, btn_srfc_7, btn_srfc_8, btn_srfc_9, btn_validate}) {
            button.setDisable(true);
        }
    }
}
