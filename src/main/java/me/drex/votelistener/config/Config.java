package me.drex.votelistener.config;

import java.util.LinkedList;
import java.util.List;

public class Config {

    public List<String> commands = new LinkedList<>() {{
        add("tellraw @a [{\"text\":\"${username}\",\"color\":\"blue\"},{\"text\":\" voted on \",\"color\":\"aqua\"},{\"text\":\"${serviceName}\",\"color\":\"blue\"}]");
    }};

    public List<String> onlineCommands = new LinkedList<>() {{
        add("give @s diamond 1");
    }};

}
