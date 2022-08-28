package com.anthonyhilyard.prism.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class RenderTickEvent
{
	public RenderTickEvent() { }

	public static final Event<RenderTickEvent.Start> START = EventFactory.createArrayBacked(RenderTickEvent.Start.class,
		callbacks -> (timer) -> {
		for (RenderTickEvent.Start callback : callbacks)
		{
			callback.onStart(timer);
		}
	});

	@FunctionalInterface
	public interface Start
	{
		void onStart(float timer);
	}
}
