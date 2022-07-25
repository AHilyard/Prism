package com.anthonyhilyard.prism.item;

import com.anthonyhilyard.prism.text.DynamicColor;
import com.anthonyhilyard.prism.text.TextColors;
import com.anthonyhilyard.prism.util.IColor;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class ItemColors
{
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
			List<Component> lines = item.getTooltipLines(mc.player, TooltipFlag.Default.ADVANCED);
			if (!lines.isEmpty())
			{
				result = lines.get(0).getStyle().getColor();
			}
		}

		// Fallback to the default TextColor if we somehow haven't found a single valid TextColor.
		if (result == null)
		{
			result = defaultColor;
		}

		return new DynamicColor((IColor)result);
	}
}
