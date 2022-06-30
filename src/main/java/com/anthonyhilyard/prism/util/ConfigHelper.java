package com.anthonyhilyard.prism.util;

import net.minecraft.network.chat.TextColor;

public class ConfigHelper
{
	/**
	 * Parses an arbitrary object to determine the intended text color.  Valid object types are strings and numbers.
	 * Useful in configuration files where colors are being specified.
	 * Valid string formats: "0xAARRGGBB", "0xRRGGBB", "#AARRGGBB", "#RRGGBB", "<color name>"
	 * 
	 * @param value The object representing a color.
	 * @return The parse color, or null if a color couldn't be parsed.
	 */
	public static TextColor parseColor(Object value, boolean allowAlpha)
	{
		TextColor color = null;
		if (value instanceof String string)
		{
			// Parse string color.
			String colorString = string.toLowerCase().replace("0x", "").replace("#", "");
			color = TextColor.parseColor(colorString);
			if (color == null)
			{
				if (colorString.length() == 6 || colorString.length() == 8)
				{
					color = TextColor.parseColor("#" + colorString);
				}
			}
		}
		else if (value instanceof Number number)
		{
			color = TextColor.fromRgb(number.intValue());
		}

		// If alpha is 0 but the color isn't 0x00000000, assume alpha is intended to be 0xFF.
		// Only downside is if users want black borders they'd have to specify "0xFF000000".
		if (color != null && color.getValue() > 0 && color.getValue() <= 0xFFFFFF)
		{
			color = TextColor.fromRgb(color.getValue() | (0xFF << 24));
		}

		// If alpha is disallowed, always force it to 0xFF.
		if (!allowAlpha && color.getValue() > 0xFFFFFF)
		{
			color = TextColor.fromRgb(color.getValue() | (0xFF << 24));
		}
		
		return color;
	}

	public static TextColor parseColor(Object value)
	{
		return parseColor(value, true);
	}

	public static boolean validateColor(Object value)
	{
		return parseColor(value) != null;
	}

	static String formatColorName(String input)
	{
		// Remove spaces and underscores and make lowercase.
		// "Dark Blue" -> "darkblue", "DARK_BLUE" -> "darkblue"
		return input.toLowerCase().replace(" ", "").replace("_", "");
	}
}
