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
import java.lang.reflect.Type;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.SpigotPlugin;
import org.msgpack.MessagePack;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.MapValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;

@RequiredArgsConstructor
public class SpigotNeteaseCustomChannel extends NeteaseCustomChannel{
    private final JavaPlugin plugin;
    private static final Gson gson = new Gson();


    @Override
    public String getIdentifier() {
        return "floodgate:netease";
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

}
