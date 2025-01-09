package test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class Node {
    private String name;
    private List<Node> edges;
    private Message msg;

    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
    }

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

    public void addEdge(Node node) {
        this.edges.add(node);
    }

    public boolean hasCycles() {
        Set<Node> visited = ConcurrentHashMap.newKeySet(); // ConcurrentHashMap as required
        Set<Node> recursionStack = ConcurrentHashMap.newKeySet(); // ConcurrentHashMap as required
        return hasCyclesHelper(this, visited, recursionStack);
    }

    private boolean hasCyclesHelper(Node node, Set<Node> visited, Set<Node> recursionStack) {
        if (recursionStack.contains(node)) {
            return true; // Cycle detected!
        }

        if (visited.contains(node)) {
            return false; // Already visited, no cycle in this path
        }

        visited.add(node);
        recursionStack.add(node);

        for (Node neighbor : node.getEdges()) {
            if (hasCyclesHelper(neighbor, visited, recursionStack)) {
                return true; // Cycle detected in a neighbor's path
            }
        }

        recursionStack.remove(node); // Backtrack: remove from recursion stack
        return false; // No cycle found in this path
    }
}