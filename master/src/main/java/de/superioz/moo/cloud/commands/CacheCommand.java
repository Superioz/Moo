package de.superioz.moo.cloud.commands;

import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.cloud.Cloud;

public class CacheCommand {

    @ArgumentHelp
    public void argumentHelp(ArgumentHelper helper) {
        helper.react(0, () -> {
            String dbTypes = "Available types: {%s}";
            dbTypes = String.format(dbTypes, String.join(", ", StringUtil.getStringList(DatabaseType.values(),
                    type1 -> type1.ordinal() + ": " + type1.name().toLowerCase())));
            helper.setHelpMessages(dbTypes);
        });
    }

    @TabCompletion
    public void tabComplete(TabCompletor completor) {
        completor.react(1, StringUtil.getStringList(DatabaseType.values(), type -> type.name().toLowerCase()));
    }

    @Command(label = "cachelist", usage = "<database>")
    public void onCacheList(CommandContext context, ParamSet args) {
        // list the database
        DatabaseType type = args.getEnum(0, DatabaseType.class);
        context.invalidArgument(type == null, true, "This type doesn't exist!");

        DatabaseCollection collection = Cloud.getInstance().getDatabaseCollection(type);
        context.invalidArgument(!collection.isCacheable(), "This collection is not cachable!");
        DatabaseCache cache = collection.getCache();

        context.sendMessage("Entry list of cache '" + type + "': " + (cache.asList().isEmpty() ? "Nothing to display!" : ""));
        for(Object entry : cache.asList()) {
            context.sendMessage("- " + entry);
        }
    }

}
