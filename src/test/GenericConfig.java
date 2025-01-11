package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class GenericConfig implements Config {
    private String configFile;
    private final List<Agent> agents = new ArrayList<>();

    public void setConfFile(String filename) {
        this.configFile = filename;
    }

    @Override
    public void create() {
        List<String> lines = new ArrayList<>();

        // Read all lines from config file
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Validate number of lines
        if (lines.size() % 3 != 0) {
            System.out.println("Invalid configuration file format");
            return;
        }

        // Process each agent configuration (3 lines per agent)
        for (int i = 0; i < lines.size(); i += 3) {
            try {
                // Get agent class name, subscriber topics and publisher topics
                String className = lines.get(i);
                String[] subs = lines.get(i + 1).isEmpty() ? new String[0] : lines.get(i + 1).split(",");
                String[] pubs = lines.get(i + 2).isEmpty() ? new String[0] : lines.get(i + 2).split(",");

                // Create agent instance using reflection
                Class<?> agentClass = Class.forName(className);
                Constructor<?> constructor = agentClass.getConstructor(String[].class, String[].class);
                Agent agent = (Agent) constructor.newInstance((Object) subs, (Object) pubs);

                // Wrap with ParallelAgent and store
                ParallelAgent parallelAgent = new ParallelAgent(agent);
                agents.add(parallelAgent);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName() {
        return "Generic Config";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void close() {
        for (Agent agent : agents) {
            agent.close();
        }
        agents.clear();
    }
}