package com.anthonyhilyard.prism.text;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.anthonyhilyard.prism.util.ColorUtil;
import com.anthonyhilyard.prism.util.IColor;
import com.google.common.collect.Lists;

import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;

public final class DynamicTextColor extends TextColor implements IColor
{
	private final List<IColor> values = Lists.newArrayList();
	private float duration;
	private int currentIndex;
	private float timer;

	private DynamicTextColor(IColor color)
	{
		this(color, color.getName());
	}

	private DynamicTextColor(IColor color, String name)
	{
		this(new IColor[] { color }, 0.0f, name);
	}

	private DynamicTextColor(IColor[] values, float duration)
	{
		this(values, duration, null);
	}

	private DynamicTextColor(IColor[] values, float duration, String name)
	{
		super(values[0].getValue(), name);
		this.values.addAll(Arrays.asList(values));
		this.duration = values.length > 0 ? duration / values.length : duration;

		if (this.duration > 0 && this.values.size() > 1)
		{
			MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderTickEvent.class, this::onRenderTick);
		}
	}

	public static DynamicTextColor fromRGB(float red, float green, float blue)
	{
		return fromARGB(1.0f, red, green, blue);
	}

	public static DynamicTextColor fromRGB(int red, int green, int blue)
	{
		return fromARGB(255, red, green, blue);
	}

	public static DynamicTextColor fromARGB(float alpha, float red, float green, float blue)
	{
		return fromARGB((int)(alpha * 255), (int)(red * 255), (int)(green * 255), (int)(blue * 255));
	}

	public static DynamicTextColor fromARGB(int alpha, int red, int green, int blue)
	{
		return new DynamicTextColor(new IColor()
		{
			@Override
			public String getName() { return null; }

			@Override
			public int getValue() { return ColorUtil.combineARGB(alpha, red, green, blue); }
		}, null);
	}

	public static DynamicTextColor fromHSV(float hue, float saturation, float value)
	{
		return fromAHSV(1.0f, hue, saturation, value);
	}

	public static DynamicTextColor fromHSV(int hue, int saturation, int value)
	{
		return fromAHSV(255, hue, saturation, value);
	}

	public static DynamicTextColor fromAHSV(float alpha, float hue, float saturation, float value)
	{
		return fromAHSV((int)(alpha * 255), (int)(hue * 255), (int)(saturation * 255), (int)(value * 255));
	}

	public static DynamicTextColor fromAHSV(int alpha, int hue, int saturation, int value)
	{
		return new DynamicTextColor(new IColor()
		{
			@Override
			public String getName() { return null; }

			@Override
			public int getValue() { return ColorUtil.AHSVtoARGB(alpha, hue, saturation, value); }
		}, null);
	}

	public static DynamicTextColor fromColor(IColor color)
	{
		return new DynamicTextColor(color);
	}

	public void addColor(IColor color)
	{
		values.add(color);
	}

	public void clearColors()
	{
		values.clear();
		currentIndex = 0;
	}

	public void setDuration(float duration)
	{
		this.duration = Math.max(duration, 0.0f);

		if (this.duration > 0 && this.values.size() > 1)
		{
			MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderTickEvent.class, this::onRenderTick);
		}
		else
		{
			MinecraftForge.EVENT_BUS.unregister((Consumer<RenderTickEvent>)this::onRenderTick);
		}
	}

	@Override
	public int getValue()
	{
		if (values.isEmpty())
		{
			return 0;
		}
		else if (values.size() > 1)
		{
			int nextIndex = (currentIndex + 1) % values.size();
			int currentValue = values.get(currentIndex).getValue();
			int nextValue = values.get(nextIndex).getValue();

			int alpha =	(int) Mth.lerp((currentValue >> 24) & 0xFF, (nextValue >> 24) & 0xFF, timer / duration);
			int red =	(int) Mth.lerp((currentValue >> 16) & 0xFF, (nextValue >> 16) & 0xFF, timer / duration);
			int green =	(int) Mth.lerp((currentValue >> 8) & 0xFF,  (nextValue >> 8) & 0xFF, timer / duration);
			int blue =	(int) Mth.lerp((currentValue >> 0) & 0xFF,  (nextValue >> 0) & 0xFF, timer / duration);

			return ColorUtil.combineARGB(alpha, red, green, blue);
		}
		else
		{
			return values.get(0).getValue();
		}
	}

	@Override
	public String toString()
	{
		if (values.size() == 1)
		{
			return name != null ? name : String.format("#%08X", this.getValue());
		}
		else
		{
			StringBuilder result = new StringBuilder();
			result.append("([");
			for (IColor value : values)
			{
				result.append(value.getName() != null ? name : String.format("#%08X", this.getValue()));
				result.append(", ");
			}
			result.delete(result.length() - 2, result.length() - 1);
			result.append("], ");
			result.append(duration);
			result.append("s)");
			return result.toString();
		}
	}

	public void onRenderTick(RenderTickEvent event)
	{
		timer += 1.0f / 20.0f;
		if (timer >= duration)
		{
			currentIndex = (currentIndex + 1) % values.size();
			timer -= duration;
		}
	}

	@Override
	public String getName()
	{
		return name;
	}
}
