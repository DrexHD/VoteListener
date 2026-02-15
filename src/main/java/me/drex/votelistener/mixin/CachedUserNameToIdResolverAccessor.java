package me.drex.votelistener.mixin;

import me.drex.votelistener.duck.ICachedUserNameToIdResolver;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(CachedUserNameToIdResolver.class)
public abstract class CachedUserNameToIdResolverAccessor implements ICachedUserNameToIdResolver {
    @Shadow
    @Final
    private Map<String, Object> profilesByName;

    @Override
    public boolean voteListener$isCached(String username) {
        return profilesByName.containsKey(username.toLowerCase());
    }
}
