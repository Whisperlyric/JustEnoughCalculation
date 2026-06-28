package me.towdium.jecalculation.mixin;

import com.mojang.authlib.GameProfile;
import me.towdium.jecalculation.data.structure.RecordPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements JecaPlayerRecordAccessor {

    @Unique
    private RecordPlayer Jeca_record = new RecordPlayer();

    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
    public void onAddData(ValueOutput output, CallbackInfo ci) {
        output.store("Jeca_record", CompoundTag.CODEC, Jeca_record.serialize());
    }

    @Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
    public void onReadData(ValueInput input, CallbackInfo ci) {
        Jeca_record.deserialize(input.read("Jeca_record", CompoundTag.CODEC).orElseGet(CompoundTag::new));
    }

    @Inject(at = @At("HEAD"), method = "restoreFrom")
    public void onRestore(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        Jeca_record = ((JecaPlayerRecordAccessor) oldPlayer).Jeca_getRecord();
    }

    @Override
    public RecordPlayer Jeca_getRecord() {
        return Jeca_record;
    }
}
