package com.anthonyhilyard.prism.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public class TextColors
{
	public static TextColor findFirstColorCode(Component textComponent)
	{
		// This function finds the first specified color code in the given text component.
		// It is intended to skip non-color formatting codes.
		String rawTitle = textComponent.getString();
		for (int i = 0; i < rawTitle.length(); i += 2)
		{
			// If we encounter a formatting code, check to see if it's a color.  If so, return it.
			if (rawTitle.charAt(i) == '\u00a7')
			{
				try
				{
					ChatFormatting format = ChatFormatting.getByCode(rawTitle.charAt(i + 1));
					if (format != null && format.isColor())
					{
						return TextColor.fromLegacyFormat(format);
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
					return null;
				}
			}
			// Otherwise, if we encounter a non-formatting character, bail.
			else
			{
				return null;
			}
		}

		return null;
	}
}
