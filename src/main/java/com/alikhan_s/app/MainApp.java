package com.alikhan_s.app;

import com.google.gson.JsonSyntaxException;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private ComboBox<String> graphSelector;
    private Label graphLabel;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        visualizer = new MSTVisualizer();
        root.setCenter(visualizer);

        setupControls();
        HBox controlBox = new HBox(10, graphLabel, graphSelector, buildBtn, removeBtn, connectBtn, jsonBtn);
        controlBox.setPadding(new Insets(10));
        root.setTop(controlBox);

        // 3. Лог (Низ)
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        root.setBottom(logArea);

        loadSelectedGraph();

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Демонстрация MST");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Создает и настраивает кнопки управления и селектор графов.
     */
    private void setupControls() {
        graphLabel = new Label("Граф:");
        graphSelector = new ComboBox<>();
        graphSelector.getItems().addAll("graph_demo.json", "graph_simple.json");
        graphSelector.setValue("graph_demo.json");

        graphSelector.setOnAction(e -> loadSelectedGraph());

        buildBtn = new Button("Шаг 1: Построить MST");
        buildBtn.setOnAction(e -> buildMST());
        buildBtn.setDisable(true);

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
     * Сбрасывает состояние симуляции.
     */
    private void resetState() {
        currentMST = null;
        splitComponents = null;
        removedEdge = null;
        connectingEdge = null;

        visualizer.setData(mainGraph, null, null, null);
        logArea.clear();

        buildBtn.setDisable(mainGraph == null);
        removeBtn.setDisable(true);
        connectBtn.setDisable(true);
        jsonBtn.setDisable(true);
    }

    /**
     * Обработчик для ComboBox: загружает выбранный граф.
     */
    private void loadSelectedGraph() {
        String filename = graphSelector.getValue();
        if (filename == null || filename.isEmpty()) {
            log("Файл графа не выбран.");
            return;
        }
        loadGraphFromJson(filename);
    }

    /**
     * Загружает граф из JSON-файла.
     * @param filename Имя файла (в корне проекта)
     */
    private void loadGraphFromJson(String filename) {
        // Используем File.separator, чтобы это работало и на Windows (\), и на Linux (/)
        String pathToFile = "data" + java.io.File.separator + filename;

        try {
            // Читаем файл
            String jsonString = Files.readString(Paths.get(pathToFile));

            // Десериализуем (превращаем JSON-строку обратно в объект Graph)
            mainGraph = gson.fromJson(jsonString, Graph.class);

            if (mainGraph == null) {
                log("Ошибка: JSON файл пуст или некорректен. Файл: " + filename);
                resetState();
                return;
            }

            // Сбрасываем симуляцию
            resetState();
            log("Граф успешно загружен из: " + filename);

        } catch (IOException e) {
            log("ОШИБКА: Не удалось прочитать файл " + pathToFile + ". Убедись, что папка 'data' существует в корне проекта.");
            mainGraph = null;
            resetState();
        } catch (JsonSyntaxException e) {
            log("ОШИБКА: Некорректный синтаксис JSON в файле " + filename + ".");
            log(e.getMessage());
            mainGraph = null;
            resetState();
        }
    }

    /**
     * Точка входа.
     */
    public static void main(String[] args) {
        launch(args);
    }
}