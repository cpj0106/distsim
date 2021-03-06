package edu.unlv.cs.graph;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class BiDirectedGraphTest {

	private BiDirectedGraph<String, String, String> graph;
	private Graph<String, String, String> backingGraph;
	
	@Before
	public void setUp() throws Exception {
		backingGraph = new HashGraph<String, String, String>();
		graph = new BiDirectedGraph<>(backingGraph);
	}
	
	@Test
	public void testPutVertex() throws Exception {
		graph.putVertex("one", "one value");
		
		assertTrue(graph.containsVertex("one"));
		assertEquals("one value", graph.getVertex("one"));
	}
	
	@Test
	public void testPutEdge() throws Exception {
		graph.putVertex("one", "one value");
		graph.putVertex("two", "two value");
		
		graph.putEdge(new EdgeKey<String>("one", "two"), "one - two");
		
		assertEquals("one - two", graph.getEdge(new EdgeKey<String>("one", "two")));
		assertEquals("one - two", graph.getEdge(new EdgeKey<String>("two", "one")));
	}
	
	@Test
	public void testRemoveVertex() throws Exception {
		graph.putVertex("one", "one value");
		
		graph.removeVertex("one");
		assertFalse(graph.containsVertex("one"));
	}
	
	@Test
	public void testRemoveEdge() throws Exception {
		graph.putVertex("one", "one value");
		graph.putVertex("two", "two value");
		
		graph.putEdge(new EdgeKey<String>("one", "two"), "one - two");
		
		assertTrue(graph.containsEdge(new EdgeKey<String>("one", "two")));
		assertTrue(graph.containsEdge(new EdgeKey<String>("two", "one")));
		
		graph.removeEdge(new EdgeKey<String>("two", "one"));
		
		assertFalse(graph.containsEdge(new EdgeKey<String>("one", "two")));
		assertFalse(graph.containsEdge(new EdgeKey<String>("two", "one")));
	}
	
	@Test
	public void testClear() throws Exception {
		graph.putVertex("one", "one value");
		graph.putVertex("two", "two value");
		
		graph.putEdge(new EdgeKey<String>("one", "two"), "one - two");
		
		assertEquals(2, graph.getVertexSize());
		assertEquals(2, graph.getEdgeSize());
		
		graph.clear();
		
		assertEquals(0, graph.getVertexSize());
		assertEquals(0, graph.getEdgeSize());
	}
	
	@Test
	public void testGetAdjacentVertices() throws Exception {
		graph.putVertex("one", "one value");
		graph.putVertex("two", "two value");
		
		graph.putEdge(new EdgeKey<String>("one", "two"), "one - two");
		
		Set<String> expected = new HashSet<String>(Arrays.asList("two"));
		Set<String> actual = new HashSet<String>(graph.getAdjacentVertices("one"));
		
		assertEquals(expected, actual);
		
		expected = new HashSet<String>(Arrays.asList("one"));
		actual = new HashSet<String>(graph.getAdjacentVertices("two"));
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testGetVertexSet() throws Exception {
		graph.putVertex("one", "one value");
		graph.putVertex("two", "two value");
		
		Set<String> expected = new HashSet<String>(Arrays.asList("one", "two"));
		Set<String> actual = new HashSet<String>(graph.getVertexSet());
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testGetEdgeSet() throws Exception {
		graph.putVertex("one", "one value");
		graph.putVertex("two", "two value");
		
		graph.putEdge(new EdgeKey<String>("one", "two"), "one - two");
		
		Set<EdgeKey<String>> expected = new HashSet<EdgeKey<String>>(Arrays.asList(
				new EdgeKey<String>("one", "two"),
				new EdgeKey<String>("two", "one")));
		Set<EdgeKey<String>> actual = new HashSet<EdgeKey<String>>(graph.getEdgeSet());
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testGetBackingGraph() throws Exception {
		assertEquals(backingGraph, graph.getBackingGraph());
	}
	
}
