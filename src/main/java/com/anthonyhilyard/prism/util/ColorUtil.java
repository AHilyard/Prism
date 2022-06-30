package com.anthonyhilyard.prism.util;

import org.apache.commons.lang3.math.NumberUtils;

import net.minecraft.util.Mth;

public class ColorUtil
{
	private static int round(float value)
	{
		return (int) (value * 255.0f + 0.5f);
	}

	public static int combineARGB(int a, int r, int g, int b)
	{
		a = Mth.clamp(a, 0, 255);
		r = Mth.clamp(r, 0, 255);
		g = Mth.clamp(g, 0, 255);
		b = Mth.clamp(b, 0, 255);
		return (a << 24) | (r << 16) | (g << 8) | (b << 0);
	}

	public static int combineRGB(int r, int g, int b)
	{
		return combineARGB(255, r, g, b);
	}

	public static int HSVtoRGB(int hue, int saturation, int value)
	{
		return AHSVtoARGB(255, hue, saturation, value);
	}

	public static int HSVtoRGB(float hue, float saturation, float value)
	{
		return AHSVtoARGB(1.0f, hue, saturation, value);
	}

	public static int AHSVtoARGB(int alpha, int hue, int saturation, int value)
	{
		return AHSVtoARGB((float) alpha / 255.0f, (float) hue / 360.0f, (float) saturation / 255.0f, (float) value / 255.0f);
	}

	public static int AHSVtoARGB(float alpha, float hue, float saturation, float value)
	{
		// Ensure all arguments are in the valid range.
		alpha = Mth.clamp(alpha, 0.0f, 1.0f);
		hue = hue - (float)Mth.floor(hue);
		saturation = Mth.clamp(saturation, 0.0f, 1.0f);
		value = Mth.clamp(value, 0.0f, 1.0f);

		int r = 0, g = 0, b = 0;
		if (saturation == 0)
		{
			r = g = b = round(value);
		}
		else
		{
			int h = (int)(hue * 6.0f);
			float p = value * (1.0f - saturation);
			float q = value * (1.0f - saturation * (float) h);
			float t = value * (1.0f - (saturation * (1.0f - (float) h)));
			switch ((int) h)
			{
				case 0:
					r = round(value);
					g = round(t);
					b = round(p);
					break;
				case 1:
					r = round(q);
					g = round(value);
					b = round(p);
					break;
				case 2:
					r = round(p);
					g = round(value);
					b = round(t);
					break;
				case 3:
					r = round(p);
					g = round(q);
					b = round(value);
					break;
				case 4:
					r = round(t);
					g = round(p);
					b = round(value);
					break;
				case 5:
					r = round(value);
					g = round(p);
					b = round(q);
					break;
			}
		}
		return combineARGB(round(alpha), r, g, b);
	}

	public static float[] RGBtoHSV(float r, float g, float b)
	{
		float[] result = ARGBtoAHSV(1.0f, r, g, b);
		return new float[] { result[1], result[2], result[3] };
	}

	public static float[] RGBtoHSV(int r, int g, int b)
	{
		float[] result = ARGBtoAHSV(255, r, g, b);
		return new float[] { result[1], result[2], result[3] };
	}

	public static float[] ARGBtoAHSV(float a, float r, float g, float b)
	{
		return ARGBtoAHSV(round(a), round(r), round(g), round(b));
	}

	public static float[] ARGBtoAHSV(int a, int r, int g, int b)
	{
		a = Mth.clamp(a, 0, 255);
		r = Mth.clamp(a, 0, 255);
		g = Mth.clamp(a, 0, 255);
		b = Mth.clamp(a, 0, 255);

		float hue, saturation, value;
		int cmax = NumberUtils.max(r, g, b);
		int cmin = NumberUtils.min(r, g, b);

		value = ((float) cmax) / 255.0f;
		if (cmax != 0)
		{
			saturation = ((float) (cmax - cmin)) / ((float) cmax);
		}
		else
		{
			saturation = 0;
		}

		if (saturation == 0)
		{
			hue = 0;
		}
		else
		{
			float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
			float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
			float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
			if (r == cmax)
			{
				hue = bluec - greenc;
			}
			else if (g == cmax)
			{
				hue = 2.0f + redc - bluec;
			}
			else
			{
				hue = 4.0f + greenc - redc;
			}
			hue = hue / 6.0f;
			if (hue < 0)
			{
				hue += 1.0f;
			}
		}

		return new float[] { a, hue, saturation, value };
	}
}
