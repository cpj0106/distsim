package edu.unlv.cs.edas.graph.design.dom;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.unlv.cs.edas.graph.design.DesignEdge;
import edu.unlv.cs.edas.graph.design.DesignVertex;
import edu.unlv.cs.edas.graph.design.Position;
import edu.unlv.cs.edas.graph.domain.Key;
import edu.unlv.cs.graph.EdgeKey;
import edu.unlv.cs.graph.Graph;
import edu.unlv.cs.graph.dom.domain.GraphDomBuilder;

public class DesignDomGraphBuilder implements GraphDomBuilder<Key, DesignVertex, DesignEdge, 
		DesignDomGraphBuilderContext> {

	@Override
	public Document createDocument() {
		try {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			return document;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Error creating SVG DOM Document.", e);
		}
	}

	@Override
	public Element createRootElement(Document document) {
		Element rootElement = document.createElementNS("http://www.w3.org/2000/svg", "svg");
		return rootElement;
	}
	
	@Override
	public DesignDomGraphBuilderContext createContext(Document document, Element rootElement, 
			Graph<Key, DesignVertex, DesignEdge> graph) {
		rootElement.setAttribute("version", "1.1");
		rootElement.setAttribute("width", "100%");
		rootElement.setAttribute("height", "100%");
		return new DesignDomGraphBuilderContext(document, rootElement, graph);
	}

	@Override
	public Node createVertex(DesignDomGraphBuilderContext context, Key key, DesignVertex vertex) {
		Document document = context.getDocument();
		Position position = vertex.getPosition();
		Integer id = vertex.getId().getId();
		
		Element groupElement = document.createElement("g");
		
		Element vertexElement = document.createElement("circle");
		vertexElement.setAttribute("id", "-v-" + id);
		vertexElement.setAttribute("vertexName", id.toString());
		vertexElement.setAttribute("r", "20");
		vertexElement.setAttribute("cx", position.getX().toString());
		vertexElement.setAttribute("cy", position.getY().toString());
		vertexElement.setAttribute("fill", "blue");
		groupElement.appendChild(vertexElement);
		
		Element textElement = document.createElement("text");
		textElement.setAttribute("id", "-l-" + id);
		textElement.setAttribute("vertexName", id.toString());
		textElement.setAttribute("x", position.getX().toString());
		textElement.setAttribute("y", position.getY().toString());
		textElement.setAttribute("style", "text-anchor: middle; dominant-baseline: central;");
		textElement.setTextContent(vertex.getLabel());
		groupElement.appendChild(textElement);
		
		return groupElement;
	}

	@Override
	public Node createEdge(DesignDomGraphBuilderContext context, EdgeKey<Key> key, 
			DesignEdge edge) {
		DesignVertex beginVertex = context.getGraph().getVertex(key.getFromKey());
		DesignVertex endVertex = context.getGraph().getVertex(key.getToKey());
		
		Element edgeElement = context.getDocument().createElement("line");
		edgeElement.setAttribute("edgeName", beginVertex.getId().getId() + "-" + 
				endVertex.getId().getId());
		edgeElement.setAttribute("vertexName1", beginVertex.getId().getId().toString());
		edgeElement.setAttribute("vertexName2", endVertex.getId().getId().toString());
		edgeElement.setAttribute("x1", beginVertex.getPosition().getX().toString());
		edgeElement.setAttribute("y1", beginVertex.getPosition().getY().toString());
		edgeElement.setAttribute("x2", endVertex.getPosition().getX().toString());
		edgeElement.setAttribute("y2", endVertex.getPosition().getY().toString());
		edgeElement.setAttribute("stroke", "black");
		edgeElement.setAttribute("stroke-width", "2");
		
		return edgeElement;
	}

}
