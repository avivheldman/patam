package configs;

import test.Agent;
import test.Message;
import test.TopicManagerSingleton;

public class IncAgent implements Agent {
    private final String name;
    private final String[] subs;
    private final String[] pubs;

    public IncAgent(String[] subs, String[] pubs) {
        this.name = "IncAgent";
        this.subs = subs;
        this.pubs = pubs;

        // Subscribe to first topic from subs
        if (subs.length >= 1) {
            TopicManagerSingleton.get().getTopic(subs[0]).subscribe(this);
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
        // Nothing to reset
    }

    @Override
    public void callback(String topic, Message msg) {
        if (subs.length >= 1 && topic.equals(subs[0]) && pubs.length >= 1) {
            Double value = msg.asDouble;
            if (value != null) {
                Double result = value + 1;
                TopicManagerSingleton.get().getTopic(pubs[0]).publish(new Message(result));
            }
        }
    }

    @Override
    public void close() {
        // Unsubscribe from input topic
        if (subs.length >= 1) {
            TopicManagerSingleton.get().getTopic(subs[0]).unsubscribe(this);
        }

        // Remove as publisher
        if (pubs.length >= 1) {
            TopicManagerSingleton.get().getTopic(pubs[0]).removePublisher(this);
        }
    }
}