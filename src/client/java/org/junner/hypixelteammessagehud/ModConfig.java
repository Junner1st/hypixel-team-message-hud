package org.junner.hypixelteammessagehud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {
	public static final boolean DEFAULT_ENABLED = true;
	public static final int DEFAULT_ACTION_BAR_DURATION_TICKS = 80;
	public static final int MIN_ACTION_BAR_DURATION_TICKS = 20;
	public static final int MAX_ACTION_BAR_DURATION_TICKS = 400;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve(HypixelTeamMessageHudClient.MOD_ID + ".json");

	private static Data data = new Data();

	private ModConfig() {
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			Data loaded = GSON.fromJson(reader, Data.class);
			data = loaded == null ? new Data() : loaded;
			data.clamp();
		} catch (IOException exception) {
			data = new Data();
		}
		save();
	}

	public static void save() {
		data.clamp();
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(data, writer);
			}
		} catch (IOException ignored) {
		}
	}

	public static int getActionBarDurationTicks() {
		data.clamp();
		return data.actionBarDurationTicks;
	}

	public static boolean isEnabled() {
		return data.enabled;
	}

	public static void setEnabled(boolean enabled) {
		data.enabled = enabled;
	}

	public static void setActionBarDurationTicks(int actionBarDurationTicks) {
		data.actionBarDurationTicks = actionBarDurationTicks;
		data.clamp();
	}

	private static final class Data {
		private boolean enabled = DEFAULT_ENABLED;
		private int actionBarDurationTicks = DEFAULT_ACTION_BAR_DURATION_TICKS;

		private void clamp() {
			actionBarDurationTicks = Math.clamp(
					actionBarDurationTicks,
					MIN_ACTION_BAR_DURATION_TICKS,
					MAX_ACTION_BAR_DURATION_TICKS
			);
		}
	}
}
