import be.seeseemelk.mockbukkit.MockCommandMap;
import be.seeseemelk.mockbukkit.help.HelpMapMock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpMap;

import java.util.Arrays;

public class ServerMock extends be.seeseemelk.mockbukkit.ServerMock {

    public ServerMock() {
        super();
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine) {

        assertMainThread();
        String[] commands = commandLine.split(" ");
        String commandLabel = commands[0];
        String[] args = Arrays.copyOfRange(commands, 1, commands.length);
        Command command = getCommandMap().getCommand(commandLabel);
        if (command != null)
            return command.execute(sender, commandLabel, args);
        else
            return false;

    }

    public MockCommandMap getCommandMap() {

        return super.getCommandMap();
    }

    @Override
    public HelpMap getHelpMap() {

        return new HelpMapMock();
    }
}
