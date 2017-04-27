package de.superioz.moo.proxy.command;

import de.superioz.moo.api.command.param.ParamType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerParamType extends ParamType<ProxiedPlayer> {

    @Override
    public String label() {
        return "player";
    }

    @Override
    public ProxiedPlayer resolve(String s) {
        return ProxyServer.getInstance().getPlayer(s);
    }

    @Override
    public Class<ProxiedPlayer> typeClass() {
        return ProxiedPlayer.class;
    }

    @Override
    public boolean checkCustom(String arg, String s) {
        if(s.equalsIgnoreCase("online")){
            return resolve(arg) != null;
        }
        return false;
    }

    @Override
    public String handleCustomException(String s) {
        if(s.equalsIgnoreCase("online")){
            return "This player isn't online!";
        }
        return null;
    }
}
