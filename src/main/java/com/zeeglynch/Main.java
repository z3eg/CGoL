package com.zeeglynch;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dmytrocherednyk on 28.12.15.
 */
public class Main extends Application {

    private Stage stage;
    private GridPane pane;
    private Cell[][] cells;
    private byte[][] neighbourCounterBoard;
    private int cellColumnCount = 200;
    private int cellRowCount = 200;
    private Cell tmpCell;
    private Timer timer = new Timer();
    private final long epochDuration = 100;
    private int startingSpecimenCount = 10000;
    private long generationNumber = 0;
    private TextField worldWidthField;
    private TextField worldHeightField;

    EventHandler handler = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {
//            System.out.println("CELL WAS CLICKED!");
            Cell source = (Cell) event.getSource();
            System.out.println();
            source.revert();
        }
    };

    private class Cell extends Button {

        private final Color liveColor = new Color(37 / 255, 1, 33 / 255, 1);
        private final Color deadColor = Color.BLACK;
        private boolean isAlive = false;
        private byte sideSize = 4;
        private byte width = sideSize;
        private byte height = sideSize;

        public Cell() {
            prepare();
        }

        public Cell(byte width, byte height) {
            this.width = width;
            this.height = height;
            prepare();
        }

        private void prepare() {
            this.setMinSize(width, height);
            this.setMaxSize(width, height);
            this.setOnMouseClicked(handler);
        }

        private void repaint(Color newColor) {
            Insets outsets = getBackground().getOutsets();
            setBackground(new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, outsets)));
        }

        private void repaint() {
            repaint(isAlive ? liveColor : deadColor);
        }

        private void revert() {
            isAlive = !isAlive;
            repaint();
        }

        private void applyTheRule(byte liveNeighboursAmount) {
            /*Any live cell with fewer than two live neighbours dies, as if caused by under-population.
            Any live cell with two or three live neighbours lives on to the next generation.
            Any live cell with more than three live neighbours dies, as if by over-population.
            Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
            (c) Wikipedia*/
            if (isAlive) {
                isAlive = (liveNeighboursAmount > 1 && liveNeighboursAmount < 4);
            } else {
                isAlive = (liveNeighboursAmount == 3);
            }
        }
    }

    private void formCounterBoard() {
        for (int i = 0; i < cellRowCount; i++) {
            for (int j = 0; j < cellColumnCount; j++) {
                neighbourCounterBoard[i][j] = 0;
            }
        }
        for (int i = 0; i < cellRowCount; i++) {
            for (int j = 0; j < cellColumnCount; j++) {
                for (int k = i - 1; k < i + 2; k++) {
                    for (int l = j - 1; l < j + 2; l++) {
                        if (k > -1 && l > -1 && k < cells.length && l < cells[0].length) {
                            if (cells[k][l].isAlive) {
                                neighbourCounterBoard[i][j]++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void refresh() {
        formCounterBoard();
        for (int i = 0; i < cellRowCount; i++) {
            for (int j = 0; j < cellColumnCount; j++) {
                cells[i][j].applyTheRule(neighbourCounterBoard[i][j]);
                cells[i][j].repaint();
            }
        }
    }

    private void prepare() {
        cells = new Cell[cellRowCount][cellColumnCount];
        neighbourCounterBoard = new byte[cellRowCount][cellColumnCount];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                tmpCell = new Cell();
                tmpCell.prepare();
                cells[i][j] = tmpCell;
                GridPane.setConstraints(cells[i][j], i, j);
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        prepare();
        stage = primaryStage;
        stage.setTitle("Conway's game of life");
        pane = new GridPane();
        for (Cell[] row : cells) {
            pane.getChildren().addAll(Arrays.asList(row));
        }

        Button startButton = new Button("Start");
        startButton.setOnMouseClicked(event -> {

                    timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("GENERATION NUMBER: " + generationNumber);
                            generationNumber++;
                            refresh();
                        }
                    }, epochDuration, epochDuration);
                }
        );
        GridPane.setConstraints(startButton, 0, cellRowCount + 1, cellColumnCount / 3, 1);
        pane.getChildren().add(startButton);
        Button pauseButton = new Button("Pause");
        pauseButton.setOnMouseClicked(event -> {
            timer.cancel();
        });
        GridPane.setConstraints(pauseButton, cellColumnCount / 3, cellRowCount + 1, cellColumnCount / 3, 1);
        pane.getChildren().add(pauseButton);
        Button randomizeButton = new Button("Randomize");
        randomizeButton.setOnMouseClicked(event -> initTheField());
        GridPane.setConstraints(randomizeButton, 2*cellColumnCount / 3, cellRowCount + 1, cellColumnCount / 3, 1);
        pane.getChildren().add(randomizeButton);
        TextField speciesAmountField = new TextField(Integer.toString(startingSpecimenCount));
        worldWidthField = new TextField(Integer.toString(cellColumnCount));
        worldHeightField = new TextField(Integer.toString(cellRowCount));
        GridPane.setConstraints(speciesAmountField,  0, cellRowCount + 2, cellColumnCount / 3, 1);
        GridPane.setConstraints(worldWidthField,  cellColumnCount / 3, cellRowCount + 2, cellColumnCount / 3, 1);
        GridPane.setConstraints(worldHeightField,  2*cellColumnCount / 3, cellRowCount + 2, cellColumnCount / 3, 1);
        pane.getChildren().add(speciesAmountField);
        pane.getChildren().add(worldWidthField);
        pane.getChildren().add(worldHeightField);
        Label speciesAmountLabel = new Label("Species amount");
        Label worldWidthLabel = new Label("World Width");
        Label worldHeightLabel = new Label("World Height");
        GridPane.setConstraints(speciesAmountLabel,  0, cellRowCount + 3, cellColumnCount / 3, 1);
        GridPane.setConstraints(worldWidthLabel,  cellColumnCount / 3, cellRowCount + 3, cellColumnCount / 3, 1);
        GridPane.setConstraints(worldHeightLabel,  2*cellColumnCount / 3, cellRowCount + 3, cellColumnCount / 3, 1);
        pane.getChildren().add(speciesAmountLabel);
        pane.getChildren().add(worldWidthLabel);
        pane.getChildren().add(worldHeightLabel);
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        primaryStage.show();
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                cell.revert();
                cell.revert();
            }
        }
    }

    private void das() {

    }

    private void initTheField() {
        clearTheField();
        Random random = new Random();
        for (int i = 0; i < startingSpecimenCount; i++) {
            int xPos = random.nextInt(cellColumnCount);
            int yPos = random.nextInt(cellRowCount);
            cells[yPos][xPos].isAlive = true;
        }
        repaint();
    }

    private void repaint() {
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                cell.repaint();
            }
        }
    }

    private void clearTheField() {
        for (int i = 0; i < cellRowCount; i++) {
            for (int j = 0; j < cellColumnCount; j++) {
                cells[i][j].isAlive = false;
                neighbourCounterBoard[i][j] = 0;
                generationNumber=0;
            }
        }
    }

}
