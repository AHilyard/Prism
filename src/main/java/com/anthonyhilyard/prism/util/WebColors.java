package com.anthonyhilyard.prism.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import com.anthonyhilyard.prism.text.DynamicTextColor;
import com.google.common.collect.Maps;

public class WebColors
{
	private static final Map<String, IColor> webColorMap;

	static
	{
		Map<String, IColor> loadedColors = Maps.newHashMap();

		// Load the HTML colors.
		BufferedReader reader = new BufferedReader(new InputStreamReader(WebColors.class.getClassLoader().getResourceAsStream("webcolors.json")));
		try
		{
			for (String line; (line = reader.readLine()) != null;)
			{
				String[] components = line.split(",");
				if (components.length >= 2)
				{

					loadedColors.put(ConfigHelper.formatColorName(components[0]), new IColor()
					{
						@Override
						public String getName() { return components[0]; }

						@Override
						public int getValue() { return Integer.getInteger(components[1]); }
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
			result = DynamicTextColor.fromColor(webColorMap.get(colorName));
		}
		return result;
	}
}
