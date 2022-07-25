package com.anthonyhilyard.prism.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import com.anthonyhilyard.prism.text.DynamicColor;
import com.google.common.collect.Maps;

public class WebColors
{
	private static final Map<String, IColor> webColorMap;

	static
	{
		Map<String, IColor> loadedColors = Maps.newHashMap();

		// Load the HTML colors.
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(WebColors.class.getClassLoader().getResourceAsStream("webcolors.csv")));)
		{
			for (String line; (line = reader.readLine()) != null;)
			{
				String[] components = line.split(",");
				if (components.length >= 2)
				{
					final String name = components[0];
					final int value = Integer.parseUnsignedInt(components[1], 16);
					loadedColors.put(ConfigHelper.formatColorName(name), new IColor()
					{
						@Override
						public String getName() { return name; }

						@Override
						public int getValue() { return value; }

						@Override
						public boolean isAnimated() { return false; }
					});
				}
			}
		}
		catch (IOException e) {}

		webColorMap = Map.copyOf(loadedColors);
	}

	public static IColor getColor(String colorName)
	{
		colorName = ConfigHelper.formatColorName(colorName);
		IColor result = null;
		if (webColorMap.containsKey(colorName))
		{
			result = DynamicColor.fromColor(webColorMap.get(colorName));
		}
		return result;
	}
}
