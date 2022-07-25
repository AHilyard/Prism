package com.anthonyhilyard.prism.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.anthonyhilyard.prism.text.DynamicColor;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.TextColor;

public class ConfigHelper
{
	public static record ColorFormatDocumentation(String name, String description, List<String> examples)
	{
		public ColorFormatDocumentation(String name, String description, String... examples) { this(name, description, Arrays.asList(examples)); }
	}

	public static List<ColorFormatDocumentation> colorFormatDocumentation()
	{
		return colorFormatDocumentation(false);
	}

	public static List<ColorFormatDocumentation> colorFormatDocumentation(boolean forKey)
	{
		if (!forKey)
		{
			return Arrays.asList(
				new ColorFormatDocumentation("Hex color code", "A hex color code is preceded by # or 0x and must be quoted.  Supports 3, 4, 6, or 8 digit codes in the formats RGB, ARGB, RRGGBB, AARRGGBB.",
											"\"#F4C\"", "\"0xFEE0\"", "\"#40FF2E\"", "\"#CC00E2EE\""),
				new ColorFormatDocumentation("Decimal color code", "A decimal color code, which is just a hex color code converted to decimal.  May or may not be quoted.",
											"15614720", "\"4278251143\""),
				new ColorFormatDocumentation("Minecraft color name", "One of the standard 16 Minecraft color names.  Must be quoted.",
											"\"red\"", "\"dark_purple\"", "\"gold\""),
				new ColorFormatDocumentation("Web color name", "One of the standard 140 web/HTML color names or \"transparent\".  Must be quoted.",
											"\"chartreuse\"", "\"darkorange\"", "\"deeppink\"", "\"deepskyblue\""),
				new ColorFormatDocumentation("Modifiers", "Colors specified in any of the above formats can be modified by using modifiers.\n" +
															"Modifiers are specified after any color in the format \"<+, -, or =><h, s, v, r, g, b, or a><amount>\".\n" +
															"The letters represent h - hue, s - saturation, v - value, r - red, g - green, b - blue, a - alpha.\n" +
															"Valid amounts are 1 to 255 for all types except hue, which accepts 1 to 359.",
											"\"red+h15\"", "\"#saddlebrown-v20+s5\"", "\"10_aqua_aqua+v15-h5\"", "\"#F4C-r15-v10=a40\""),
				new ColorFormatDocumentation("Animated color", "An animated color that fades from one to another in sequence.\n" +
															"A string in the format \"<duration in seconds>_<list of color definitions separated by underscores>\".  Must be quoted.",
											"\"10_black_#7FFF00\"", "\"5.5_gold_orange_orangered\"", "\"20_red_orange_yellow_green_blue_purple\"")
			);
		}
		else
		{
			return Arrays.asList(
				new ColorFormatDocumentation("Hex color code", "A hex color code is preceded by # or 0x and must be quoted.  Supports 3, 4, 6, or 8 digit codes in the formats RGB, ARGB, RRGGBB, or AARRGGBB.",
											"\"#F4C\"", "\"0xFEE0\"", "\"#40FF2E\"", "\"#CC00E2EE\""),
				new ColorFormatDocumentation("Decimal color code", "A decimal color code, which is just a hex color code converted to decimal.  May or may not be quoted.",
											"15614720", "\"4278251143\""),
				new ColorFormatDocumentation("Minecraft color name", "One of the standard 16 Minecraft color names.  May or may not be quoted.",
											"\"red\"", "\"dark_purple\"", "\"gold\""),
				new ColorFormatDocumentation("Web color name", "One of the standard 140 web/HTML color names or \"transparent\".  May or may not be quoted.",
											"\"chartreuse\"", "\"darkorange\"", "\"deeppink\"", "\"deepskyblue\""),
				new ColorFormatDocumentation("Modifiers", "Colors specified in any of the above formats can be modified by using modifiers.\n" +
															"Modifiers are specified after any color in the format \"<+, -, or =><h, s, v, r, g, b, or a><amount>\".\n" +
															"The letters represent h - hue, s - saturation, v - value, r - red, g - green, b - blue, a - alpha.\n" +
															"Valid amounts are 1 to 255 for all types except hue, which accepts 1 to 359.\n",
															"Using any modifiers will REQUIRE the key to be quoted.\n",
											"\"red+h15\"", "\"#saddlebrown-v20+s5\"", "\"10_aqua_aqua+v15-h5\"", "\"#F4C-r15-v10=a40\""),
				new ColorFormatDocumentation("Animated color", "An animated color that fades from one to another in sequence.\n" +
															"A string in the format \"<duration in seconds>_<list of color definitions separated by underscores>\".  May or may not be quoted.",
											"\"10_black_#7FFF00\"", "\"5.5_gold_orange_orangered\"", "\"20_red_orange_yellow_green_blue_purple\"")
			);
		}
	}


	public static TextColor applyModifiers(List<String> modifiers, TextColor color)
	{
		return TextColor.fromRgb(applyModifiers(modifiers, DynamicColor.fromRgb(color.getValue())).getValue());
	}

	public static DynamicColor applyModifiers(List<String> modifiers, DynamicColor color)
	{
		final Map<Character, BiFunction<Integer, Integer, Integer>> modifierFuncs = Map.of(
				'+', (v, a) -> v + a,
				'-', (v, a) -> v - a,
				'=', (v, a) -> a
			);

		// Now apply all specified modifiers.
		for (String modifier : modifiers)
		{
			if (modifier.length() < 3)
			{
				continue;
			}

			char type = modifier.toLowerCase().charAt(1);
			int amount;
			BiFunction<Integer, Integer, Integer> mod;
			try
			{
				amount = Integer.parseInt(modifier.substring(2));
				mod = modifierFuncs.get(modifier.charAt(0));
			}
			catch (Exception e)
			{
				continue;
			}

			if (mod == null)
			{
				continue;
			}

			switch (type)
			{
				case 'h':
					color = DynamicColor.fromAHSV(color.alpha(), mod.apply(color.hue(), amount), color.saturation(), color.value());
					break;
				case 's':
					color = DynamicColor.fromAHSV(color.alpha(), color.hue(), mod.apply(color.saturation(), amount), color.value());
					break;
				case 'v':
					color = DynamicColor.fromAHSV(color.alpha(), color.hue(), color.saturation(), mod.apply(color.value(), amount));
					break;
				case 'r':
					color = DynamicColor.fromARGB(color.alpha(), mod.apply(color.red(), amount), color.green(), color.blue());
					break;
				case 'g':
					color = DynamicColor.fromARGB(color.alpha(), color.red(), mod.apply(color.green(), amount), color.blue());
					break;
				case 'b':
					color = DynamicColor.fromARGB(color.alpha(), color.red(), color.green(), mod.apply(color.blue(), amount));
					break;
				case 'a':
					color = DynamicColor.fromARGB(mod.apply(color.alpha(), amount), color.red(), color.green(), color.blue());
					break;
				default:
					continue;
			}
		}
		return color;
	}

	/**
	 * Parses an arbitrary object to determine the intended text color.  Valid object types are strings, integral numbers, and lists.
	 * Useful in configuration files where colors are being specified.
	 * Valid string formats: "0xAARRGGBB", "0xRRGGBB", "#AARRGGBB", "#RRGGBB", "<color name>"
	 * 
	 * To specify a dynamic color, a list of values can be specified with the following formats:
	 * [<duration in seconds>, <one or more color values as above>]
	 * OR
	 * "<duration in seconds>_<one or more color values as above separated by underscores>"
	 * 
	 * @param value The object representing a color.
	 * @return The parse color, or null if a color couldn't be parsed.
	 */
	public static IColor parseColor(Object value, boolean allowAlpha)
	{
		List<Object> unparsedColors = null;
		List<IColor> colors = Lists.newArrayList();
		float duration = 10.0f;

		if (value instanceof String string && string.contains("_"))
		{
			List<String> entries = List.of(string.split("_"));
			try
			{
				duration = Float.parseFloat(entries.get(0));
				unparsedColors = entries.stream().skip(1).map(s -> (Object)s).toList();
			}
			catch (NumberFormatException e)
			{
				// The first entry isn't a valid float, so it's malformed.
				// We'll continue assuming it's a valid color value.
				unparsedColors = entries.stream().map(s -> (Object)s).toList();
			}
		}
		else if (value instanceof List<?> list && !list.isEmpty())
		{
			List<Object> entries = null;
			try
			{
				entries = Collections.unmodifiableList(list);
				if (entries.get(0) instanceof String string)
				{
					duration = Float.parseFloat((String)entries.get(0));
				}
				else if (entries.get(0) instanceof Number number)
				{
					duration = number.floatValue();
				}
				
				unparsedColors = entries.subList(1, entries.size());
			}
			catch (NumberFormatException e)
			{
				// The first entry isn't a valid float, so it's malformed.
				// We'll continue assuming it's a valid color value.
				unparsedColors = entries;
			}
			catch (ClassCastException e)
			{
				// The list cannot be cast to a list of objects for some reason.
				// Do nothing.
			}
		}
		else
		{
			unparsedColors = Lists.newArrayList();
			unparsedColors.add(value);
		}

		// Didn't find any valid colors, so bail.
		if (unparsedColors == null || unparsedColors.isEmpty())
		{
			return null;
		}

		for (Object unparsedValue : unparsedColors)
		{
			if (unparsedValue == null)
			{
				return null;
			}

			boolean isTransparent = false;
			TextColor color = null;
			List<String> modifiers = List.of();
			if (unparsedValue instanceof String string)
			{
				// First parse modifiers.
				string = string.replace("~", "").replace("-", "~-").replace("+", "~+");
				if (string.contains("~"))
				{
					modifiers = Lists.newArrayList(string.split("~"));
					if (modifiers.size() > 1)
					{
						string = modifiers.remove(0);
					}
				}

				// Parse string color.  First check for color codes.
				if (isValidColorCode(string))
				{
					boolean isHex = string.startsWith("0x") || string.startsWith("#");
					String colorString = string.toLowerCase().replace("0x", "").replace("#", "");
					
					// Double shorthand codes if needed.
					if (isHex && colorString.length() == 3 || colorString.length() == 4)
					{
						colorString = colorString.replaceAll(".", "$0$0");
					}
					color = TextColor.parseColor("#" + colorString);
				}
				// This wasn't a color code, so try Minecraft colors and then web colors.
				else
				{
					// Transparent is a special case.
					if (string.toLowerCase().contentEquals("transparent"))
					{
						isTransparent = true;
						color = TextColor.fromRgb(0);
					}
					// Check if this is a standard minecraft color and if not, a web color.
					else
					{
						color = MinecraftColors.getColor(string);

						if (color == null)
						{
							color = (TextColor)WebColors.getColor(string);
						}
					}
				}
			}
			else if (unparsedValue instanceof Number number)
			{
				color = TextColor.fromRgb(number.intValue());
			}

			// If alpha is 0 but the color isn't 0x00000000, assume alpha is intended to be 0xFF.
			// Only downside is if users want black borders they'd have to specify "0xFF000000".
			if (color != null && !isTransparent && color.getValue() > 0 && color.getValue() <= 0xFFFFFF)
			{
				color = TextColor.fromRgb(color.getValue() | (0xFF << 24));
			}

			if (color != null)
			{
				color = applyModifiers(modifiers, color);
			}

			// If alpha is disallowed, always force it to 0xFF.
			if (!allowAlpha && color.getValue() > 0xFFFFFF)
			{
				color = TextColor.fromRgb(color.getValue() | (0xFF << 24));
			}

			if (color != null)
			{
				colors.add((IColor)color);
			}
		}

		if (colors.size() == 1)
		{
			return colors.get(0);
		}
		else if (colors.size() > 1)
		{
			return new DynamicColor(colors, duration);
		}

		return null;
	}

	public static IColor parseColor(Object value)
	{
		return parseColor(value, true);
	}

	public static boolean validateColor(Object value)
	{
		return parseColor(value) != null;
	}

	private static boolean isValidColorCode(Object value)
	{
		// Null values are always invalid.
		if (value == null)
		{
			return false;
		}

		if (value instanceof String string)
		{
			// Empty strings aren't valid.
			if (string.isEmpty())
			{
				return false;
			}

			boolean isHex = false;
			if (string.startsWith("0x") || string.startsWith("#"))
			{
				isHex = true;
				string = string.replace("0x", "").replace("#", "");
			}

			if (isHex)
			{
				// Ensure this is a valid hex number.
				try
				{
					Long number = Long.parseUnsignedLong(string, 16);
					if (Long.compareUnsigned(number, 0) < 0 || Long.compareUnsigned(number, 0xFFFFFFFF) > 0)
					{
						return false;
					}
				}
				catch (NumberFormatException e)
				{
					return false;
				}

				// Both longform and shorthand hex notations are valid:
				// #RGB, #ARGB, #RRGGBB, #AARRGGBB
				// Make sure this string is of the valid length.
				return (string.length() == 3 || string.length() == 4 || string.length() == 6 || string.length() == 8);
			}
			else
			{
				// Ensure this is a valid decimal number.
				try
				{
					long number = Long.parseUnsignedLong(string);
					if (Long.compareUnsigned(number, 0) < 0 || Long.compareUnsigned(number, 0xFFFFFFFF) > 0)
					{
						return false;
					}
				}
				catch (NumberFormatException e)
				{
					return false;
				}

				// Now make sure the length is valid.
				return string.length() <= 10;
			}
		}
		else if (value instanceof Number number)
		{
			Long colorValue = number.longValue();
			return Long.compareUnsigned(colorValue, 0) >= 0 && Long.compareUnsigned(colorValue, 0xFFFFFFFF) <= 0;
		}
		return false;
	}

	static String formatColorName(String input)
	{
		// Remove spaces and underscores and make lowercase.
		// "Dark Blue" -> "darkblue", "DARK_BLUE" -> "darkblue"
		return input.toLowerCase().replace(" ", "").replace("_", "");
	}
}
