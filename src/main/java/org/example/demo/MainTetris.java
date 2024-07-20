package org.example.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainTetris {

    @FXML
    private GridPane tetrisBack;
    private Piece currentPiece;
    private int points = 0;
    private int lines = 0;
    private Timeline timeline;
    private ArrayList<ArrayList<Boolean>> isOccupied;
    @FXML
    private Label point;
    @FXML
    private Label line;
    private double fallinterval=1;
    private boolean gameEnd=false;

    public void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(fallinterval), event -> {
            movePieceDown();
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void stopTimer() {
        if (timeline != null) {
            timeline.stop();
        }
    }
    @FXML
    public void initialize() {
        isOccupied = new ArrayList<>();
        for (int i = 0; i < tetrisBack.getRowCount(); i++) {
            ArrayList<Boolean> row = new ArrayList<>(Collections.nCopies(tetrisBack.getColumnCount(), false));
            isOccupied.add(row);
        }
        createAndPlacePiece();

        tetrisBack.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            switch (key) {
                case DOWN:
                    movePieceDown();
                    break;
                case SPACE:
                    spacePieceDown();
                    break;
                case LEFT:
                    movePieceLeft();
                    break;
                case RIGHT:
                    movePieceRight();
                    break;
                case R: // Tecla para rotar a la izquierda
                    rotatePieceLeft();
                    break;
                case E: // Tecla para rotar a la derecha
                    rotatePieceRight();
                    break;
                default:
                    break;
            }
        });


        tetrisBack.setFocusTraversable(true);
        startTimer();
    }

    private void createAndPlacePiece() {
        currentPiece = new Piece(); // Crear una pieza aleatoria
        placePieceOnGrid();
    }
    private void removePieceFromGrid() {
        int row = currentPiece.getCurrentRow();
        int col = currentPiece.getCurrentColumn();
        int[][] coords = currentPiece.getCoordinates();

        for (int i = 0; i < currentPiece.getCurrentDesign().size(); i++) {
            Rectangle square = currentPiece.getCurrentDesign().get(i);
            int removeRow = row + coords[i][0];
            int removeCol = col + coords[i][1];
            tetrisBack.getChildren().remove(square);
            isOccupied.get(removeRow).set(removeCol, false);
        }
    }


    private void placePieceOnGrid() {
        int row = currentPiece.getCurrentRow();
        int col = currentPiece.getCurrentColumn();
        int[][] coords = currentPiece.getCoordinates();
        List<Rectangle> design = currentPiece.getCurrentDesign();

        for (int i = 0; i < design.size(); i++) {
            Rectangle square = design.get(i);
            int newRow = row + coords[i][0];
            int newCol = col + coords[i][1];
            tetrisBack.add(square, newCol, newRow);
            isOccupied.get(newRow).set(newCol, true);
        }
    }
    public boolean canMove(Piece piece, int rowDelta, int colDelta) {
        int row = piece.getCurrentRow();
        int col = piece.getCurrentColumn();
        int[][] coords = piece.getCoordinates();

        for (int[] coord : coords) {
            int newRow = row + coord[0] + rowDelta;
            int newCol = col + coord[1] + colDelta;

            // Verificar límites del tablero
            if (newRow < 0 || newRow >= tetrisBack.getRowCount() ||
                    newCol < 0 || newCol >= tetrisBack.getColumnCount()) {
                return false;
            }

            // Verificar si la celda está ocupada por otra pieza
            if (isOccupied.get(newRow).get(newCol) && !isOccupiedCellByCurrentPiece(newRow, newCol, coords)) {
                return false;
            }
        }
        return true;
    }
    private boolean isOccupiedCellByCurrentPiece(int row, int col, int[][] coords) {
        int pieceRow = currentPiece.getCurrentRow();
        int pieceCol = currentPiece.getCurrentColumn();

        for (int[] coord : coords) {
            int cellRow = pieceRow + coord[0];
            int cellCol = pieceCol + coord[1];
            if (cellRow == row && cellCol == col) {
                return true;
            }
        }
        return false;
    }

    private void rotatePieceLeft() {
        if(!gameEnd) {
            removePieceFromGrid(); // Eliminar la pieza actual del tablero

            currentPiece.rotateLeft(); // Rotar la pieza a la izquierda

            if (canMove(currentPiece, 0, 0)) {
                placePieceOnGrid(); // Si el movimiento es válido, colocar la pieza en el tablero
            } else {
                // Si la rotación no es válida, revertir la rotación
                currentPiece.rotateRight();
                placePieceOnGrid(); // Volver a colocar la pieza original en el tablero
            }
        }
    }

    private void rotatePieceRight() {
        if(!gameEnd) {
            removePieceFromGrid(); // Eliminar la pieza actual del tablero

            currentPiece.rotateRight(); // Rotar la pieza a la derecha

            if (canMove(currentPiece, 0, 0)) {
                placePieceOnGrid(); // Si el movimiento es válido, colocar la pieza en el tablero
            } else {
                // Si la rotación no es válida, revertir la rotación
                currentPiece.rotateLeft();
                placePieceOnGrid(); // Volver a colocar la pieza original en el tablero
            }
        }
    }



    private void movePieceDown() {
        if (!gameEnd) {
            if (canMove(currentPiece, 1, 0)) {
                removePieceFromGrid();
                currentPiece.moveDown();
                placePieceOnGrid();
            } else {
                if (isGameOver()) {
                    stopTimer();
                    System.out.println("Game Over");
                    // Aquí puedes añadir lógica para manejar el fin del juego
                } else {
                    cleanLines();
                    createAndPlacePiece();
                }
            }
        }
    }

    private void movePieceLeft() {
        if (canMove(currentPiece, 0, -1) && !gameEnd) {
            removePieceFromGrid();
            currentPiece.moveLeft();
            placePieceOnGrid();
        }
    }

    private void movePieceRight() {
        if (canMove(currentPiece, 0, 1) && !gameEnd) {
            removePieceFromGrid();
            currentPiece.moveRight();
            placePieceOnGrid();
        }
    }



    private void spacePieceDown() {
        while (canMove(currentPiece, 1, 0) && !gameEnd) {
            removePieceFromGrid();
            currentPiece.moveDown();
            placePieceOnGrid();
        }
        cleanLines();
        createAndPlacePiece();
    }
    private void cleanLines() {
        for (int row = tetrisBack.getRowCount() - 1; row >= 0; row--) {
            boolean isFull = true;

            for (int col = 0; col < tetrisBack.getColumnCount(); col++) {
                if (!isOccupied.get(row).get(col)) {
                    isFull = false;
                    break;
                }
            }

            if (isFull) {
                for (int col = 0; col < tetrisBack.getColumnCount(); col++) {
                    Node nodeToRemove = null;
                    for (Node node : tetrisBack.getChildren()) {
                        if (GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == row
                                && GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == col) {
                            nodeToRemove = node;
                            break;
                        }
                    }
                    if (nodeToRemove != null) {
                        tetrisBack.getChildren().remove(nodeToRemove);
                    }
                }

                isOccupied.remove(row);
                ArrayList<Boolean> newRow = new ArrayList<>(Collections.nCopies(tetrisBack.getColumnCount(), false));
                isOccupied.add(0, newRow);

                for (int r = row; r >= 0; r--) {
                    for (int c = 0; c < tetrisBack.getColumnCount(); c++) {
                        Node node = getNodeAt(r, c);
                        if (node != null) {
                            int newRowIndex = r + 1;
                            if (newRowIndex < tetrisBack.getRowCount()) {
                                GridPane.setRowIndex(node, newRowIndex);
                            }
                        }
                    }
                }

                points += 100;
                lines += 1;
                point.setText(String.valueOf(points));
                line.setText(String.valueOf(lines));
                row++;

                // Reducir el intervalo de caída y actualizar el Timeline
                fallinterval = Math.max(0.1, fallinterval - 0.1);
                updateTimeline();
            }
        }
    }

    private void updateTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(new KeyFrame(Duration.seconds(fallinterval), event -> {
            movePieceDown();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    // Obtiene el nodo en una posición específica
    private Node getNodeAt(int row, int col) {
        for (Node node : tetrisBack.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer colIndex = GridPane.getColumnIndex(node);
            if (rowIndex != null && rowIndex == row && colIndex != null && colIndex == col) {
                return node;
            }
        }
        return null;
    }
    private boolean isGameOver() {
        for (int col = 0; col < tetrisBack.getColumnCount(); col++) {
            if (isOccupied.get(0).get(col)) {
                Platform.runLater(this::showGameOverDialog);
                gameEnd=true;
                return true;
            }
        }
        return false;
    }
    private void showGameOverDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Over");
        alert.setContentText("Do you want to restart the game or exit?");

        ButtonType restartButton = new ButtonType("Restart");
        ButtonType exitButton = new ButtonType("Exit");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(restartButton, exitButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == restartButton) {
                restartGame();
            } else if (response == exitButton) {
                Platform.exit(); // Cierra la aplicación
            }
        });
    }

    private void restartGame() {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) HelloApplication.getPrimaryStage().getScene().getWindow();
                stage.close();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/hello-view.fxml"));
                Parent root = loader.load();
                Stage primaryStage = new Stage();
                primaryStage.setTitle("Tetris Game");
                primaryStage.setScene(new Scene(root));
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
