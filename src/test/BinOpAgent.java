package test;
import java.util.function.BinaryOperator;

public class BinOpAgent implements Agent {
    private final String name;
    private final String inputTopic1;
    private final String inputTopic2;
    private final String outputTopic;
    private final BinaryOperator<Double> operation;

    private Double value1 = null; // Stores the latest value from inputTopic1
    private Double value2 = null; // Stores the latest value from inputTopic2

    public BinOpAgent(String name, String inputTopic1, String inputTopic2, String outputTopic, BinaryOperator<Double> operation) {
        this.name = name;
        this.inputTopic1 = inputTopic1;
        this.inputTopic2 = inputTopic2;
        this.outputTopic = outputTopic;
        this.operation = operation;

        // Subscribe to both input topics
        TopicManagerSingleton.get().getTopic(inputTopic1).subscribe(this);
        TopicManagerSingleton.get().getTopic(inputTopic2).subscribe(this);

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        value1 = null;
        value2 = null;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (topic.equals(inputTopic1)) {
            value1 = msg.asDouble;
        } else if (topic.equals(inputTopic2)) {
            value2 = msg.asDouble;
        }

        // Perform operation if both values are available
        if (value1 != null && value2 != null) {
            Double result = operation.apply(value1, value2);
            TopicManagerSingleton.get().getTopic(outputTopic).publish(new Message(result));
        }
    }

    @Override
    public void close() {
        // Optional: Unsubscribe from topics if needed
        TopicManagerSingleton.get().getTopic(inputTopic1).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(inputTopic2).unsubscribe(this);
    }
}
