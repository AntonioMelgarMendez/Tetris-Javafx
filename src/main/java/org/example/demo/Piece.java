package org.example.demo;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

enum TetrominoType {
    I(new int[][]{{0, 1}, {1, 1}, {2, 1}, {3, 1}}),
    O(new int[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}}),
    T(new int[][]{{0, 1}, {1, 0}, {1, 1}, {1, 2}}),
    S(new int[][]{{1, 0}, {1, 1}, {0, 1}, {0, 2}}),
    Z(new int[][]{{0, 0}, {0, 1}, {1, 1}, {1, 2}}),
    J(new int[][]{{0, 0}, {1, 0}, {1, 1}, {1, 2}}),
    L(new int[][]{{0, 2}, {1, 0}, {1, 1}, {1, 2}});

    public final int[][] coordinates;

    TetrominoType(int[][] coordinates) {
        this.coordinates = coordinates;
    }
}

public class Piece {

    private List<Rectangle> currentDesign;
    private TetrominoType type;
    private int currentRow;
    private int currentColumn;
    private int rotationIndex;
    private Color color; // Color aleatorio para la pieza

    public Piece() {
        selectRandomDesign();
        this.currentRow = 0;
        this.currentColumn = 4;
        this.rotationIndex = 0;
    }

    private void selectRandomDesign() {
        Random random = new Random();
        TetrominoType[] types = TetrominoType.values();
        this.type = types[random.nextInt(types.length)];
        this.color = getRandomColor(); // Asignar color aleatorio

        currentDesign = new ArrayList<>();
        for (int[] coord : getCoordinates()) {
            Rectangle rect = new Rectangle(40, 40, color);
            rect.setStroke(Color.GRAY); // Color del borde
            rect.setStrokeWidth(1); // Grosor del borde
            currentDesign.add(rect);
        }
    }

    private Color getRandomColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public List<Rectangle> getCurrentDesign() {
        return currentDesign;
    }

    public int[][] getCoordinates() {
        return rotatePiece(type.coordinates, rotationIndex);
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public int getCurrentColumn() {
        return currentColumn;
    }

    public void moveDown() {
        currentRow++;
    }

    public void moveLeft() {
        currentColumn--;
    }

    public void moveRight() {
        currentColumn++;
    }

    public void rotateLeft() {
        rotationIndex = (rotationIndex - 1 + getRotationCount()) % getRotationCount();
        updateDesign();
    }

    public void rotateRight() {
        rotationIndex = (rotationIndex + 1) % getRotationCount();
        updateDesign();
    }

    private void updateDesign() {
        int[][] newCoords = getCoordinates();
        currentDesign.clear();
        for (int[] coord : newCoords) {
            Rectangle rect = new Rectangle(40, 40, color); // Usar el color aleatorio
            rect.setStroke(Color.GRAY); // Color del borde
            rect.setStrokeWidth(1); // Grosor del borde
            currentDesign.add(rect);
        }
    }

    private int getRotationCount() {
        return switch (type) {
            case I -> 2;
            case S, Z -> 2;
            case T, J, L -> 4;
            default -> 1;
        };
    }

    private int[][] rotatePiece(int[][] coordinates, int rotations) {
        int[][] result = new int[coordinates.length][2];
        int[][] rotationMatrix = {{0, -1}, {1, 0}};

        for (int i = 0; i < coordinates.length; i++) {
            int x = coordinates[i][0];
            int y = coordinates[i][1];
            for (int j = 0; j < rotations; j++) {
                int tempX = x;
                x = -y;
                y = tempX;
            }
            result[i][0] = x;
            result[i][1] = y;
        }

        return result;
    }
}
