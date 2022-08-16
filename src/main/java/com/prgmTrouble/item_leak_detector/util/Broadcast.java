package com.prgmTrouble.item_leak_detector.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class Broadcast
{
    public static final Style NUMBER = Style.EMPTY.withColor(Formatting.GOLD),
                              TEXT   = Style.EMPTY.withColor(Formatting.GRAY),
                              ERR    = Style.EMPTY.withColor(Formatting.RED),
                              WARN   = Style.EMPTY.withColor(0xFFBB00);
    public static MutableText txt(final String s,final Style st) {return new LiteralText(s).setStyle(st);}
    public static MutableText asTxt(final String s) {return txt(s,TEXT);}
    public static MutableText asTxt(final char c) {return txt(Character.toString(c),TEXT);}
    public static MutableText asWarn(final String s) {return txt("[\u26A0] ",WARN).append(asTxt(s));}
    public static MutableText asErr(final String s) {return txt(s,ERR);}
    public static MutableText asNum(final int num) {return txt(Integer.toString(num),NUMBER);}
    public static MutableText asNum(final double num) {return txt(Double.toString(num),NUMBER);}
    public static void broadcast(final ServerWorld world,final Text...msg)
    {
        for(final ServerPlayerEntity p : world.getServer().getPlayerManager().getPlayerList())
            for(final Text t : msg)
                p.sendMessage(t,false);
    }
}