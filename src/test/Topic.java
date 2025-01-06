package test;

public class Topic {
    public final String name;
    private final List<Agent> subs;    // subscribers list
    private final List<Agent> pubs;    // publishers list

    Topic(String name) {
        this.name = name;
        this.subs = new ArrayList<>();
        this.pubs = new ArrayList<>();
    }

    /**
     * Subscribe an agent to receive messages from this topic
     * @param a The agent to subscribe
     */
    public void subscribe(Agent a) {
        if (a != null && !subs.contains(a)) {
            subs.add(a);
        }
    }

    /**
     * Unsubscribe an agent from this topic
     * @param a The agent to unsubscribe
     */
    public void unsubscribe(Agent a) {
        if (a != null) {
            subs.remove(a);
        }
    }

    /**
     * Publish a message to all subscribers
     * @param m The message to publish
     */
    public void publish(Message m) {
        if (m != null) {
            for (Agent subscriber : subs) {
                subscriber.callback(this, m);
            }
        }
    }

    /**
     * Add an agent as a publisher to this topic
     * @param a The agent to add as publisher
     */
    public void addPublisher(Agent a) {
        if (a != null && !pubs.contains(a)) {
            pubs.add(a);
        }
    }

    /**
     * Remove an agent from publishers list
     * @param a The agent to remove
     */
    public void removePublisher(Agent a) {
        if (a != null) {
            pubs.remove(a);
        }
    }
}