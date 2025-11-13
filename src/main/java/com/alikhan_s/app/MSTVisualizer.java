package com.alikhan_s.app;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import com.alikhan_s.model.Edge;
import com.alikhan_s.model.Graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Компонент JavaFX (Pane) для визуализации графа и MST.
 */
public class MSTVisualizer extends Pane {

    private Graph graph;
    private Set<Edge> mstEdges;
    private Edge removedEdge;
    private Edge addedEdge;

    private final Map<Integer, Point2D> vertexPositions = new HashMap<>();

    public MSTVisualizer() {
        setPrefSize(600, 400);
        setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc;");
    }

    /**
     * Устанавливает данные для визуализации и запускает перерисовку.
     */
    public void setData(Graph graph, Set<Edge> mstEdges, Edge removedEdge, Edge addedEdge) {
        this.graph = graph;
        this.mstEdges = mstEdges;
        this.removedEdge = removedEdge;
        this.addedEdge = addedEdge;

        if (this.graph != null && vertexPositions.isEmpty()) {
            calculatePositions();
        }
        draw();
    }

    /**
     * Рассчитывает позиции вершин
     */
    private void calculatePositions() {
        if (graph == null) return;
        vertexPositions.clear();

        double width = getPrefWidth();
        double height = getPrefHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double radius = Math.min(width, height) / 2 * 0.8;

        Set<Integer> vertices = graph.getVertices();
        int vCount = vertices.size();
        int i = 0;

        for (int vertexId : vertices) {
            double angle = 2 * Math.PI * i / vCount;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            vertexPositions.put(vertexId, new Point2D(x, y));
            i++;
        }
    }

    /**
     * Главный метод отрисовки.
     */
    private void draw() {
        getChildren().clear();
        if (graph == null || vertexPositions.isEmpty()) {
            return;
        }

        for (Edge edge : graph.getEdges()) {
            drawGraphEdge(edge, Color.LIGHTGRAY, 1);
            drawEdgeWeight(edge, Color.GRAY);
        }

        if (mstEdges != null) {
            for (Edge edge : mstEdges) {
                drawGraphEdge(edge, Color.BLACK, 3);
                drawEdgeWeight(edge, Color.BLACK);
            }
        }

        if (removedEdge != null) {
            Line line = drawGraphEdge(removedEdge, Color.RED, 2);
            line.getStrokeDashArray().addAll(10.0, 5.0);

            drawEdgeWeight(removedEdge, Color.RED);
        }

        if (addedEdge != null) {
            drawGraphEdge(addedEdge, Color.GREEN, 4);

            drawEdgeWeight(addedEdge, Color.GREEN);
        }

        for (Map.Entry<Integer, Point2D> entry : vertexPositions.entrySet()) {
            int vertexId = entry.getKey();
            Point2D pos = entry.getValue();

            Circle circle = new Circle(pos.getX(), pos.getY(), 8, Color.DODGERBLUE);
            circle.setStroke(Color.BLACK);

            Text text = new Text(String.valueOf(vertexId));
            text.setX(pos.getX() - (text.getLayoutBounds().getWidth() / 2));
            text.setY(pos.getY() + 4);
            text.setFill(Color.WHITE);

            getChildren().addAll(circle, text);
        }
    }

    /**
     * Вспомогательный метод для рисования одного ребра.
     */
    private Line drawGraphEdge(Edge edge, Color color, double width) {
        Point2D posSrc = vertexPositions.get(edge.src);
        Point2D posDest = vertexPositions.get(edge.dest);

        if (posSrc == null || posDest == null) return null;

        Line line = new Line(posSrc.getX(), posSrc.getY(), posDest.getX(), posDest.getY());
        line.setStroke(color);
        line.setStrokeWidth(width);

        getChildren().add(line);
        line.toBack();

        return line;
    }

    /**
     * Вспомогательный метод для рисования веса ребра.
     * @param edge Ребро, вес которого нужно нарисовать
     * @param color Цвет текста
     */
    private void drawEdgeWeight(Edge edge, Color color) {
        Point2D posSrc = vertexPositions.get(edge.src);
        Point2D posDest = vertexPositions.get(edge.dest);

        if (posSrc == null || posDest == null) return;

        String weightStr = String.valueOf(edge.getWeight());
        Text weightText = new Text(weightStr);

        double midX = (posSrc.getX() + posDest.getX()) / 2;
        double midY = (posSrc.getY() + posDest.getY()) / 2;

        weightText.setX(midX + 5);
        weightText.setY(midY - 5);

        weightText.setFill(color);
        weightText.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-padding: 1px 3px; -fx-background-radius: 3;");

        getChildren().add(weightText);
    }
}