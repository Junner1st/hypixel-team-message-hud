package org.junner.hypixelteammessagehud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.scores.PlayerTeam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class HypixelTeamMessageHudClient implements ClientModInitializer {
	public static final String MOD_ID = "hypixel-team-message-hud";
	private static final String MOD_NAME = "Hypixel Team Message HUD";
	private static final String LOG_PREFIX = "[" + MOD_NAME + "] ";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
	private static final int ACTION_BAR_REFRESH_TICKS = 40;

	private static String currentServerAddress = "";
	private static boolean connectedToHypixel;
	private static boolean bedWarsStarted;
	private static Component actionBarMessage;
	private static int actionBarTicksRemaining;
	private static int actionBarRefreshTicks;

	@Override
	public void onInitializeClient() {
		ModConfig.load();
		ClientTickEvents.END_CLIENT_TICK.register(HypixelTeamMessageHudClient::onEndTick);
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> onGameMessage(message));
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onJoin(client));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onDisconnect());
		log("loaded");
	}

	public static boolean shouldMirrorMessage(String plainText) {
		Minecraft client = Minecraft.getInstance();
		updateConnectionState(client);
		if (!ModConfig.isEnabled() || !connectedToHypixel || !bedWarsStarted || plainText == null || plainText.isBlank()) {
			return false;
		}
		if (client.player == null || client.level == null) {
			return false;
		}

		PlayerTeam team = client.player.getTeam();
		if (team == null) {
			return false;
		}

		Set<String> teamMembers = team.getPlayers().stream()
				.filter(USERNAME.asMatchPredicate())
				.map(name -> name.toLowerCase(Locale.ROOT))
				.collect(Collectors.toUnmodifiableSet());

		Set<String> messageNames = USERNAME.matcher(plainText).results()
				.map(match -> match.group().toLowerCase(Locale.ROOT))
				.collect(Collectors.toUnmodifiableSet());
		return teamMembers.stream().anyMatch(messageNames::contains);
	}

	public static void showActionBar(Component message) {
		actionBarMessage = message.copy().withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
		actionBarTicksRemaining = ModConfig.getActionBarDurationTicks();
		actionBarRefreshTicks = 0;
		sendActionBar(Minecraft.getInstance());
	}

	private static void onEndTick(Minecraft client) {
		if (actionBarMessage == null || actionBarTicksRemaining <= 0) {
			return;
		}

		actionBarTicksRemaining--;
		if (actionBarRefreshTicks-- <= 0) {
			actionBarRefreshTicks = ACTION_BAR_REFRESH_TICKS;
			sendActionBar(client);
		}
	}

	private static void onGameMessage(Component message) {
		updateConnectionState(Minecraft.getInstance());
		if (!connectedToHypixel) {
			return;
		}

		String plain = message.getString().trim();
		if (plain.equals("Protect your bed and destroy the enemy beds.")) {
			startBedWars();
		} else if (plain.startsWith("The game starts in ")
				|| plain.startsWith("The game is starting in ")
				|| plain.startsWith("Sending you to ")
				|| plain.equals("We don't have enough players! Start cancelled.")
				|| plain.equals("You have been eliminated!")) {
			resetGame();
		}
	}

	private static void onJoin(Minecraft client) {
		updateConnectionState(client);
		resetGame();
	}

	private static void onDisconnect() {
		if (connectedToHypixel) {
			log("leave hypixel detected");
		}
		currentServerAddress = "";
		connectedToHypixel = false;
		resetGame();
	}

	private static void startBedWars() {
		if (!bedWarsStarted) {
			bedWarsStarted = true;
			log("bedwar join detected");
		}
	}

	private static void resetGame() {
		if (bedWarsStarted) {
			log("bedwar leave detected");
		}
		bedWarsStarted = false;
		clearActionBar();
	}

	private static void clearActionBar() {
		actionBarMessage = null;
		actionBarTicksRemaining = 0;
		actionBarRefreshTicks = 0;
	}

	private static void sendActionBar(Minecraft client) {
		if (client.player != null && actionBarMessage != null) {
			client.player.sendOverlayMessage(actionBarMessage);
		}
	}

	private static void updateConnectionState(Minecraft client) {
		ServerData serverInfo = client.getCurrentServer();
		String address = serverInfo == null ? "" : serverInfo.ip;
		boolean hypixel = isHypixelAddress(address);

		if (!address.equals(currentServerAddress) || hypixel != connectedToHypixel) {
			if (!connectedToHypixel && hypixel) {
				log("join hypixel detected");
			} else if (connectedToHypixel && !hypixel) {
				log("leave hypixel detected");
				resetGame();
			}
			currentServerAddress = address;
			connectedToHypixel = hypixel;
		}
	}

	private static boolean isHypixelAddress(String address) {
		if (address == null || address.isBlank()) {
			return false;
		}

		String host = address.trim().toLowerCase(Locale.ROOT);
		if (host.endsWith(".")) {
			host = host.substring(0, host.length() - 1);
		}
		if (!host.startsWith("[") && host.chars().filter(character -> character == ':').count() == 1) {
			host = host.substring(0, host.indexOf(':'));
		}

		return host.equals("hypixel.net") || host.endsWith(".hypixel.net");
	}

	private static void log(String message, Object... args) {
		LOGGER.info(LOG_PREFIX + message, args);
	}
}
