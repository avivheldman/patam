package configs;

import test.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Node {
    private String name;
    private List<Node> edges; // List of connected nodes
    private Message msg; // Message associated with the node

    // Constructor
    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
    }

    // Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getEdges() {
        return edges;
    }

    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }

    // Method to add an edge to the node
    public void addEdge(Node node) {
        this.edges.add(node);
    }

    // Method to check if the graph contains cycles
    public boolean hasCycles() {
        Set<Node> visited = ConcurrentHashMap.newKeySet(); // Tracks nodes already visited
        Set<Node> recursionStack = ConcurrentHashMap.newKeySet(); // Tracks nodes in the current recursion stack

        return hasCyclesHelper(this, visited, recursionStack);
    }

    // Helper method to check for cycles recursively
    private boolean hasCyclesHelper(Node node, Set<Node> visited, Set<Node> recursionStack) {
        // If the node is already in the recursion stack, a cycle is detected
        if (recursionStack.contains(node)) {
            return true;
        }

        // If the node has already been visited, skip further processing
        if (visited.contains(node)) {
            return false;
        }

        // Mark the node as visited and add it to the recursion stack
        visited.add(node);
        recursionStack.add(node);

        // Recursively check all connected nodes for cycles
        for (Node neighbor : node.getEdges()) {
            if (hasCyclesHelper(neighbor, visited, recursionStack)) {
                return true;
            }
        }

        // Remove the node from the recursion stack after processing
        recursionStack.remove(node);

        return false;
    }
}
