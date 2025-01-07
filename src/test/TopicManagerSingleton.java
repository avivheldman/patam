package test;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;


public class TopicManagerSingleton {

    // Static inner class - loaded only when first accessed
    private static class TopicManager {
        // Single instance - thread-safe due to static initialization
        private static final TopicManager instance = new TopicManager();

        // Thread-safe map for storing topics
        private final ConcurrentHashMap<String, Topic> topics;

        // Private constructor - can only be called from within the class
        private TopicManager() {
            topics = new ConcurrentHashMap<>();
        }

        /**
         * Gets or creates a topic by name
         * @param name The topic name
         * @return The topic instance
         */
        public Topic getTopic(String name) {
            return topics.computeIfAbsent(name, Topic::new);
        }

        /**
         * Gets all topics
         * @return Collection of all topics
         */
        public Collection<Topic> getTopics() {
            return topics.values();
        }

        /**
         * Clears all topics
         */
        public void clear() {
            topics.clear();
        }
    }

    /**
     * Returns the single instance of TopicManager
     * @return The TopicManager instance
     */
    public static TopicManager get() {
        return TopicManager.instance;
    }
}