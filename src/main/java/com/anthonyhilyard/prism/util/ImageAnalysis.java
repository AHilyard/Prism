package com.anthonyhilyard.prism.util;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;

import javax.imageio.ImageIO;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class ImageAnalysis
{
	/**
	 * Returns the dominant color from the given image.
	 * @param imageLocation The resource location of the image.
	 * @param region The region of the image to use, or null for the whole image.
	 * @return The TextColor representation of the most dominant color in the image, or null if an error occurred.
	 */
	public static TextColor getDominantColor(ResourceLocation imageLocation, Rect2i region)
	{
		// First, load the resource and make sure it's a valid image.
		Minecraft minecraft = Minecraft.getInstance();
		ResourceManager resourceManager = minecraft.getResourceManager();
		try (InputStream imageStream = resourceManager.getResource(imageLocation).getInputStream())
		{
			BufferedImage image = ImageIO.read(imageStream);
			
			if (region != null)
			{
				image = image.getSubimage(region.getX(), region.getY(), region.getWidth(), region.getHeight());
			}
			return getDominantColor(image);
		}
		catch (Exception e) {}
		return null;
	}

	/**
	 * Returns the dominant color from the given image.
	 * @param image The image as a BufferedImage instance.
	 * @return The TextColor representation of the most dominant color in the image, or null if an error occurred.
	 */
	public static TextColor getDominantColor(BufferedImage image)
	{
		record ImageSampleData(float weight, int count) {};
		final Map<Integer, ImageSampleData> samples = Maps.newHashMap();
		Function<Integer, Float> getWeight = color ->
		{
			float a = ((color >> 24) & 0xFF) / 255.0f;
			float r = ((color >> 16) & 0xFF) / 255.0f;
			float g = ((color >>  8) & 0xFF) / 255.0f;
			float b = ((color >>  0) & 0xFF) / 255.0f;

			// Ignore black and low-alpha colors.
			if ((r <= 0.06f && g <= 0.06f && b <= 0.06f) || a < 0.3f)
			{
				return 0.0f;
			}

			// Prioritize bright and opaque colors.
			return (1.0f - ((1.0f - a) * (1.0f - a)) +
					1.0f - ((1.0f - r) * (1.0f - r)) +
					1.0f - ((1.0f - g) * (1.0f - g)) +
					1.0f - ((1.0f - r) * (1.0f - r)) +
					1.0f - ((1.0f - g) * (1.0f - g)) +
					1.0f - ((1.0f - b) * (1.0f - b)) +
					1.0f - ((1.0f - b) * (1.0f - b))) / 7.0f;
		};

		// Loop over every pixel, calculating the "weight" of that color, and adding
		// it to our sample map.
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				// Also degrade the pixel color to group similar colors.
				for (int degrade = 0; degrade < 8; degrade += 2)
				{
					int color = image.getRGB(x, y);
					int degradedColor = ColorUtil.combineARGB(((color >> 24) & 0xFF) >> degrade,
															  ((color >> 16) & 0xFF) >> degrade,
															  ((color >>  8) & 0xFF) >> degrade,
															  ((color >>  0) & 0xFF) >> degrade);

					if (!samples.containsKey(degradedColor))
					{
						float weight = getWeight.apply(degradedColor);
						if (weight == 0)
						{
							continue;
						}
						ImageSampleData data = new ImageSampleData(weight, 1);

						samples.put(degradedColor, data);
					}
					else
					{
						samples.replace(degradedColor, new ImageSampleData(samples.get(degradedColor).weight(), samples.get(degradedColor).count() + 1));
					}
				}
			}
		}

		final Map<Integer, Float> groups = Maps.newHashMap();
		// Now iterate over the sample map and determine the most dominant color.
		for (Integer color : samples.keySet())
		{
			groups.put(color, samples.get(color).count() * samples.get(color).weight());
		}

		if (groups.isEmpty())
		{
			return null;
		}

		return TextColor.fromRgb(groups.entrySet().stream().max((a, b) -> Float.compare(a.getValue(), b.getValue())).get().getKey());
	}
}
