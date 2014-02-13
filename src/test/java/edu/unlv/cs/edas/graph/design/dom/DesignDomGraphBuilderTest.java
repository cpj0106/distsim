package edu.unlv.cs.edas.graph.design.dom;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.unlv.cs.edas.graph.design.DesignEdge;
import edu.unlv.cs.edas.graph.design.DesignVertex;
import edu.unlv.cs.edas.graph.design.Position;
import edu.unlv.cs.edas.graph.domain.Key;
import edu.unlv.cs.graph.EdgeKey;
import edu.unlv.cs.graph.Graph;

public class DesignDomGraphBuilderTest {

	private DesignDomGraphBuilder builder;
	
	private Document document;
	private Element rootElement;
	private Graph<Key, DesignVertex, DesignEdge> graph;
	private DesignDomGraphBuilderContext context;
	
	@SuppressWarnings("unchecked")
	private Graph<Key, DesignVertex, DesignEdge> mockGraph() {
		return mock(Graph.class);
	}
	
	@Before
	public void setUp() {
		graph = mockGraph();
		
		builder = new DesignDomGraphBuilder();
		
		document = builder.createDocument();
		rootElement = builder.createRootElement(document);
		context = builder.createContext(document, rootElement, graph);
	}
	
	@Test
	public void testCreateDocument() {
		Document actual = builder.createDocument();
		assertNotNull(actual);
	}
	
	@Test
	public void testCreateRootElement() {
		Element actual = builder.createRootElement(document);
		assertEquals("svg", actual.getTagName());
		assertEquals("http://www.w3.org/2000/svg", actual.getNamespaceURI());
	}
	
	@Test
	public void testCreateContext() {
		DesignDomGraphBuilderContext actual = builder.createContext(document, rootElement, graph);
		
		assertEquals("1.1", rootElement.getAttribute("version"));
		assertEquals("100%", rootElement.getAttribute("width"));
		assertEquals("100%", rootElement.getAttribute("height"));
		
		assertEquals(document, actual.getDocument());
		assertEquals(rootElement, actual.getRootElement());
		assertEquals(graph, actual.getGraph());
	}
	
	@Test
	public void testCreateVertex() {
		Key key = new Key(1);
		DesignVertex vertex = new DesignVertex();
		vertex.setLabel("some label");
		vertex.setPosition(new Position(2, 3));
		
		Element actual = builder.createVertex(context, key, vertex);
		assertEquals("g", actual.getTagName());
		assertEquals(2, actual.getChildNodes().getLength());
		
		Element actualCircle = (Element) actual.getChildNodes().item(0);
		assertEquals("circle", actualCircle.getTagName());
		assertEquals("-v-1", actualCircle.getAttribute("id"));
		assertEquals("1", actualCircle.getAttribute("vertexId"));
		assertEquals("20", actualCircle.getAttribute("r"));
		assertEquals("2", actualCircle.getAttribute("cx"));
		assertEquals("3", actualCircle.getAttribute("cy"));
		
		Element actualText = (Element) actual.getChildNodes().item(1);
		assertEquals("text", actualText.getTagName());
		assertEquals("-l-1", actualText.getAttribute("id"));
		assertEquals("1", actualText.getAttribute("vertexId"));
		assertEquals("2", actualText.getAttribute("x"));
		assertEquals("3", actualText.getAttribute("y"));
		assertEquals("some label", actualText.getTextContent());
	}
	
	@Test
	public void testCreateEdge() {
		EdgeKey<Key> key = new EdgeKey<Key>(new Key(1), new Key(2)); 
		DesignEdge edge = new DesignEdge();
		edge.setWeight(9);
		
		DesignVertex beginVertex = new DesignVertex();
		beginVertex.setPosition(new Position(3, 5));
		
		DesignVertex endVertex = new DesignVertex();
		endVertex.setPosition(new Position(6, 8));
		
		when(graph.getVertex(new Key(1))).thenReturn(beginVertex);
		when(graph.getVertex(new Key(2))).thenReturn(endVertex);
		
		Element actual = builder.createEdge(context, key, edge);
		assertEquals("g", actual.getTagName());
		assertEquals(2, actual.getChildNodes().getLength());
		
		Element acutalLine = (Element) actual.getChildNodes().item(0);
		assertEquals("line", acutalLine.getTagName());
		assertEquals("-e-1-2", acutalLine.getAttribute("id"));
		assertEquals("1-2", acutalLine.getAttribute("edgeId"));
		assertEquals("1", acutalLine.getAttribute("vertexId1"));
		assertEquals("2", acutalLine.getAttribute("vertexId2"));
		assertEquals("3", acutalLine.getAttribute("x1"));
		assertEquals("5", acutalLine.getAttribute("y1"));
		assertEquals("6", acutalLine.getAttribute("x2"));
		assertEquals("8", acutalLine.getAttribute("y2"));
		
		Element actualText = (Element) actual.getChildNodes().item(1);
		assertEquals("text", actualText.getTagName());
		assertEquals("-w-1-2", actualText.getAttribute("id"));
		assertEquals("1-2", actualText.getAttribute("edgeId"));
		assertEquals("1", actualText.getAttribute("vertexId1"));
		assertEquals("2", actualText.getAttribute("vertexId2"));
		assertEquals("4", actualText.getAttribute("x"));
		assertEquals("6", actualText.getAttribute("y"));
		assertEquals("9", actualText.getTextContent());
	}
	
}