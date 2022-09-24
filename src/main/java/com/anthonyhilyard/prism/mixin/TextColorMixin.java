package com.anthonyhilyard.prism.mixin;

import com.anthonyhilyard.prism.util.IColor;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.chat.TextColor;

@Mixin(TextColor.class)
public class TextColorMixin implements IColor
{
	@Shadow
	@Final
	private String name;

	@Shadow
	@Final
	private int value;

	@Override
	public String getName() { return name; }

	@Override
	public int getValue() { return value; }

	@Override
	public boolean isAnimated() { return false; }

	/**
	 * Fix an issue in TextColor parsing that makes it so only alpha values up to 0x7F are supported.
	 */
	@Inject(method = "parseColor", at = @At("HEAD"), cancellable = true, require = 1)
	private static void parseColor(String colorString, CallbackInfoReturnable<TextColor> info)
	{
		if (!colorString.startsWith("#"))
		{
			return;
		}

		try
		{
			int i = Integer.parseUnsignedInt(colorString.substring(1), 16);
			info.setReturnValue(TextColor.fromRgb(i));
		}
		catch (NumberFormatException numberformatexception)
		{
			info.setReturnValue(null);
		}
	}
}
