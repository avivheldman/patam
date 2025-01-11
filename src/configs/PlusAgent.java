package configs;

import test.Agent;
import test.Message;
import test.TopicManagerSingleton;

public class PlusAgent implements Agent {
    private final String name;
    private final String[] subs;
    private final String[] pubs;
    private Double x = 0.0;
    private Double y = 0.0;

    public PlusAgent(String[] subs, String[] pubs) {
        this.name = "PlusAgent";
        this.subs = subs;
        this.pubs = pubs;

        // Subscribe to first two topics from subs
        if (subs.length >= 2) {
            TopicManagerSingleton.get().getTopic(subs[0]).subscribe(this);
            TopicManagerSingleton.get().getTopic(subs[1]).subscribe(this);
        }

        // Register as publisher for first topic in pubs
        if (pubs.length >= 1) {
            TopicManagerSingleton.get().getTopic(pubs[0]).addPublisher(this);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        x = 0.0;
        y = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (subs.length >= 2) {
            if (topic.equals(subs[0])) {
                x = msg.asDouble;
            } else if (topic.equals(subs[1])) {
                y = msg.asDouble;
            }

            // If both values are valid, calculate and publish result
            if (x != null && y != null && pubs.length >= 1) {
                Double result = x + y;
                TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(result));
            }
        }
    }

    @Override
    public void close() {
        // Unsubscribe from input topics
        if (subs.length >= 2) {
            TopicManagerSingleton.get().getTopic(subs[0]).unsubscribe(this);
            TopicManagerSingleton.get().getTopic(subs[1]).unsubscribe(this);
        }

        // Remove as publisher
        if (pubs.length >= 1) {
            TopicManagerSingleton.get().getTopic(pubs[0]).removePublisher(this);
        }
    }
}