package org.junner.hypixelteammessagehud.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.junner.hypixelteammessagehud.HypixelTeamMessageHudClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public final class ChatHudMixin {
	@Inject(
			method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
			at = @At("HEAD")
	)
	private void hypixelTeamMessageHud$mirrorTeamChat(Text message, @Nullable MessageSignatureData signature,
													  @Nullable MessageIndicator indicator, CallbackInfo ci) {
		if (HypixelTeamMessageHudClient.shouldMirrorMessage(message.getString())) {
			HypixelTeamMessageHudClient.showActionBar(message);
		}
	}
}
