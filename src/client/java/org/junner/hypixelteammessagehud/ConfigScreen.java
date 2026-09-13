package org.junner.hypixelteammessagehud;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.EmptyEntry;
import me.shedaniel.clothconfig2.gui.widget.SearchFieldEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ConfigScreen {
	private ConfigScreen() {
	}

	public static Screen create(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal("Hypixel Team Message HUD"))
				.setSavingRunnable(ModConfig::save);
		builder.setGlobalized(false);
		builder.setAfterInitConsumer(ConfigScreen::removeSearchField);

		ConfigEntryBuilder entries = builder.entryBuilder();
		ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

		general.addEntry(entries.startBooleanToggle(
						Component.literal("Enabled"),
						ModConfig.isEnabled()
				)
				.setDefaultValue(ModConfig.DEFAULT_ENABLED)
				.setTooltip(Component.literal("Mirror matching Hypixel Bed Wars messages to the action bar."))
				.setSaveConsumer(ModConfig::setEnabled)
				.build());

		general.addEntry(entries.startIntField(
						Component.literal("Action bar duration"),
						ModConfig.getActionBarDurationTicks()
				)
				.setDefaultValue(ModConfig.DEFAULT_ACTION_BAR_DURATION_TICKS)
				.setMin(ModConfig.MIN_ACTION_BAR_DURATION_TICKS)
				.setMax(ModConfig.MAX_ACTION_BAR_DURATION_TICKS)
				.setTooltip(Component.literal("How long mirrored messages stay visible, in ticks."))
				.setSaveConsumer(ModConfig::setActionBarDurationTicks)
				.build());

		return builder.build();
	}

	private static void removeSearchField(Screen screen) {
		if (!(screen instanceof ClothConfigScreen clothConfigScreen)) {
			return;
		}

		List<?> entries = clothConfigScreen.listWidget.children();
		for (int index = 0; index < entries.size(); index++) {
			if (!(entries.get(index) instanceof SearchFieldEntry)) {
				continue;
			}

			entries.remove(index);
			if (index > 0 && entries.get(index - 1) instanceof EmptyEntry) {
				entries.remove(index - 1);
				index--;
			}
			if (index < entries.size() && entries.get(index) instanceof EmptyEntry) {
				entries.remove(index);
			}
			return;
		}
	}
}
