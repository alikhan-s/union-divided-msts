package com.alikhan_s.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import com.alikhan_s.algorithm.KruskalMST;
import com.alikhan_s.model.Edge;
import com.alikhan_s.model.Graph;
import com.alikhan_s.model.MST;

import java.util.List;
import java.util.Set;

/**
 * Главный класс приложения JavaFX.
 * Управляет UI и логикой демонстрации.
 */
public class MainApp extends Application {

    // --- Поля состояния приложения ---
    private Graph mainGraph;
    private MST currentMST;
    private List<MST> splitComponents;
    private Edge removedEdge;
    private Edge connectingEdge;

    // --- Компоненты UI ---
    private MSTVisualizer visualizer;
    private TextArea logArea;
    private Button buildBtn;
    private Button removeBtn;
    private Button connectBtn;
    private Button jsonBtn;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        visualizer = new MSTVisualizer();
        root.setCenter(visualizer);

        setupControls();
        HBox controlBox = new HBox(10, buildBtn, removeBtn, connectBtn, jsonBtn);
        controlBox.setPadding(new Insets(10));
        root.setTop(controlBox);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        root.setBottom(logArea);

        createDemoGraph();
        visualizer.setData(mainGraph, null, null, null);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Демонстрация MST");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Создает и настраивает кнопки управления.
     */
    private void setupControls() {
        buildBtn = new Button("Шаг 1: Построить MST");
        buildBtn.setOnAction(e -> buildMST());

        removeBtn = new Button("Шаг 2: Удалить ребро");
        removeBtn.setOnAction(e -> removeRandomEdge());
        removeBtn.setDisable(true);

        connectBtn = new Button("Шаг 3: Найти и объединить");
        connectBtn.setOnAction(e -> findAndConnect());
        connectBtn.setDisable(true);

        jsonBtn = new Button("Сериализовать в JSON");
        jsonBtn.setOnAction(e -> serializeMST());
        jsonBtn.setDisable(true);
    }

    // --- Логика шагов ---

    private void buildMST() {
        KruskalMST kruskal = new KruskalMST();
        Set<Edge> mstEdges = kruskal.buildMST(mainGraph);
        currentMST = new MST(mainGraph, mstEdges);

        removedEdge = null;
        connectingEdge = null;
        splitComponents = null;

        visualizer.setData(mainGraph, currentMST.getMstEdges(), null, null);
        log("MST построен. Ребер в MST: " + mstEdges.size());

        removeBtn.setDisable(false);
        connectBtn.setDisable(true);
        jsonBtn.setDisable(false);
    }

    private void removeRandomEdge() {
        if (currentMST == null) return;

        removedEdge = currentMST.removeEdgeRandomInMiddleRange();
        if (removedEdge == null) {
            log("Не удалось удалить ребро (MST пуст?).");
            return;
        }

        // MST разделился. Получаем компоненты.
        splitComponents = currentMST.splitIntoComponents();

        // currentMST.getMstEdges() уже обновился (ребро удалено)
        visualizer.setData(mainGraph, currentMST.getMstEdges(), removedEdge, null);
        log("Ребро " + removedEdge + " удалено. MST разделен на 2 компоненты.");

        // Обновляем состояние кнопок
        removeBtn.setDisable(true);
        connectBtn.setDisable(false);
    }

    private void findAndConnect() {
        if (splitComponents == null || splitComponents.size() < 2) return;

        MST mst1 = splitComponents.get(0);
        MST mst2 = splitComponents.get(1);

        connectingEdge = mst1.findMinEdgeBetween(mst2);

        if (connectingEdge == null) {
            log("Не найдено соединяющее ребро (граф несвязный?).");
            return;
        }

        currentMST = mst1.unionWith(mst2, connectingEdge);

        visualizer.setData(mainGraph, currentMST.getMstEdges(), removedEdge, connectingEdge);
        log("Найдено мин. ребро: " + connectingEdge + ". Компоненты объединены.");

        splitComponents = null;
        removeBtn.setDisable(false);
        connectBtn.setDisable(true);
    }

    private void serializeMST() {
        if (currentMST == null) {
            log("Сначала постройте MST.");
            return;
        }
        String json = gson.toJson(currentMST);
        log("--- JSON Сериализация currentMST ---");
        log(json);
        log("-------------------------------------");
    }

    /**
     * Вспомогательный метод для логирования.
     */
    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    /**
     * Создает тестовый граф.
     */
    private void createDemoGraph() {
        // 9 вершин
        mainGraph = new Graph(9);
        mainGraph.addEdge(0, 1, 4);
        mainGraph.addEdge(0, 7, 8);
        mainGraph.addEdge(1, 2, 8);
        mainGraph.addEdge(1, 7, 11);
        mainGraph.addEdge(2, 3, 7);
        mainGraph.addEdge(2, 8, 2);
        mainGraph.addEdge(2, 5, 4);
        mainGraph.addEdge(3, 4, 9);
        mainGraph.addEdge(3, 5, 14);
        mainGraph.addEdge(4, 5, 10);
        mainGraph.addEdge(5, 6, 2);
        mainGraph.addEdge(6, 7, 1);
        mainGraph.addEdge(6, 8, 6);
        mainGraph.addEdge(7, 8, 7);
    }

    /**
     * Точка входа.
     */
    public static void main(String[] args) {
        launch(args);
    }
}