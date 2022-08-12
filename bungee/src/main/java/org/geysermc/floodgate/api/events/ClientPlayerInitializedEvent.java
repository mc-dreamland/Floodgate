package org.geysermc.floodgate.api.events;

import java.util.UUID;
import net.md_5.bungee.api.plugin.Event;

public class ClientPlayerInitializedEvent extends Event {

	private final UUID uuid;
	private long runtimeId;

	public ClientPlayerInitializedEvent(UUID uuid, long runtimeId) {
		this.uuid = uuid;
		this.runtimeId = runtimeId;
	}

	public UUID getUuid() {
		return this.uuid;
	}
	public long getRuntimeId() {
		return this.runtimeId;
	}

}
