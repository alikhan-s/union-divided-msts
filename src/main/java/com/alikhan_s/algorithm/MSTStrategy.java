package com.alikhan_s.algorithm;

import com.alikhan_s.model.Edge;
import com.alikhan_s.model.Graph;

import java.util.Set;

/**
 * Интерфейс для стратегий построения MST.
 * Позволяет в будущем добавить, например, алгоритм Прима.
 */
public interface MSTStrategy {
    /**
     * Строит MST для заданного графа.
     * @param graph Исходный граф.
     * @return Набор ребер, входящих в MST.
     */
    Set<Edge> buildMST(Graph graph);
}