package com.prgmTrouble.item_leak_detector.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public final class Broadcast
{
    public static final Style NUMBER = Style.EMPTY.withColor(Formatting.GOLD),
                              TEXT   = Style.EMPTY.withColor(Formatting.GRAY);
    public static MutableText txt(final String s,final Style st) {return new LiteralText(s).setStyle(st);}
    private static final UUID NIL = new UUID(0L,0L);
    public static void broadcast(final ServerWorld world,final Text...msg)
    {
        for(final PlayerEntity p : world.getPlayers())
            for(final Text t : msg)
               p.sendSystemMessage(t,NIL);
    }
}