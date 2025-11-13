package com.alikhan_s.algorithm;

import com.alikhan_s.model.Edge;
import com.alikhan_s.model.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Реализация алгоритма Крускала для построения MST.
 */
public class KruskalMST implements MSTStrategy {

    /**
     * Строит MST по алгоритму Крускала.
     */
    @Override
    public Set<Edge> buildMST(Graph graph) {
        ArrayList<Edge> allEdges = new ArrayList<>(graph.getEdges());
        Collections.sort(allEdges);

        DisjointSetUnion dsu = new DisjointSetUnion(graph.getV());

        Set<Edge> mstResult = new LinkedHashSet<>();

        for (Edge edge : allEdges) {
            int rootSrc = dsu.find(edge.src);
            int rootDest = dsu.find(edge.dest);

            if (rootSrc != rootDest) {
                mstResult.add(edge);
                dsu.union(rootSrc, rootDest);
            }
        }

        return mstResult;
    }
}