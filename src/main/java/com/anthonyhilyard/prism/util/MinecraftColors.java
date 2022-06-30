package com.anthonyhilyard.prism.util;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

public class MinecraftColors
{
	private static final Map<String, Integer> minecraftColorMap;

	static
	{
		Map<String, Integer> loadedColors = Maps.newHashMap();

		// Collect all of Minecraft's built-in colors.
		for (ChatFormatting color : ChatFormatting.values())
		{
			if (color.isColor())
			{
				loadedColors.put(ConfigHelper.formatColorName(color.getName()), color.getColor());
			}
		}

		minecraftColorMap = Map.copyOf(loadedColors);
	}

	public static TextColor getColor(String colorName)
	{
		colorName = ConfigHelper.formatColorName(colorName);
		TextColor result = null;
		if (minecraftColorMap.containsKey(colorName))
		{
			result = TextColor.fromRgb(minecraftColorMap.get(colorName));
		}
		return result;
	}
}
