package com.brunomnsilva.smartgraph.graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Graph} and {@link Digraph} implementations backed by
 * edge lists.
 */
@DisplayName("Graph implementations")
class GraphImplTest {

    @SafeVarargs
    private static <V> void assertContainsExactly(Collection<Vertex<V>> vertices, Vertex<V>... expected) {
        assertEquals(expected.length, vertices.size());
        for (Vertex<V> vertex : expected) {
            assertTrue(vertices.contains(vertex), "Missing vertex " + vertex.element());
        }
    }

    @SafeVarargs
    private static <E, V> void assertContainsExactly(Collection<Edge<E, V>> edges, Edge<E, V>... expected) {
        assertEquals(expected.length, edges.size());
        for (Edge<E, V> edge : expected) {
            assertTrue(edges.contains(edge), "Missing edge " + edge.element());
        }
    }

    @Nested
    @DisplayName("GraphEdgeList (undirected graph)")
    class UndirectedGraphTest {

        private Graph<String, Integer> graph;
        private Vertex<String> a;
        private Vertex<String> b;
        private Vertex<String> c;

        @BeforeEach
        void setUp() {
            graph = new GraphEdgeList<>();
            a = graph.insertVertex("A");
            b = graph.insertVertex("B");
            c = graph.insertVertex("C");
        }

        @Test
        @DisplayName("a new graph is empty")
        void newGraphIsEmpty() {
            Graph<String, Integer> empty = new GraphEdgeList<>();

            assertEquals(0, empty.numVertices());
            assertEquals(0, empty.numEdges());
            assertTrue(empty.vertices().isEmpty());
            assertTrue(empty.edges().isEmpty());
        }

        @Test
        @DisplayName("insertVertex stores the element and increases the vertex count")
        void insertVertexStoresElement() {
            assertEquals("A", a.element());
            assertEquals("B", b.element());
            assertEquals("C", c.element());
            assertEquals(3, graph.numVertices());
            assertContainsExactly(graph.vertices(), a, b, c);
        }

        @Test
        @DisplayName("insertVertex rejects duplicate elements")
        void insertVertexRejectsDuplicateElements() {
            assertThrows(InvalidVertexException.class, () -> graph.insertVertex("A"));

            assertEquals(3, graph.numVertices());
            assertContainsExactly(graph.vertices(), a, b, c);
        }

        @Test
        @DisplayName("vertices() returns a snapshot of the graph")
        void verticesReturnsSnapshot() {
            Collection<Vertex<String>> vertices = graph.vertices();
            vertices.clear();

            assertEquals(3, graph.numVertices());
        }

        @Test
        @DisplayName("edges() returns a snapshot of the graph")
        void edgesReturnsSnapshot() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            Collection<Edge<Integer, String>> edges = graph.edges();
            edges.clear();

            assertEquals(1, graph.numEdges());
            assertContainsExactly(graph.edges(), edge);
        }

        @Test
        @DisplayName("insertEdge connects both vertices")
        void insertEdgeConnectsVertices() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            assertEquals(1, graph.numEdges());
            assertEquals(10, edge.element());
            assertArrayEquals(new Vertex[]{a, b}, edge.vertices());
            assertContainsExactly(graph.edges(), edge);
            assertContainsExactly(graph.incidentEdges(a), edge);
            assertContainsExactly(graph.incidentEdges(b), edge);
            assertTrue(graph.areAdjacent(a, b));
            assertTrue(graph.areAdjacent(b, a));
        }

        @Test
        @DisplayName("insertEdge by vertex elements connects the matching vertices")
        void insertEdgeByElementsConnectsVertices() {
            Edge<Integer, String> edge = graph.insertEdge("A", "C", 11);

            assertArrayEquals(new Vertex[]{a, c}, edge.vertices());
            assertEquals(1, graph.numEdges());
            assertTrue(graph.areAdjacent(a, c));
            assertThrows(InvalidVertexException.class, () -> graph.insertEdge("A", "Z", 12));
            assertEquals(1, graph.numEdges());
        }

        @Test
        @DisplayName("insertEdge rejects duplicate edge elements")
        void insertEdgeRejectsDuplicateElements() {
            graph.insertEdge(a, b, 10);

            assertThrows(InvalidEdgeException.class, () -> graph.insertEdge(b, c, 10));
            assertThrows(InvalidEdgeException.class, () -> graph.insertEdge("B", "C", 10));
            assertEquals(1, graph.numEdges());
        }

        @Test
        @DisplayName("insertEdge rejects invalid vertices")
        void insertEdgeRejectsInvalidVertices() {
            Graph<String, Integer> other = new GraphEdgeList<>();
            Vertex<String> foreign = other.insertVertex("X");

            assertAll(
                    () -> assertThrows(InvalidVertexException.class, () -> graph.insertEdge(null, b, 10)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.insertEdge(a, null, 11)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.insertEdge(foreign, b, 12)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.insertEdge(a, foreign, 13))
            );
            assertEquals(0, graph.numEdges());
        }

        @Test
        @DisplayName("an isolated vertex has no incident edges")
        void isolatedVertexHasNoIncidentEdges() {
            graph.insertEdge(a, b, 10);

            assertTrue(graph.incidentEdges(c).isEmpty());
        }

        @Test
        @DisplayName("opposite returns the vertex at the other end of an edge")
        void oppositeReturnsOtherEnd() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            assertEquals(b, graph.opposite(a, edge));
            assertEquals(a, graph.opposite(b, edge));
            assertNull(graph.opposite(c, edge));
        }

        @Test
        @DisplayName("opposite rejects invalid arguments")
        void oppositeRejectsInvalidArguments() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            assertThrows(InvalidVertexException.class, () -> graph.opposite(null, edge));
            assertThrows(InvalidEdgeException.class, () -> graph.opposite(a, null));
        }

        @Test
        @DisplayName("areAdjacent is false for unconnected vertices")
        void areAdjacentIsFalseForUnconnectedVertices() {
            graph.insertEdge(a, b, 10);

            assertFalse(graph.areAdjacent(a, c));
            assertFalse(graph.areAdjacent(c, a));
        }

        @Test
        @DisplayName("self-loops are allowed")
        void selfLoopsAreAllowed() {
            Edge<Integer, String> loop = graph.insertEdge(a, a, 10);

            assertEquals(1, graph.numEdges());
            assertContainsExactly(graph.incidentEdges(a), loop);
            assertTrue(graph.areAdjacent(a, a));
            assertEquals(a, graph.opposite(a, loop));
        }

        @Test
        @DisplayName("removeVertex removes incident self-loops")
        void removeVertexRemovesSelfLoops() {
            graph.insertEdge(a, a, 10);
            graph.insertEdge(a, b, 11);

            assertEquals("A", graph.removeVertex(a));

            assertEquals(0, graph.numEdges());
        }

        @Test
        @DisplayName("parallel edges between the same vertices are allowed")
        void parallelEdgesAreAllowed() {
            Edge<Integer, String> first = graph.insertEdge(a, b, 10);
            Edge<Integer, String> second = graph.insertEdge(a, b, 11);

            assertEquals(2, graph.numEdges());
            assertContainsExactly(graph.incidentEdges(a), first, second);
            assertContainsExactly(graph.incidentEdges(b), first, second);
            assertTrue(graph.areAdjacent(a, b));
        }

        @Test
        @DisplayName("removeEdge removes only the given edge")
        void removeEdgeRemovesEdge() {
            Edge<Integer, String> ab = graph.insertEdge(a, b, 10);
            Edge<Integer, String> ac = graph.insertEdge(a, c, 11);

            assertEquals(10, graph.removeEdge(ab));

            assertEquals(1, graph.numEdges());
            assertContainsExactly(graph.edges(), ac);
            assertTrue(graph.incidentEdges(b).isEmpty());
            assertFalse(graph.areAdjacent(a, b));
        }

        @Test
        @DisplayName("removeVertex removes the vertex and all incident edges")
        void removeVertexRemovesIncidentEdges() {
            graph.insertEdge(a, b, 10);
            graph.insertEdge(a, c, 11);
            Edge<Integer, String> bc = graph.insertEdge(b, c, 12);

            assertEquals("A", graph.removeVertex(a));

            assertEquals(2, graph.numVertices());
            assertEquals(1, graph.numEdges());
            assertFalse(graph.vertices().contains(a));
            assertContainsExactly(graph.edges(), bc);
        }

        @Test
        @DisplayName("removeVertex rejects invalid vertices")
        void removeVertexRejectsInvalidVertices() {
            Graph<String, Integer> other = new GraphEdgeList<>();
            Vertex<String> foreign = other.insertVertex("X");

            assertAll(
                    () -> assertThrows(InvalidVertexException.class, () -> graph.removeVertex(null)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.removeVertex(foreign))
            );
            assertEquals(3, graph.numVertices());
        }

        @Test
        @DisplayName("removeEdge rejects invalid edges")
        void removeEdgeRejectsInvalidEdges() {
            Graph<String, Integer> other = new GraphEdgeList<>();
            Vertex<String> x = other.insertVertex("X");
            Vertex<String> y = other.insertVertex("Y");
            Edge<Integer, String> foreign = other.insertEdge(x, y, 10);

            assertAll(
                    () -> assertThrows(InvalidEdgeException.class, () -> graph.removeEdge(null)),
                    () -> assertThrows(InvalidEdgeException.class, () -> graph.removeEdge(foreign))
            );
            assertEquals(0, graph.numEdges());
        }

        @Test
        @DisplayName("replace(Vertex) returns the previous element and updates the vertex")
        void replaceVertexElement() {
            Vertex<String> d = graph.insertVertex("D");

            assertEquals("D", graph.replace(d, "E"));

            assertEquals("E", d.element());
            assertEquals(4, graph.numVertices());
            assertTrue(graph.vertices().contains(d));
        }

        @Test
        @DisplayName("replace(Vertex) rejects an element already used by another vertex")
        void replaceVertexRejectsDuplicateElement() {
            Vertex<String> d = graph.insertVertex("D");

            assertThrows(InvalidVertexException.class, () -> graph.replace(d, "A"));

            assertEquals("D", d.element());
            assertEquals(4, graph.numVertices());
        }

        @Test
        @DisplayName("replace(Vertex) re-indexes the new element")
        void replaceVertexReindexesElement() {
            Vertex<String> d = graph.insertVertex("D");

            assertEquals("D", graph.replace(d, "E"));

            assertThrows(InvalidVertexException.class, () -> graph.insertVertex("E"));
            assertEquals(4, graph.numVertices());

            Vertex<String> reinserted = graph.insertVertex("D");
            assertEquals("D", reinserted.element());
            assertEquals(5, graph.numVertices());
        }

        @Test
        @DisplayName("a replaced vertex can be used by element-based edge insertion")
        void replacedVertexCanBeUsedByElementBasedInsertEdge() {
            Vertex<String> d = graph.insertVertex("D");
            graph.replace(d, "E");

            Edge<Integer, String> edge = graph.insertEdge("E", "A", 10);

            assertArrayEquals(new Vertex[]{d, a}, edge.vertices());
        }

        @Test
        @DisplayName("replace(Edge) returns the previous element and updates the edge")
        void replaceEdgeElement() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            assertEquals(10, graph.replace(edge, 20));

            assertEquals(20, edge.element());
            assertEquals(20, graph.edges().iterator().next().element());
            assertEquals(1, graph.numEdges());
        }

        @Test
        @DisplayName("replace(Edge) rejects an element already used by another edge")
        void replaceEdgeRejectsDuplicateElement() {
            Edge<Integer, String> first = graph.insertEdge(a, b, 10);
            graph.insertEdge(b, c, 11);

            assertThrows(InvalidEdgeException.class, () -> graph.replace(first, 11));

            assertEquals(10, first.element());
            assertEquals(2, graph.numEdges());
        }

        @Test
        @DisplayName("replace(Edge) re-indexes the new element")
        void replaceEdgeReindexesElement() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            assertEquals(10, graph.replace(edge, 20));

            assertThrows(InvalidEdgeException.class, () -> graph.insertEdge(a, c, 20));
            assertEquals(1, graph.numEdges());

            Edge<Integer, String> reinserted = graph.insertEdge(a, c, 10);
            assertEquals(10, reinserted.element());
            assertEquals(2, graph.numEdges());
        }

        @Test
        @DisplayName("vertices and edges from another graph are rejected")
        void foreignElementsAreRejected() {
            Graph<String, Integer> other = new GraphEdgeList<>();
            Vertex<String> x = other.insertVertex("X");
            Vertex<String> y = other.insertVertex("Y");
            Edge<Integer, String> xy = other.insertEdge(x, y, 10);
            Edge<Integer, String> ab = graph.insertEdge(a, b, 11);

            assertAll(
                    () -> assertThrows(InvalidVertexException.class, () -> graph.incidentEdges(x)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.opposite(x, ab)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.areAdjacent(x, a)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.replace(x, "Z")),
                    () -> assertThrows(InvalidEdgeException.class, () -> graph.opposite(a, xy)),
                    () -> assertThrows(InvalidEdgeException.class, () -> graph.replace(xy, 12))
            );
        }
    }

    @Nested
    @DisplayName("DigraphEdgeList (directed graph)")
    class DirectedGraphTest {

        private Digraph<String, Integer> graph;
        private Vertex<String> a;
        private Vertex<String> b;
        private Vertex<String> c;

        @BeforeEach
        void setUp() {
            graph = new DigraphEdgeList<>();
            a = graph.insertVertex("A");
            b = graph.insertVertex("B");
            c = graph.insertVertex("C");
        }

        @Test
        @DisplayName("a new digraph is empty")
        void newDigraphIsEmpty() {
            Digraph<String, Integer> empty = new DigraphEdgeList<>();

            assertEquals(0, empty.numVertices());
            assertEquals(0, empty.numEdges());
            assertTrue(empty.vertices().isEmpty());
            assertTrue(empty.edges().isEmpty());
        }

        @Test
        @DisplayName("insertVertex stores the element and increases the vertex count")
        void insertVertexStoresElement() {
            assertEquals(3, graph.numVertices());
            assertEquals("A", a.element());
            assertEquals("B", b.element());
            assertEquals("C", c.element());
            assertContainsExactly(graph.vertices(), a, b, c);
        }

        @Test
        @DisplayName("insertVertex rejects duplicate elements")
        void insertVertexRejectsDuplicateElements() {
            assertThrows(InvalidVertexException.class, () -> graph.insertVertex("A"));

            assertEquals(3, graph.numVertices());
            assertContainsExactly(graph.vertices(), a, b, c);
        }

        @Test
        @DisplayName("vertices() and edges() return snapshots of the graph")
        void collectionsAreSnapshots() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            graph.vertices().clear();
            graph.edges().clear();

            assertEquals(3, graph.numVertices());
            assertEquals(1, graph.numEdges());
            assertContainsExactly(graph.edges(), edge);
        }

        @Test
        @DisplayName("insertEdge connects outbound to inbound")
        void insertEdgeIsDirected() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            assertEquals(1, graph.numEdges());
            assertEquals(10, edge.element());
            assertArrayEquals(new Vertex[]{a, b}, edge.vertices());
            assertContainsExactly(graph.outboundEdges(a), edge);
            assertContainsExactly(graph.incidentEdges(b), edge);
            assertTrue(graph.outboundEdges(b).isEmpty());
            assertTrue(graph.incidentEdges(a).isEmpty());
            assertTrue(graph.areAdjacent(a, b));
            assertFalse(graph.areAdjacent(b, a));
        }

        @Test
        @DisplayName("insertEdge by vertex elements preserves the direction")
        void insertEdgeByElementsIsDirected() {
            Edge<Integer, String> edge = graph.insertEdge("A", "C", 11);

            assertArrayEquals(new Vertex[]{a, c}, edge.vertices());
            assertContainsExactly(graph.outboundEdges(a), edge);
            assertContainsExactly(graph.incidentEdges(c), edge);
            assertThrows(InvalidVertexException.class, () -> graph.insertEdge("A", "Z", 12));
            assertEquals(1, graph.numEdges());
        }

        @Test
        @DisplayName("edges in opposite directions are independent")
        void oppositeDirectionEdgesAreIndependent() {
            Edge<Integer, String> ab = graph.insertEdge(a, b, 10);
            Edge<Integer, String> ba = graph.insertEdge(b, a, 11);

            assertEquals(2, graph.numEdges());
            assertContainsExactly(graph.outboundEdges(a), ab);
            assertContainsExactly(graph.incidentEdges(a), ba);
            assertContainsExactly(graph.outboundEdges(b), ba);
            assertContainsExactly(graph.incidentEdges(b), ab);
            assertTrue(graph.areAdjacent(a, b));
            assertTrue(graph.areAdjacent(b, a));
        }

        @Test
        @DisplayName("parallel edges with the same direction are allowed")
        void parallelEdgesAreAllowed() {
            Edge<Integer, String> first = graph.insertEdge(a, b, 10);
            Edge<Integer, String> second = graph.insertEdge(a, b, 11);

            assertEquals(2, graph.numEdges());
            assertContainsExactly(graph.outboundEdges(a), first, second);
            assertContainsExactly(graph.incidentEdges(b), first, second);
            assertTrue(graph.areAdjacent(a, b));
        }

        @Test
        @DisplayName("insertEdge rejects duplicate edge elements")
        void insertEdgeRejectsDuplicateElements() {
            graph.insertEdge(a, b, 10);

            assertThrows(InvalidEdgeException.class, () -> graph.insertEdge(b, c, 10));
            assertThrows(InvalidEdgeException.class, () -> graph.insertEdge("B", "C", 10));
            assertEquals(1, graph.numEdges());
        }

        @Test
        @DisplayName("insertEdge rejects invalid vertices")
        void insertEdgeRejectsInvalidVertices() {
            Digraph<String, Integer> other = new DigraphEdgeList<>();
            Vertex<String> foreign = other.insertVertex("X");

            assertAll(
                    () -> assertThrows(InvalidVertexException.class, () -> graph.insertEdge(null, b, 10)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.insertEdge(a, null, 11)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.insertEdge(foreign, b, 12)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.insertEdge(a, foreign, 13))
            );
            assertEquals(0, graph.numEdges());
        }

        @Test
        @DisplayName("opposite returns the vertex at the other end of an edge")
        void oppositeReturnsOtherEnd() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            assertEquals(b, graph.opposite(a, edge));
            assertEquals(a, graph.opposite(b, edge));
            assertNull(graph.opposite(c, edge));
        }

        @Test
        @DisplayName("opposite rejects invalid arguments")
        void oppositeRejectsInvalidArguments() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            assertThrows(InvalidVertexException.class, () -> graph.opposite(null, edge));
            assertThrows(InvalidEdgeException.class, () -> graph.opposite(a, null));
        }

        @Test
        @DisplayName("self-loops are allowed")
        void selfLoopsAreAllowed() {
            Edge<Integer, String> loop = graph.insertEdge(a, a, 10);

            assertEquals(1, graph.numEdges());
            assertContainsExactly(graph.outboundEdges(a), loop);
            assertContainsExactly(graph.incidentEdges(a), loop);
            assertTrue(graph.areAdjacent(a, a));
            assertEquals(a, graph.opposite(a, loop));
        }

        @Test
        @DisplayName("removeVertex removes incident self-loops")
        void removeVertexRemovesSelfLoops() {
            graph.insertEdge(a, a, 10);
            graph.insertEdge(a, b, 11);

            assertEquals("A", graph.removeVertex(a));

            assertEquals(0, graph.numEdges());
        }

        @Test
        @DisplayName("removeEdge removes only the given edge")
        void removeEdgeRemovesEdge() {
            Edge<Integer, String> ab = graph.insertEdge(a, b, 10);
            Edge<Integer, String> ac = graph.insertEdge(a, c, 11);

            assertEquals(10, graph.removeEdge(ab));

            assertEquals(1, graph.numEdges());
            assertContainsExactly(graph.edges(), ac);
            assertTrue(graph.outboundEdges(a).contains(ac));
            assertFalse(graph.areAdjacent(a, b));
        }

        @Test
        @DisplayName("removeVertex removes both incoming and outgoing edges")
        void removeVertexRemovesIncomingAndOutgoingEdges() {
            graph.insertEdge(a, b, 10);
            graph.insertEdge(c, a, 11);
            Vertex<String> d = graph.insertVertex("D");
            Vertex<String> e = graph.insertVertex("E");
            Edge<Integer, String> de = graph.insertEdge(d, e, 12);

            assertEquals("A", graph.removeVertex(a));

            assertEquals(4, graph.numVertices());
            assertEquals(1, graph.numEdges());
            assertFalse(graph.vertices().contains(a));
            assertContainsExactly(graph.edges(), de);
        }

        @Test
        @DisplayName("removeVertex rejects invalid vertices")
        void removeVertexRejectsInvalidVertices() {
            Digraph<String, Integer> other = new DigraphEdgeList<>();
            Vertex<String> foreign = other.insertVertex("X");

            assertAll(
                    () -> assertThrows(InvalidVertexException.class, () -> graph.removeVertex(null)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.removeVertex(foreign))
            );
            assertEquals(3, graph.numVertices());
        }

        @Test
        @DisplayName("removeEdge rejects invalid edges")
        void removeEdgeRejectsInvalidEdges() {
            Digraph<String, Integer> other = new DigraphEdgeList<>();
            Vertex<String> x = other.insertVertex("X");
            Vertex<String> y = other.insertVertex("Y");
            Edge<Integer, String> foreign = other.insertEdge(x, y, 10);

            assertAll(
                    () -> assertThrows(InvalidEdgeException.class, () -> graph.removeEdge(null)),
                    () -> assertThrows(InvalidEdgeException.class, () -> graph.removeEdge(foreign))
            );
            assertEquals(0, graph.numEdges());
        }

        @Test
        @DisplayName("replace(Vertex) returns the previous element and updates the vertex")
        void replaceVertexElement() {
            Vertex<String> d = graph.insertVertex("D");

            assertEquals("D", graph.replace(d, "E"));

            assertEquals("E", d.element());
            assertEquals(4, graph.numVertices());
            assertTrue(graph.vertices().contains(d));
        }

        @Test
        @DisplayName("replace(Vertex) rejects an element already used by another vertex")
        void replaceVertexRejectsDuplicateElement() {
            Vertex<String> d = graph.insertVertex("D");

            assertThrows(InvalidVertexException.class, () -> graph.replace(d, "A"));

            assertEquals("D", d.element());
            assertEquals(4, graph.numVertices());
        }

        @Test
        @DisplayName("replace(Vertex) re-indexes the new element")
        void replaceVertexReindexesElement() {
            Vertex<String> d = graph.insertVertex("D");

            assertEquals("D", graph.replace(d, "E"));

            assertThrows(InvalidVertexException.class, () -> graph.insertVertex("E"));
            assertEquals(4, graph.numVertices());

            Vertex<String> reinserted = graph.insertVertex("D");
            assertEquals("D", reinserted.element());
            assertEquals(5, graph.numVertices());
        }

        @Test
        @DisplayName("a replaced vertex can be used by element-based edge insertion")
        void replacedVertexCanBeUsedByElementBasedInsertEdge() {
            Vertex<String> d = graph.insertVertex("D");
            graph.replace(d, "E");

            Edge<Integer, String> edge = graph.insertEdge("E", "A", 10);

            assertArrayEquals(new Vertex[]{d, a}, edge.vertices());
        }

        @Test
        @DisplayName("replace(Edge) returns the previous element and updates the edge")
        void replaceEdgeElement() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            assertEquals(10, graph.replace(edge, 20));

            assertEquals(20, edge.element());
            assertEquals(20, graph.edges().iterator().next().element());
            assertEquals(1, graph.numEdges());
        }

        @Test
        @DisplayName("replace(Edge) rejects an element already used by another edge")
        void replaceEdgeRejectsDuplicateElement() {
            Edge<Integer, String> first = graph.insertEdge(a, b, 10);
            graph.insertEdge(b, c, 11);

            assertThrows(InvalidEdgeException.class, () -> graph.replace(first, 11));

            assertEquals(10, first.element());
            assertEquals(2, graph.numEdges());
        }

        @Test
        @DisplayName("replace(Edge) re-indexes the new element")
        void replaceEdgeReindexesElement() {
            Edge<Integer, String> edge = graph.insertEdge(a, b, 10);

            assertEquals(10, graph.replace(edge, 20));

            assertThrows(InvalidEdgeException.class, () -> graph.insertEdge(a, c, 20));
            assertEquals(1, graph.numEdges());

            Edge<Integer, String> reinserted = graph.insertEdge(a, c, 10);
            assertEquals(10, reinserted.element());
            assertEquals(2, graph.numEdges());
        }

        @Test
        @DisplayName("vertices and edges from another digraph are rejected")
        void foreignElementsAreRejected() {
            Digraph<String, Integer> other = new DigraphEdgeList<>();
            Vertex<String> x = other.insertVertex("X");
            Vertex<String> y = other.insertVertex("Y");
            Edge<Integer, String> xy = other.insertEdge(x, y, 10);
            Edge<Integer, String> ab = graph.insertEdge(a, b, 11);

            assertAll(
                    () -> assertThrows(InvalidVertexException.class, () -> graph.incidentEdges(x)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.outboundEdges(x)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.opposite(x, ab)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.areAdjacent(x, a)),
                    () -> assertThrows(InvalidVertexException.class, () -> graph.replace(x, "Z")),
                    () -> assertThrows(InvalidEdgeException.class, () -> graph.opposite(a, xy)),
                    () -> assertThrows(InvalidEdgeException.class, () -> graph.replace(xy, 12))
            );
        }
    }
}
