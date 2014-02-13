package edu.unlv.cs.edas.graph.design.manager;

import edu.unlv.cs.edas.graph.design.DesignEdge;
import edu.unlv.cs.edas.graph.design.DesignGraph;
import edu.unlv.cs.edas.graph.design.DesignGraphId;
import edu.unlv.cs.edas.graph.design.DesignVertex;
import edu.unlv.cs.edas.graph.manager.GraphManager;

/**
 * A {@link GraphManager} for design graphs.
 * 
 * @author Chris Jones
 *
 */
public interface DesignGraphManager extends GraphManager<DesignGraphId, DesignVertex, DesignEdge, 
		DesignGraph> {

}