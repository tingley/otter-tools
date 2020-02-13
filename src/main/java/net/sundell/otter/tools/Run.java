package net.sundell.otter.tools;

import java.util.Map;
import java.util.Properties;

import net.sundell.cauliflower.CLI;
import net.sundell.cauliflower.Command;
import net.sundell.cauliflower.UserData;

public class Run extends CLI {

    @Override
    protected UserData initializeUserData(Properties properties) {
        return null;
    }

    @Override
    protected void registerCommands(Map<String, Class<? extends Command>> commands) {
        commands.put("count", CountTUs.class);
        commands.put("copy", CopyHead.class);
        commands.put("replace", ReplaceTarget.class);
        commands.put("split", Split.class);
        commands.put("filter-lang", FilterLanguages.class);
    }

    @Override
    public String getName() {
        return "otter";
    }

    public static void main(String[] args) {
        new Run().run(args);
    }
}
