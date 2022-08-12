package org.geysermc.floodgate.api.events;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClientPlayerInitializedEvent extends Event {
	@Getter
	private static final HandlerList handlerList = new HandlerList();

	private UUID uuid;
	private long runtimeId;

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
}
