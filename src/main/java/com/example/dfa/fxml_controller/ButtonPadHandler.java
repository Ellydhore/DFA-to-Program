package com.example.dfa.fxml_controller;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

public class ButtonPadHandler {
    private boolean isAnimating = false;

    public void clickAnimation(Pane btn, AudioClip soundFX) {
        if (isAnimating) return;
        isAnimating = true;
        soundFX.play();

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), btn);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), btn);
        translateTransition.setByX(5);
        translateTransition.setByY(10);
        translateTransition.setAutoReverse(true);
        translateTransition.setCycleCount(2);

        scaleTransition.setOnFinished(event -> isAnimating = false);
        translateTransition.setOnFinished(event -> isAnimating = false);

        scaleTransition.play();
        translateTransition.play();
    }
}
