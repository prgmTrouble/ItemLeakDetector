package com.prgmTrouble.item_leak_detector.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.prgmTrouble.item_leak_detector.command.ItemLeakCommand;
import com.prgmTrouble.item_leak_detector.command.PowerBlockCommand;
import com.prgmTrouble.item_leak_detector.command.SetItemVelocityCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds custom commands to the game. */
@Mixin(CommandManager.class)
public abstract class CommandManagerMixin
{
    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;
    
    @Inject(method = "<init>",at = @At("RETURN"))
    private void onRegister(final CommandManager.RegistrationEnvironment arg,final CallbackInfo ci)
    {
        ItemLeakCommand.register(dispatcher);
        SetItemVelocityCommand.register(dispatcher);
        PowerBlockCommand.register(dispatcher);
    }
}