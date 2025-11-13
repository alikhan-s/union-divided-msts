package com.alikhan_s.algorithm;

/**
 * Реализация системы непересекающихся множеств (Union-Find)
 * с оптимизациями: сжатие пути (path compression) и объединение по рангу (union by rank).
 */
public class DisjointSetUnion {
    private final int[] parent;
    private final int[] rank;

    public DisjointSetUnion(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    /**
     * Находит представителя (корень) множества, к которому принадлежит i.
     * Использует сжатие пути.
     */
    public int find(int i) {
        if (parent[i] == i) {
            return i;
        }
        parent[i] = find(parent[i]);
        return parent[i];
    }

    /**
     * Объединяет два множества, содержащие x и y.
     * Использует объединение по рангу.
     */
    public void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);

        if (rootX != rootY) {
            if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
        }
    }
}