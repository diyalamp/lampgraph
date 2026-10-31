/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2019-2024  brunomnsilva@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.brunomnsilva.smartgraph.graph;

import java.util.*;

/**
 * ADT Graph implementation that stores a collection of edges (and vertices) and
 * where each edge contains the references for the vertices it connects.
 * <br>
 * Does not allow duplicates of stored elements through <b>equals</b> criteria.
 * <p>
 * Individual operations are synchronized. Sequences of operations are not atomic;
 * callers that need to group mutations can synchronize on the graph instance.
 * Visualization updates read the model from the JavaFX Application Thread.
 *
 * @param <V> Type of element stored at a vertex
 * @param <E> Type of element stored at an edge
 * 
 * @author brunomnsilva
 */
public class GraphEdgeList<V, E> implements Graph<V, E> {

    /* inner classes are defined at the end of the class, so are the auxiliary methods 
     */
    private final Map<V, Vertex<V>> vertices;
    private final Map<E, Edge<E, V>> edges;

    /**
     * Default constructor that initializes an empty graph.
     */
    public GraphEdgeList() {
        this.vertices = new HashMap<>();
        this.edges = new HashMap<>();
    }

    @Override
    public synchronized int numVertices() {
        return vertices.size();
    }

    @Override
    public synchronized int numEdges() {
        return edges.size();
    }

    @Override
    public synchronized Collection<Vertex<V>> vertices() {
        return new ArrayList<>(vertices.values());
    }

    @Override
    public synchronized Collection<Edge<E, V>> edges() {
        return new ArrayList<>(edges.values());
    }

    @Override
    public synchronized Collection<Edge<E, V>> incidentEdges(Vertex<V> v) throws InvalidVertexException {

        checkVertex(v);

        List<Edge<E, V>> incidentEdges = new ArrayList<>();
        for (Edge<E, V> edge : edges.values()) {

            if (((MyEdge) edge).contains(v)) {
                /* an edge is incident to a vertex when it is one of its endpoints */
                incidentEdges.add(edge);
            }

        }

        return incidentEdges;
    }

    @Override
    public synchronized Vertex<V> opposite(Vertex<V> v, Edge<E, V> e) throws InvalidVertexException, InvalidEdgeException {
        checkVertex(v);
        MyEdge edge = checkEdge(e);

        if (!edge.contains(v)) {
            return null; /* this edge does not connect vertex v */
        }

        Vertex<V>[] endpoints = edge.vertices();
        return endpoints[0] == v ? endpoints[1] : endpoints[0];

    }

    @Override
    public synchronized boolean areAdjacent(Vertex<V> u, Vertex<V> v) throws InvalidVertexException {
        //we allow loops, so we do not check if u == v
        checkVertex(v);
        checkVertex(u);

        /* find and edge that contains both u and v */
        for (Edge<E, V> edge : edges.values()) {
            MyEdge myEdge = (MyEdge) edge;
            if (myEdge.contains(u) && myEdge.contains(v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized Vertex<V> insertVertex(V vElement) throws InvalidVertexException {
        requireVertexElementUnique(vElement);

        MyVertex newVertex = new MyVertex(vElement);

        vertices.put(vElement, newVertex);

        return newVertex;
    }

    @Override
    public synchronized Edge<E, V> insertEdge(Vertex<V> u, Vertex<V> v, E edgeElement) 
            throws InvalidVertexException, InvalidEdgeException {

        requireEdgeElementUnique(edgeElement);

        MyVertex outVertex = checkVertex(u);
        MyVertex inVertex = checkVertex(v);

        MyEdge newEdge = new MyEdge(edgeElement, outVertex, inVertex);

        edges.put(edgeElement, newEdge);

        return newEdge;

    }

    @Override
    public synchronized Edge<E, V> insertEdge(V vElement1, V vElement2, E edgeElement) 
            throws InvalidVertexException, InvalidEdgeException {

        requireEdgeElementUnique(edgeElement);

        MyVertex outVertex = findVertex(vElement1);
        MyVertex inVertex = findVertex(vElement2);

        MyEdge newEdge = new MyEdge(edgeElement, outVertex, inVertex);

        edges.put(edgeElement, newEdge);

        return newEdge;

    }

    @Override
    public synchronized V removeVertex(Vertex<V> v) throws InvalidVertexException {
        MyVertex vertex = checkVertex(v);

        V element = vertex.element();

        //remove incident edges in a single pass
        edges.values().removeIf(edge -> ((MyEdge) edge).contains(vertex));

        vertices.remove(element);

        return element;
    }

    @Override
    public synchronized E removeEdge(Edge<E, V> e) throws InvalidEdgeException {
        checkEdge(e);

        E element = e.element();
        edges.remove(e.element());

        return element;
    }

    @Override
    public synchronized V replace(Vertex<V> v, V newElement) throws InvalidVertexException {
        requireVertexElementUnique(newElement);

        MyVertex vertex = checkVertex(v);

        V oldElement = vertex.element;

        vertices.remove(oldElement);
        vertex.element = newElement;
        vertices.put(newElement, vertex);

        return oldElement;
    }

    @Override
    public synchronized E replace(Edge<E, V> e, E newElement) throws InvalidEdgeException {
        requireEdgeElementUnique(newElement);

        MyEdge edge = checkEdge(e);

        E oldElement = edge.element;

        edges.remove(oldElement);
        edge.element = newElement;
        edges.put(newElement, edge);

        return oldElement;
    }

    @Override
    public String toString() {
        Collection<Vertex<V>> vertexSnapshot;
        Collection<Edge<E, V>> edgeSnapshot;
        synchronized (this) {
            vertexSnapshot = new ArrayList<>(vertices.values());
            edgeSnapshot = new ArrayList<>(edges.values());
        }

        StringBuilder sb = new StringBuilder(
                String.format("Graph with %d vertices and %d edges:\n",
                        vertexSnapshot.size(), edgeSnapshot.size())
        );

        sb.append("--- Vertices: \n");
        for (Vertex<V> v : vertexSnapshot) {
            sb.append("\t").append(v.toString()).append("\n");
        }
        sb.append("\n--- Edges: \n");
        for (Edge<E, V> e : edgeSnapshot) {
            sb.append("\t").append(e.toString()).append("\n");
        }
        return sb.toString();
    }

    class MyVertex implements Vertex<V> {

        volatile V element;
        final Graph<V,?> parent;

        public MyVertex(V element) {
            this.element = element;
            this.parent = GraphEdgeList.this;
        }

        @Override
        public V element() {
            return this.element;
        }

        @Override
        public String toString() {
            return "Vertex{" + element + '}';
        }
    }

    class MyEdge implements Edge<E, V> {

        volatile E element;
        final Vertex<V> vertexA;
        final Vertex<V> vertexB;
        final Graph<V,E> parent;

        public MyEdge(E element, Vertex<V> vertexA, Vertex<V> vertexB) {
            this.element = element;
            this.vertexA = vertexA;
            this.vertexB = vertexB;
            this.parent = GraphEdgeList.this;
        }

        @Override
        public E element() {
            return this.element;
        }

        public boolean contains(Vertex<V> v) {
            return (vertexA == v || vertexB == v);
        }

        @Override
        public Vertex<V>[] vertices() {
            Vertex<V>[] vertices = new Vertex[2];
            vertices[0] = vertexA;
            vertices[1] = vertexB;

            return vertices;
        }

        @Override
        public String toString() {
            return "Edge{{" + element + "}, vertexA=" + vertexA.toString()
                    + ", vertexB=" + vertexB.toString() + '}';
        }
    }

    /**
     * Checks whether a given vertex is valid (i.e., not <i>null</i>) and belongs to this graph
     *
     * @param v vertex to check
     * @return the reference of the vertex
     * @throws InvalidVertexException if the vertex is invalid
     */
    private MyVertex checkVertex(Vertex<V> v) throws InvalidVertexException {
        if(v == null) throw new InvalidVertexException("Null vertex.");
        
        MyVertex vertex;
        try {
            vertex = (MyVertex) v;
        } catch (ClassCastException e) {
            throw new InvalidVertexException("Not a vertex.");
        }

        if (vertex.parent != GraphEdgeList.this) {
            throw new InvalidVertexException("Vertex does not belong to this graph.");
        }

        return vertex;
    }

    private MyEdge checkEdge(Edge<E, V> e) throws InvalidEdgeException {
        if(e == null) throw new InvalidEdgeException("Null edge.");
        
        MyEdge edge;
        try {
            edge = (MyEdge) e;
        } catch (ClassCastException ex) {
            throw new InvalidVertexException("Not an adge.");
        }

        if (edge.parent != GraphEdgeList.this) {
            throw new InvalidEdgeException("Edge does not belong to this graph.");
        }

        return edge;
    }

    /**
     * Finds the vertex whose element is the given one.
     *
     * @param vElement vertex element
     * @return the vertex with the given element
     * @throws InvalidVertexException if no vertex contains this element
     */
    private MyVertex findVertex(V vElement) throws InvalidVertexException {
        MyVertex vertex = (MyVertex) vertices.get(vElement);
        if (vertex == null) {
            throw new InvalidVertexException("No vertex contains " + vElement);
        }
        return vertex;
    }

    /**
     * Ensures that no vertex with the given element exists in this graph.
     *
     * @param vElement element to check
     * @throws InvalidVertexException if a vertex with this element already exists
     */
    private void requireVertexElementUnique(V vElement) throws InvalidVertexException {
        if (vertices.containsKey(vElement)) {
            throw new InvalidVertexException("There's already a vertex with this element.");
        }
    }

    /**
     * Ensures that no edge with the given element exists in this graph.
     *
     * @param edgeElement element to check
     * @throws InvalidEdgeException if an edge with this element already exists
     */
    private void requireEdgeElementUnique(E edgeElement) throws InvalidEdgeException {
        if (edges.containsKey(edgeElement)) {
            throw new InvalidEdgeException("There's already an edge with this element.");
        }
    }
}
