package org.geysermc.floodgate.api.events;

import java.util.HashMap;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.msgpack.type.Value;

@Getter
@ToString
@NoArgsConstructor
public class ClientPlayerInitializedEvent extends Event {
	private static final HandlerList handlerList;

	static {
		handlerList = new HandlerList();
	}

	private UUID player_uuid;
	private long runtimeId;


	public ClientPlayerInitializedEvent(UUID player_uuid, long runtimeId) {
		this.player_uuid = player_uuid;
		this.runtimeId = runtimeId;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}


	public static HandlerList getHandlerList() {
		return handlerList;
	}
}
