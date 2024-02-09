package me.drex.votelistener.mixin;

import me.drex.votelistener.VoteListener;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(
        method = "saveEverything",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;saveAll()V"
        )
    )
    private void saveVoteData(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        VoteListener.saveData((MinecraftServer) (Object) this);
    }

}
