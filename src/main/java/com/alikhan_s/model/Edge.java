package com.alikhan_s.model;

import java.util.Objects;

/**
 * Класс, представляющий ребро графа.
 * Реализует Comparable для сортировки по весу.
 */
public class Edge implements Comparable<Edge> {
    public final int src;
    public final int dest;
    public final int weight;

    public Edge(int src, int dest, int weight) {
        if (src <= dest) {
            this.src = src;
            this.dest = dest;
        } else {
            this.src = dest;
            this.dest = src;
        }
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public int compareTo(Edge other) {
        return Integer.compare(this.weight, other.weight);
    }

    @Override
    public String toString() {
        return String.format("(%d-%d, w:%d)", src, dest, weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return (src == edge.src && dest == edge.dest) && weight == edge.weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, dest, weight);
    }
}