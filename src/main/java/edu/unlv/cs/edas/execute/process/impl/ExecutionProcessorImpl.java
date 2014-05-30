package edu.unlv.cs.edas.execute.process.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import edu.unlv.cs.edas.design.domain.Algorithm;
import edu.unlv.cs.edas.design.domain.DesignEdge;
import edu.unlv.cs.edas.design.domain.DesignGraph;
import edu.unlv.cs.edas.design.domain.DesignVertex;
import edu.unlv.cs.edas.execute.domain.Execution;
import edu.unlv.cs.edas.execute.domain.ExecutionEdge;
import edu.unlv.cs.edas.execute.domain.ExecutionHashGraph;
import edu.unlv.cs.edas.execute.domain.ExecutionVertex;
import edu.unlv.cs.edas.execute.domain.ImmutableRound;
import edu.unlv.cs.edas.execute.domain.MutableRound;
import edu.unlv.cs.edas.execute.domain.Round;
import edu.unlv.cs.edas.execute.process.ExecutionProcessor;
import edu.unlv.cs.graph.EdgeKey;

@Component @Scope("prototype")
public class ExecutionProcessorImpl implements ExecutionProcessor {
	
	public static class MessageContext {
		
		private List<Integer> neighbors;
		
		private Map<Integer, Map<String, Object>> messages = new HashMap<>();
		
		public MessageContext(Collection<Integer> neighbors) {
			this.neighbors = new ArrayList<>(neighbors);
			Collections.sort(this.neighbors);
			for (Integer key : neighbors) {
				messages.put(key, NULL_MESSAGE);
			}
		}
		
		public void send(Integer neighbor, Map<String, Object> message) {
			Integer key = neighbors.get(neighbor);
			messages.put(key, convertNativeMap(message));
		}
		
		public void send(Integer neighbor, String message) {
			sendPrimitive(neighbor, message);
		}
		
		public void send(Integer neighbor, Number message) {
			sendPrimitive(neighbor, message);
		}
		
		private void sendPrimitive(Integer neighbor, Object message) {
			Map<String, Object> mapMessage = new HashMap<>();
			mapMessage.put("value", message);
			send(neighbor, mapMessage);
		}
		
		public Map<Integer, Map<String, Object>> getMessages() {
			return messages;
		}
		
	}
	
	private static final Map<String, Object> NULL_MESSAGE;
	
	static {
		NULL_MESSAGE = new HashMap<>();
		NULL_MESSAGE.put("NULL_MESSAGE", null);
	}
	
	private String baseJs;
	
	@PostConstruct
	public void init() throws IOException {
		baseJs = IOUtils.toString(getClass().getResource("execution.js"));
	}
	
	@Override
	public void processToRound(Execution execution, Integer round) throws ScriptException, 
			NoSuchMethodException {
		try {
			while (round >= execution.getRoundCount()) {
				processNextRound(execution);
			}
		} catch (ScriptException e) {
			throw e;
		} catch (NoSuchMethodException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void processNextRound(Execution execution) throws ScriptException, 
			NoSuchMethodException, JsonProcessingException {
		Integer round = execution.getRoundCount();
		
		if (round == 0) {	
			processFirstRound(execution);
		}
		
		Round currentRound = execution.getCurrentRound();
		MutableRound nextRound = new MutableRound();
		nextRound.setMessageCount(currentRound.getMessageCount());
		nextRound.setPreviousMessageCount(currentRound.getMessageCount());
		nextRound.setGraph(new ExecutionHashGraph());
		Map<EdgeKey<Integer>, ExecutionEdge> edges = new HashMap<>();
		
		for (Integer key : currentRound.getGraph().getVertexSet()) {
			processRound(execution.getAlgorithm(), currentRound, nextRound, key, edges);
		}
		
		for (Map.Entry<EdgeKey<Integer>, ExecutionEdge> entry : edges.entrySet()) {
			nextRound.getGraph().putEdge(entry.getKey(), entry.getValue());
		}
		
		execution.addRound(new ImmutableRound(nextRound));
	}
	
	private void processRound(Algorithm algorithm, Round currentRound, MutableRound nextRound, 
			Integer key, Map<EdgeKey<Integer>, ExecutionEdge> edges) throws JsonProcessingException, 
			ScriptException {
		ExecutionVertex currentVertex = currentRound.getGraph().getVertex(key);
		List<Integer> incomingNeighbors = new ArrayList<>(
				currentRound.getGraph().getDestinatingAdjacentVertices(key));
		Collections.sort(incomingNeighbors);
		MessageContext messageContext = new MessageContext(currentRound.getGraph()
				.getAdjacentVertices(key));
		ObjectMapper mapper = new ObjectMapper();
		
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		
		String stateJson = mapper.writeValueAsString(currentVertex.getState());
		engine.eval(baseJs);
		engine.eval("state = " + stateJson + ";");
		engine.put("_mc", messageContext);
		engine.eval(algorithm.getAlgorithm());
		
		boolean sendIndividually = engine.get("onMessage") != null;
		
		StringBuilder singleSend = new StringBuilder()
				.append("onMessages([");
		boolean first = true;
		for (int i = 0; i < incomingNeighbors.size(); i++) {
			Integer neightborKey = incomingNeighbors.get(i);
			EdgeKey<Integer> incomingEdgeKey = new EdgeKey<Integer>(neightborKey, key);
			ExecutionEdge currentEdge = currentRound.getGraph().getEdge(incomingEdgeKey);
			Map<String, Object> message = currentEdge.getState();
			if (message.isEmpty()) {
				continue;
			}
			String valueString = null;
			if (isNotNullMessage(message) && message.size() == 1) {
				Object value = message.values().iterator().next();
				if (value instanceof String) {
					valueString = "\"" + value.toString() +  "\"";
				} else {
					valueString = value.toString();
				}
			} else {
				valueString = mapper.writeValueAsString(message);
			}
			if (sendIndividually) {
				engine.eval("onMessage(" + i + ", " + valueString + ");");
			} else {
				if (!first) {
					singleSend.append(", ");
				}
				singleSend.append(valueString);
			}
			first = false;
		}
		if (!sendIndividually) {
			singleSend.append("])");
			engine.eval(singleSend.toString());
		}
		
		
		Map<String, Object> state = getState(engine);
		
		String stateDisplay = formatDisplayPattern(algorithm.getStateDisplayPattern(), state);
		ExecutionVertex vertex = new ExecutionVertex(currentVertex.getDesign(), state, 
				stateDisplay);
		nextRound.getGraph().putVertex(key, vertex);
		
		for (Map.Entry<Integer, Map<String, Object>> entry : messageContext.getMessages().entrySet()) {
			EdgeKey<Integer> edgeKey = new EdgeKey<Integer>(key, entry.getKey());
			ExecutionEdge currentEdge = currentRound.getGraph().getEdge(edgeKey);
			Map<String, Object> message = entry.getValue();
			if (isNotNullMessage(message)) {
				nextRound.incrementMessageCount();
			}
			String messageDisplay = formatDisplayPattern(algorithm.getMessageDisplayPattern(), 
					message);
			ExecutionEdge edge = new ExecutionEdge(currentEdge.getDesign(), message, 
					messageDisplay);
			edges.put(edgeKey, edge);
		}
	}
	
	private void processFirstRound(Execution execution) throws ScriptException, 
			NoSuchMethodException {
		DesignGraph designGraph = execution.getDesignGraphDetails().getGraph();
		MutableRound round = new MutableRound();
		round.setMessageCount(0);
		round.setPreviousMessageCount(0);
		round.setGraph(new ExecutionHashGraph());
		Map<EdgeKey<Integer>, ExecutionEdge> edges = new HashMap<>();
		
		for (Integer key : designGraph.getVertexSet()) {
			processFirstRound(execution.getAlgorithm(), round, designGraph, key, edges);
		}
		
		for (Map.Entry<EdgeKey<Integer>, ExecutionEdge> entry : edges.entrySet()) {
			round.getGraph().putEdge(entry.getKey(), entry.getValue());
		}
		
		execution.addRound(new ImmutableRound(round));
	}
	
	private void processFirstRound(Algorithm algorithm, MutableRound round, DesignGraph designGraph, 
			Integer key, Map<EdgeKey<Integer>, ExecutionEdge> edges) throws ScriptException, 
			NoSuchMethodException {
		DesignVertex designVertex = designGraph.getVertex(key);
		MessageContext messageContext = new MessageContext(designGraph.getAdjacentVertices(key));
		
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		
		engine.eval(baseJs);
		engine.put("_mc", messageContext);
		engine.eval(algorithm.getAlgorithm());
		
		((Invocable) engine).invokeFunction("begin", designVertex.getLabel());
		
		Map<String, Object> state = getState(engine);
		
		String stateDisplay = formatDisplayPattern(algorithm.getStateDisplayPattern(), state);
		ExecutionVertex vertex = new ExecutionVertex(designVertex, state, stateDisplay);
		round.getGraph().putVertex(key, vertex);
		
		for (Map.Entry<Integer, Map<String, Object>> entry : messageContext.getMessages().entrySet()) {
			EdgeKey<Integer> edgeKey = new EdgeKey<Integer>(key, entry.getKey());
			DesignEdge designEdge = designGraph.getEdge(edgeKey);
			Map<String, Object> message = entry.getValue();
			if (isNotNullMessage(message)) {
				round.incrementMessageCount();
			}
			String messageDisplay = formatDisplayPattern(algorithm.getMessageDisplayPattern(), 
					message);
			ExecutionEdge edge = new ExecutionEdge(designEdge, message, messageDisplay);
			edges.put(edgeKey, edge);
		}
	}
	
	private String formatDisplayPattern(String pattern, Map<String, Object> values) {
		if (values.isEmpty()) {
			return "";
		}
		
		if (isNullMessage(values)) {
			return "";
		}
		
		if (values.size() == 1) {
			return values.values().iterator().next().toString();
		}
		
		if (StringUtils.isBlank(pattern)) {
			return "";
		}
		
		Pattern p = Pattern.compile("\\{\\$(\\w|\\.|\\[|\\]|\\*)*\\}");
		Matcher m = p.matcher(pattern);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			String jsonPath = m.group();
			jsonPath = jsonPath.substring(1, jsonPath.length() - 1);
			Object output = JsonPath.read(values, jsonPath);
			if (output == null) {
				output = "";
			}
			m.appendReplacement(sb, output.toString());
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	private boolean isNullMessage(Map<String, Object> message) {
		return message.containsKey("NULL_MESSAGE");
	}
	
	private boolean isNotNullMessage(Map<String, Object> message) {
		return !isNullMessage(message);
	}
	
	private Map<String, Object> getState(ScriptEngine engine) {
		return convertNativeMap(engine.get("state"));
	}
	
	private static Object convertNative(Object nativeValue) {
		if (nativeValue instanceof Map) {
			return convertNativeMap(nativeValue);
		}
		if (nativeValue instanceof List) {
			return convertNativeArray(nativeValue);
		}
		if (nativeValue instanceof Double) {
			Double nativeDouble = (Double) nativeValue;
			if (nativeDouble == Math.floor(nativeDouble)) {
				return new Integer(nativeDouble.intValue());
			}
		}
		return nativeValue;
	}
	
	private static Map<String, Object> convertNativeMap(Object nativeValue) {
		if (!(nativeValue instanceof Map)) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> nativeMap = (Map<String, Object>) nativeValue;
		Map<String, Object> map = new HashMap<>();
		for (Map.Entry<String, Object> entry : nativeMap.entrySet()) {
			map.put(entry.getKey(), convertNative(entry.getValue()));
		}
		return map;
	}
	
	private static List<Object> convertNativeArray(Object nativeValue) {
		if (!(nativeValue instanceof List)) {
			return null;
		}
		@SuppressWarnings("unchecked")
		List<Object> nativeList = (List<Object>) nativeValue;
		List<Object> value = new ArrayList<>();
		for (Object nativeListValue : nativeList) {
			value.add(convertNative(nativeListValue));
		}
		return value;
	}
	
}