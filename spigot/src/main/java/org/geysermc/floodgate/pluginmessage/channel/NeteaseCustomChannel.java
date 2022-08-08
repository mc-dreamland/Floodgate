/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Floodgate
 */

package org.geysermc.floodgate.pluginmessage.channel;

import com.ayou.protocolsupportcustompacket.event.NeteaseReciveCustomPacketEvent;
import com.google.gson.Gson;
import com.google.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.geysermc.floodgate.api.UnsafeFloodgateApi;
import org.geysermc.floodgate.platform.pluginmessage.PluginMessageUtils;
import org.geysermc.floodgate.pluginmessage.PluginMessageChannel;
import org.msgpack.MessagePack;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.MapValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;

public class NeteaseCustomChannel implements PluginMessageChannel {
    @Inject private PluginMessageUtils pluginMessageUtils;
    private static final Gson gson = new Gson();

    @Override
    public String getIdentifier() {
        return "floodgate:netease";
    }

    @Override
    public Result handleProxyCall(
            byte[] data,
            UUID targetUuid,
            String targetUsername,
            Identity targetIdentity,
            UUID sourceUuid,
            String sourceUsername,
            Identity sourceIdentity) {
        if (sourceIdentity == Identity.SERVER) {
            // send it to the client
            return Result.forward();
        }
        if (sourceIdentity == Identity.PLAYER) {
            return Result.forward();
        }

        return Result.handled();
    }

    @Override
    public Result handleServerCall(byte[] data, UUID player_uuid, String player_name) {
        MessagePack messagePack = new MessagePack();
        try {
            Value originJson = messagePack.read(data);
            Value unConvert = messagePack.unconvert("value");
            if (!originJson.getType().equals(ValueType.MAP)) return Result.handled();
            MapValue mapValue = originJson.asMapValue();
            ArrayValue values = mapValue.get(unConvert).asArrayValue();
            if (!values.get(0).toString().equals("\"ModEventC2S\"") && !values.get(0).toString().equals("\"ModEventS2C\"")) return Result.handled();
            ArrayValue packData = values.get(1).asMapValue().get(unConvert).asArrayValue();

            String modName = packData.get(0).toString().replace("\"", "");
            String systemName = packData.get(1).toString().replace("\"", "");
            String eventName = packData.get(2).toString().replace("\"", "");
            HashMap<String, Object> packDataMap = new HashMap<>();
            if (packData.get(3).isMapValue()) {
                packDataMap = gson.fromJson(packData.get(3).toString(), (Type) HashMap.class);
            }
            NeteaseReciveCustomPacketEvent neteaseReciveCustomPacketEvent = new NeteaseReciveCustomPacketEvent(
                    false,
                    player_uuid,
                    player_name,
                    modName,
                    systemName,
                    eventName,
                    packDataMap,
                    originJson
            );
            Bukkit.getServer().getPluginManager().callEvent(neteaseReciveCustomPacketEvent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.handled();
    }

    public boolean sendPacket(UUID player, byte[] packet, UnsafeFloodgateApi api) {
        if (api == null) {
            throw new IllegalArgumentException("Can only send a packet using the unsafe api");
        }
        return pluginMessageUtils.sendMessage(player, getIdentifier(), packet);
    }

    public boolean sendPacket(UUID player, byte[] packet) {
        return pluginMessageUtils.sendMessage(player, getIdentifier(), packet);
    }
}
