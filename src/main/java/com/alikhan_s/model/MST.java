package com.alikhan_s.model;

import java.util.*;

/**
 * Класс, представляющий Минимальное Остовное Дерево (MST).
 * Хранит ребра, входящие в MST, и ссылку на исходный граф.
 */
public class MST {
    private final Graph originalGraph;
    private final Set<Edge> mstEdges;
    private final Set<Integer> vertices;

    public MST(Graph originalGraph, Set<Edge> mstEdges) {
        this.originalGraph = originalGraph;
        this.mstEdges = new LinkedHashSet<>(mstEdges);

        this.vertices = new HashSet<>();
        for (Edge edge : mstEdges) {
            this.vertices.add(edge.src);
            this.vertices.add(edge.dest);
        }
    }

    /**
     * Приватный конструктор для внутреннего использования (split/union),
     * который принимает готовый набор вершин.
     */
    private MST(Graph originalGraph, Set<Edge> mstEdges, Set<Integer> vertices) {
        this.originalGraph = originalGraph;
        this.mstEdges = new LinkedHashSet<>(mstEdges);
        this.vertices = new HashSet<>(vertices);
    }

    public Set<Edge> getMstEdges() {
        return new HashSet<>(mstEdges);
    }

    public Graph getOriginalGraph() {
        return originalGraph;
    }

    /**
     * Получает все вершины, задействованные в данном MST.
     */
    public Set<Integer> getVertices() {
        return new HashSet<>(this.vertices);
    }

    /**
     * Удаляет ребро, случайно выбранное из "среднего" диапазона MST.
     * Диапазон определен как 30% - 70% от списка ребер.
     *
     * @return Удаленное ребро.
     */
    public Edge removeEdgeRandomInMiddleRange() {
        if (mstEdges.isEmpty()) {
            return null;
        }

        List<Edge> orderedEdges = new ArrayList<>(mstEdges);
        int totalEdges = orderedEdges.size();

        if (totalEdges <= 2) {
            Edge edgeToRemove = orderedEdges.get(0);
            mstEdges.remove(edgeToRemove);
            return edgeToRemove;
        }

        int startIndex = (int) (totalEdges * 0.3);
        int endIndex = (int) (totalEdges * 0.7);

        if (startIndex >= endIndex) {
            startIndex = 0;
        }

        int randomIndex = new Random().nextInt(startIndex, endIndex + 1);
        if (randomIndex >= orderedEdges.size()) {
            randomIndex = orderedEdges.size() - 1;
        }


        Edge removedEdge = orderedEdges.get(randomIndex);
        mstEdges.remove(removedEdge);
        return removedEdge;
    }

    /**
     * Разделяет текущее MST (после удаления ребра) на две связные компоненты.
     *
     * @return Список из двух новых MST, представляющих компоненты.
     */
    public List<MST> splitIntoComponents() {
        com.alikhan_s.algorithm.DisjointSetUnion dsu =
                new com.alikhan_s.algorithm.DisjointSetUnion(originalGraph.getV());

        Set<Integer> allVerticesInGraph = originalGraph.getVertices();

        for (Edge edge : mstEdges) {
            dsu.union(edge.src, edge.dest);
        }

        Map<Integer, Set<Integer>> componentsVertices = new HashMap<>();
        for (int vertex : allVerticesInGraph) {
            int root = dsu.find(vertex);
            componentsVertices.computeIfAbsent(root, k -> new HashSet<>()).add(vertex);
        }

        if (componentsVertices.size() < 2) {
            return List.of(new MST(originalGraph, new HashSet<>()), new MST(originalGraph, new HashSet<>()));
        }

        Iterator<Set<Integer>> iterator = componentsVertices.values().iterator();
        Set<Integer> comp1Verts = iterator.next();
        Set<Integer> comp2Verts = iterator.next();

        Set<Edge> comp1Edges = new HashSet<>();
        Set<Edge> comp2Edges = new HashSet<>();

        for (Edge edge : mstEdges) {
            if (comp1Verts.contains(edge.src) && comp1Verts.contains(edge.dest)) {
                comp1Edges.add(edge);
            } else if (comp2Verts.contains(edge.src) && comp2Verts.contains(edge.dest)) {
                comp2Edges.add(edge);
            }
        }

        MST mst1 = new MST(originalGraph, comp1Edges, comp1Verts);
        MST mst2 = new MST(originalGraph, comp2Edges, comp2Verts);

        return List.of(mst1, mst2);
    }

    /**
     * Ищет минимальное по весу ребро в *исходном* графе,
     * которое соединяет *этот* MST (компоненту) с *другим* MST.
     *
     * @param other Другой MST (вторая компонента).
     * @return Ребро с минимальным весом.
     */
    public Edge findMinEdgeBetween(MST other) {
        Set<Integer> thisVertices = this.getVertices();
        Set<Integer> otherVertices = other.getVertices();
        Edge minEdge = null;

        for (Edge edge : originalGraph.getEdges()) {
            boolean isCrossEdge = (thisVertices.contains(edge.src) && otherVertices.contains(edge.dest)) ||
                    (thisVertices.contains(edge.dest) && otherVertices.contains(edge.src));

            if (isCrossEdge) {
                if (minEdge == null || edge.getWeight() < minEdge.getWeight()) {
                    minEdge = edge;
                }
            }
        }
        return minEdge;
    }

    /**
     * Создает новый MST, объединяя этот MST, другой MST и соединяющее ребро.
     */
    public MST unionWith(MST other, Edge connectingEdge) {
        Set<Edge> combinedEdges = new HashSet<>(this.mstEdges);
        combinedEdges.addAll(other.mstEdges);
        if (connectingEdge != null) {
            combinedEdges.add(connectingEdge);
        }

        Set<Integer> combinedVertices = new HashSet<>(this.vertices);
        combinedVertices.addAll(other.vertices);

        return new MST(this.originalGraph, combinedEdges, combinedVertices);
    }
}