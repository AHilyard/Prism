package com.anthonyhilyard.prism.text;

import java.util.List;

import com.anthonyhilyard.prism.events.RenderTickEvent;
import com.anthonyhilyard.prism.util.ColorUtil;
import com.anthonyhilyard.prism.util.IColor;
import com.google.common.collect.Lists;

import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;

public final class DynamicColor extends TextColor implements IColor
{
	private final List<IColor> values = Lists.newArrayList();
	private float duration;
	private int currentIndex;
	private float timer;

	public DynamicColor(IColor color)
	{
		this(color, color.getName());
	}

	public DynamicColor(IColor color, String name)
	{
		this(List.of(color), 0.0f, name);
	}

	public DynamicColor(List<IColor> values, float duration)
	{
		this(values, duration, null);
	}

	public DynamicColor(List<IColor> values, float duration, String name)
	{
		super(values.get(0).getValue(), name);

		this.values.addAll(values);
		this.duration = values.size() > 0 ? duration / values.size() : duration;

		if (isAnimated())
		{
			RenderTickEvent.START.register(this::onRenderTick);
		}
	}

	public static DynamicColor fromRgb(int value)
	{
		if (Integer.compareUnsigned(value, 0xFFFFFF) >= 0)
		{
			return fromARGB((value >> 24) & 0xFF, (value >> 16) & 0xFF, (value >> 8) & 0xFF, (value >> 0) & 0xFF);
		}
		else
		{
			return fromRGB((value >> 16) & 0xFF, (value >> 8) & 0xFF, (value >> 0) & 0xFF);
		}
	}

	public static DynamicColor fromRGB(float red, float green, float blue)
	{
		return fromARGB(1.0f, red, green, blue);
	}

	public static DynamicColor fromRGB(int red, int green, int blue)
	{
		return fromARGB(255, red, green, blue);
	}

	public static DynamicColor fromARGB(float alpha, float red, float green, float blue)
	{
		return fromARGB((int)(alpha * 255), (int)(red * 255), (int)(green * 255), (int)(blue * 255));
	}

	public static DynamicColor fromARGB(int alpha, int red, int green, int blue)
	{
		return new DynamicColor(new IColor()
		{
			@Override
			public String getName() { return null; }

			@Override
			public int getValue() { return ColorUtil.combineARGB(alpha, red, green, blue); }

			@Override
			public boolean isAnimated() { return false; }
		}, null);
	}

	public static DynamicColor fromHSV(float hue, float saturation, float value)
	{
		return fromAHSV(1.0f, hue, saturation, value);
	}

	public static DynamicColor fromHSV(int hue, int saturation, int value)
	{
		return fromAHSV(255, hue, saturation, value);
	}

	public static DynamicColor fromAHSV(float alpha, float hue, float saturation, float value)
	{
		return fromAHSV((int)(alpha * 255 + 0.5f), (int)(hue * 255 + 0.5f), (int)(saturation * 255 + 0.5f), (int)(value * 255 + 0.5f));
	}

	public static DynamicColor fromAHSV(int alpha, int hue, int saturation, int value)
	{
		return new DynamicColor(new IColor()
		{
			@Override
			public String getName() { return null; }

			@Override
			public int getValue() { return ColorUtil.AHSVtoARGB(alpha, hue, saturation, value); }

			@Override
			public boolean isAnimated() { return false; }
		}, null);
	}

	public static DynamicColor fromColor(IColor color)
	{
		return new DynamicColor(color);
	}

	public int alpha()		{ return (getValue() >> 24) & 0xFF; }
	public int red()		{ return (getValue() >> 16) & 0xFF; }
	public int green()		{ return (getValue() >> 8)  & 0xFF; }
	public int blue()		{ return (getValue() >> 0)  & 0xFF; }

	public int hue()		{ return (int)(ColorUtil.RGBtoHSV(red(), green(), blue())[0] * 360.0f); }
	public int saturation()	{ return (int)(ColorUtil.RGBtoHSV(red(), green(), blue())[1] * 255.0f); }
	public int value()		{ return (int)(ColorUtil.RGBtoHSV(red(), green(), blue())[2] * 255.0f); }

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

		if (isAnimated())
		{
			RenderTickEvent.START.register(this::onRenderTick);
		}
	}

	@Override
	public boolean isAnimated()
	{
		return values.size() > 1 && duration > 0;
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

			int alpha =	(int) Mth.lerp(timer / duration, (currentValue >> 24) & 0xFF, (nextValue >> 24) & 0xFF);
			int red =	(int) Mth.lerp(timer / duration, (currentValue >> 16) & 0xFF, (nextValue >> 16) & 0xFF);
			int green =	(int) Mth.lerp(timer / duration, (currentValue >> 8) & 0xFF,  (nextValue >> 8) & 0xFF);
			int blue =	(int) Mth.lerp(timer / duration, (currentValue >> 0) & 0xFF,  (nextValue >> 0) & 0xFF);

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

	public void onRenderTick(float partialTick)
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
