package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph extends ArrayList<Node> {

    public boolean hasCycles() {
        // Check for cycles in all nodes
        for (Node node : this) {
            if (node.hasCycles()) {
                return true;
            }
        }
        return false;
    }

    public void createFromTopics() {
        // Map to store created nodes and prevent duplicates
        Map<String, Node> nodesMap = new HashMap<>();

        // Create nodes for all Topics
        for (Topic topic : TopicManagerSingleton.get().getTopics()) {
            String topicNodeName = "T" + topic.name;
            Node topicNode = nodesMap.computeIfAbsent(topicNodeName, Node::new);

            // Add edges from topic to all subscribed agents
            for (Agent agent : topic.getSubs()) {
                String agentNodeName = "A" + agent.getName();
                Node agentNode = nodesMap.computeIfAbsent(agentNodeName, Node::new);
                topicNode.addEdge(agentNode);
            }

            // Add edges from publishers to topic
            for (Agent agent : topic.getPubs()) {
                String agentNodeName = "A" + agent.getName();
                Node agentNode = nodesMap.computeIfAbsent(agentNodeName, Node::new);
                agentNode.addEdge(topicNode);
            }
        }

        // Add all nodes to the graph
        this.addAll(nodesMap.values());
    }
}