package com.alikhan_s.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс, представляющий граф.
 * Хранит количество вершин и набор ребер.
 */
public class Graph {
    private final int V;
    private final Set<Edge> edges;

    /**
     * @param V Количество вершин в графе.
     */
    public Graph(int V) {
        this.V = V;
        this.edges = new HashSet<>();
    }

    /**
     * Добавляет ребро в граф.
     */
    public void addEdge(int src, int dest, int weight) {
        if (src >= V || dest >= V || src < 0 || dest < 0) {
            throw new IllegalArgumentException("Индекс вершины вне диапазона");
        }
        edges.add(new Edge(src, dest, weight));
    }

    public Set<Edge> getEdges() {
        return new HashSet<>(edges);
    }

    public int getV() {
        return V;
    }

    /**
     * Вспомогательный метод для получения всех уникальных вершин,
     * которые реально используются в ребрах.
     */
    public Set<Integer> getVertices() {
        Set<Integer> vertices = new HashSet<>();
        for (Edge edge : edges) {
            vertices.add(edge.src);
            vertices.add(edge.dest);
        }
        return vertices;
    }
}