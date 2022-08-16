package com.prgmTrouble.item_leak_detector.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.Vec3d;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import static com.prgmTrouble.item_leak_detector.util.Broadcast.*;
import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class SetItemVelocityCommand
{
    //TODO figure out how to send this crap to the client
    public static final class Velocity
    {
        public byte relative;
        public double x,y,z;
        Velocity(final byte relative,final double x,final double y,final double z)
        {
            this.relative = relative;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    private static final record CoordArg(boolean ignore,boolean rel,double val) implements Serializable {}
    private static final class SetVArg implements ArgumentType<CoordArg>,Serializable
    {
        public static CoordArg getV(final CommandContext<ServerCommandSource> c,final String name)
        {
            return c.getArgument(name,CoordArg.class);
        }
        static boolean eatIgnore(final StringReader reader)
        {
            final boolean ignore = reader.peek() == '^';
            if(ignore) reader.skip();
            return ignore;
        }
        static boolean eatRel(final StringReader reader)
        {
            final boolean rel = reader.peek() == '~';
            if(rel) reader.skip();
            return rel;
        }
        static double eatPos(final StringReader reader,final boolean rel) throws CommandSyntaxException
        {
            final int begin = reader.getCursor();
            final double val;
            if(!reader.canRead() || reader.peek() == ' ')
            {
                if(!rel)
                {
                    reader.setCursor(begin);
                    throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
                }
                val = 0;
            }
            else
                val = reader.readDouble();
            return val;
        }
        @Override
        public CoordArg parse(final StringReader reader) throws CommandSyntaxException
        {
            if(!reader.canRead()) throw CoordinateArgument.MISSING_COORDINATE.createWithContext(reader);
            if(eatIgnore(reader))
            {
                if(reader.canRead() && reader.peek() != ' ')
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
                return new CoordArg(true,false,0);
            }
            final boolean rx = eatRel(reader);
            return new CoordArg(false,rx,eatPos(reader,rx));
        }
    
        static final String[] suggestions = {"~","^"};
        static final Collection<String> suggestionsSet = Set.of(suggestions);
        @Override public Collection<String> getExamples() {return suggestionsSet;}
    }
    static {ArgumentTypes.register("velocity",SetVArg.class,new ConstantArgumentSerializer<>(SetVArg::new));}
    public static Velocity velocityModifier = new Velocity((byte)0b111,0,0,0);
    private static MutableText settingText()
    {
        return
            txt("[",TEXT).
                append(txt((((velocityModifier.relative&(byte)0b001) == (byte)0)? "":"~")+ velocityModifier.x,NUMBER)).
                append(txt(",",TEXT)).
                append(txt((((velocityModifier.relative&(byte)0b010) == (byte)0)? "":"~")+ velocityModifier.y,NUMBER)).
                append(txt(",",TEXT)).
                append(txt((((velocityModifier.relative&(byte)0b100) == (byte)0)? "":"~")+ velocityModifier.z,NUMBER)).
                append(txt("]",TEXT));
    }
    private static int readSettings(final CommandContext<ServerCommandSource> c)
    {
        broadcast(c.getSource().getWorld(),txt("Current setting: ",TEXT).append(settingText()));
        return 1;
    }
    private static CoordArg tryGet(final CommandContext<ServerCommandSource> c,final String arg)
    {
        try {return SetVArg.getV(c,arg);}
        catch(final IllegalArgumentException ignored) {return null;}
    }
    private static int writeSettings(final CommandContext<ServerCommandSource> c)
    {
        final CoordArg vx = SetVArg.getV(c,"vx"),
                       vy = tryGet(c,"vy"),
                       vz = vy == null? null : tryGet(c,"vz");
        velocityModifier.relative = (byte)((              !vx.ignore? vx.rel? (byte)0b001:(byte)0:(velocityModifier.relative&(byte)0b001)) |
                                           (vy != null && !vy.ignore? vy.rel? (byte)0b010:(byte)0:(velocityModifier.relative&(byte)0b010)) |
                                           (vz != null && !vz.ignore? vz.rel? (byte)0b100:(byte)0:(velocityModifier.relative&(byte)0b100)));
        if(              !vx.ignore) velocityModifier.x = vx.val;
        if(vy != null && !vy.ignore) velocityModifier.y = vy.val;
        if(vz != null && !vz.ignore) velocityModifier.z = vz.val;
        broadcast(c.getSource().getWorld(),txt("Setting updated to: ",TEXT).append(settingText()));
        return 1;
    }
    public static Vec3d modify(final double x,final double y,final double z)
    {
        return new Vec3d
        (
            (velocityModifier.relative & (byte)0b001) != (byte)0
                ? velocityModifier.x + x
                : velocityModifier.x,
            (velocityModifier.relative & (byte)0b010) != (byte)0
                ? velocityModifier.y + y
                : velocityModifier.y,
            (velocityModifier.relative & (byte)0b100) != (byte)0
                ? velocityModifier.z + z
                : velocityModifier.z
        );
    }
    public static void register(final CommandDispatcher<ServerCommandSource> dispatcher)
    {
        // I have no idea why the 'suggests' function disconnects the client. WTF Mojang?
        dispatcher.register
        (
            literal("itemVelocity").
                executes(SetItemVelocityCommand::readSettings).
                then
                (
                    argument("vx",new SetVArg()).
                    //suggests((c,b) -> suggestMatching(SetVArg.suggestions,b)).
                    executes(SetItemVelocityCommand::writeSettings).
                    then
                    (
                        argument("vy",new SetVArg()).
                        //suggests((c,b) -> suggestMatching(SetVArg.suggestions,b)).
                        executes(SetItemVelocityCommand::writeSettings).
                        then
                        (
                            argument("vz",new SetVArg()).
                            //suggests((c,b) -> suggestMatching(SetVArg.suggestions,b)).
                            executes(SetItemVelocityCommand::writeSettings)
                        )
                    )
                )
        );
    }
}