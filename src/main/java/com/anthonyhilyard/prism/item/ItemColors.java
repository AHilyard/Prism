package com.anthonyhilyard.prism.item;

import com.anthonyhilyard.prism.Prism;
import com.anthonyhilyard.prism.text.DynamicColor;
import com.anthonyhilyard.prism.text.TextColors;
import com.anthonyhilyard.prism.util.IColor;
import com.anthonyhilyard.prism.util.WebColors;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class ItemColors
{
	private static boolean logItemColorError = true;
	private static class ColorCollector implements FormattedCharSink
	{
		private TextColor color = null;

		@Override
		public boolean accept(int index, Style style, int codePoint)
		{
			if (style.getColor() != null)
			{
				color = style.getColor();
				return false;
			}
			return true;
		}

		public TextColor getColor() { return color; }
	}

	public static TextColor getColorForItem(ItemStack item, TextColor defaultColor)
	{
		TextColor result = null;

		// TextColor based on rarity value.
		result = item.getDisplayName().getStyle().getColor();

		// Some mods override the getName() method of the Item class, so grab that TextColor if it's there.
		if (item.getItem() != null &&
			item.getItem().getName(item) != null &&
			item.getItem().getName(item).getStyle() != null &&
			item.getItem().getName(item).getStyle().getColor() != null)
		{
			result = item.getItem().getName(item).getStyle().getColor();
		}

		// If the item has a special hover name TextColor (Stored in NBT), use that.
		if (!item.getHoverName().getStyle().isEmpty() && item.getHoverName().getStyle().getColor() != null)
		{
			result = item.getHoverName().getStyle().getColor();
		}

		// If there is a color code specified for the item name, use that.
		TextColor formattingColor = TextColors.findFirstColorCode(item.getHoverName());
		if (formattingColor != null)
		{
			result = formattingColor;
		}

		// Finally, if there is a color style stored per-character, use the first one found.
		ColorCollector colorCollector = new ColorCollector();
		item.getHoverName().getVisualOrderText().accept(colorCollector);
		if (colorCollector.getColor() != null)
		{
			result = colorCollector.getColor();
		}

		// If we haven't found a color or we're still using the rarity color, check the actual tooltip.
		// This is slow, so it better get cached externally!
		if (result == null || result.equals(item.getDisplayName().getStyle().getColor()))
		{
			Minecraft mc = Minecraft.getInstance();
			try
			{
				List<Component> lines = item.getTooltipLines(mc.player, TooltipFlag.Default.ADVANCED);
				if (!lines.isEmpty() && lines.get(0).getStyle().getColor() != null)
				{
					result = lines.get(0).getStyle().getColor();
				}
			}
			catch (Exception e)
			{
				// An exception here indicates that something went wrong when fetching the tooltip.
				// This can happen when the tooltip is grabbed too early, or sometimes from bugs in other mods.
				// In this case log the error, but only log here one time at most to prevent log spam.
				if (logItemColorError)
				{
					logItemColorError = false;
					Prism.LOGGER.error("Error getting tooltip for item: " + item.toString());
					Prism.LOGGER.error(ExceptionUtils.getStackTrace(e));
				}
			}
		}

		// Fallback to the default TextColor if we somehow haven't found a single valid TextColor.
		if (result == null)
		{
			result = defaultColor;
		}

		// If the result is still null (default color is null), just return transparent.
		if (result == null)
		{
			return (TextColor)WebColors.getColor("transparent");
		}

		return new DynamicColor((IColor)result);
	}
}
