package me.drex.votelistener.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Config {

    public List<String> commands = new LinkedList<>() {{
        add("tellraw @a [{\"text\":\"${username}\",\"color\":\"blue\"},{\"text\":\" voted on \",\"color\":\"aqua\"},{\"text\":\"${serviceName}\",\"color\":\"blue\"}]");
    }};

    public List<String> onlineCommands = new LinkedList<>() {{
        add("give @s diamond 1");
    }};

    public Map<Integer, List<String>> milestones = new HashMap<>() {{
        put(5, new LinkedList<>() {{
            add("give @s apple 5");
        }});
        put(10, new LinkedList<>() {{
            add("give @s golden_apple 2");
        }});
    }};

}
