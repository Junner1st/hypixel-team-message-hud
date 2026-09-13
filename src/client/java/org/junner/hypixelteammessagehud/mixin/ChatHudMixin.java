package org.junner.hypixelteammessagehud.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.junner.hypixelteammessagehud.HypixelTeamMessageHudClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public final class ChatHudMixin {
	@Inject(
			method = "addServerSystemMessage(Lnet/minecraft/network/chat/Component;)V",
			at = @At("HEAD")
	)
	private void hypixelTeamMessageHud$mirrorTeamChat(Component message, CallbackInfo ci) {
		if (HypixelTeamMessageHudClient.shouldMirrorMessage(message.getString())) {
			HypixelTeamMessageHudClient.showActionBar(message);
		}
	}
}
