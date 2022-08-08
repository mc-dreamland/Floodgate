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

package org.geysermc.floodgate.addon.data;

import io.netty.channel.Channel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.geysermc.floodgate.BungeePlugin;
import org.geysermc.floodgate.api.handshake.HandshakeData;
import org.geysermc.floodgate.config.FloodgateConfig;
import org.geysermc.floodgate.util.BedrockData;
import org.geysermc.floodgate.util.LinkedPlayer;

@Getter
public class HandshakeDataImpl implements HandshakeData {
    private final Channel channel;
    private final boolean floodgatePlayer;
    private final BedrockData bedrockData;
    private final String javaUsername;
    private final UUID javaUniqueId;

    @Setter private LinkedPlayer linkedPlayer;
    @Setter private String hostname;
    @Setter private String ip;
    @Setter private String disconnectReason;

    public HandshakeDataImpl(
            Channel channel,
            boolean floodgatePlayer,
            BedrockData bedrockData,
            FloodgateConfig config,
            LinkedPlayer linkedPlayer,
            String hostname) {

        this.channel = channel;
        this.floodgatePlayer = floodgatePlayer;
        this.bedrockData = bedrockData;
        this.linkedPlayer = linkedPlayer;
        this.hostname = hostname;

        String javaUsername = null;
        UUID javaUniqueId = null;

        if (bedrockData != null) {
            String prefix = config.getUsernamePrefix();
            javaUniqueId = UUID.fromString("00000000-0000-4000-8000-0000" + bedrockData.getXuid());
            int usernameLength = Math.min(bedrockData.getUsername().length(), 16 - prefix.length());
            String relname = bedrockData.getUsername().substring(0, usernameLength);
            String rename = this.lookupName(javaUniqueId,relname,"PE");
            javaUsername = prefix + rename;
//            javaUsername = prefix + relname;
            if (config.isReplaceSpaces()) {
                javaUsername = javaUsername.replace(" ", "_");
            }

//            javaUniqueId = Utils.getJavaUuid(bedrockData.getXuid());
            this.ip = bedrockData.getIp();
        }

        this.javaUsername = javaUsername;
        this.javaUniqueId = javaUniqueId;
    }

    @Override
    public String getCorrectUsername() {
        return linkedPlayer != null ? linkedPlayer.getJavaUsername() : javaUsername;
    }

    @Override
    public UUID getCorrectUniqueId() {
        return linkedPlayer != null ? linkedPlayer.getJavaUniqueId() : javaUniqueId;
    }
    public String lookupName(final UUID id, final String name, final String type) {
        try {
            final Connection conn =  BungeePlugin.getDataSource().getConnection();
            try {
                final PreparedStatement sql = conn.prepareStatement("select name from localprofile where id = ?");
                try {
                    sql.setString(1, id.toString());
                    final ResultSet set = sql.executeQuery();
                    try {
                        if (set.next()) {
                            return set.getString("name");
                        }
                        return this.modify(id, name, type, conn);
                    }
                    finally {
                        if (Collections.singletonList(set).get(0) != null) {
                            set.close();
                        }
                    }
                }
                finally {
                    if (Collections.singletonList(sql).get(0) != null) {
                        sql.close();
                    }
                }
            }
            finally {
                if (Collections.singletonList(conn).get(0) != null) {
                    conn.close();
                }
            }
        }
        catch (Throwable $ex) {
            return name;
        }
    }
    private String modify(final UUID id, final String name, final String  type, final Connection conn) throws SQLException {
        String mod = name;
        int count = 0;
        while (true) {
            final PreparedStatement sql = conn.prepareStatement("insert into localprofile(id, name, name_origin, pc_pe) value(? ,?, ?, ?)");
            try {
                sql.setString(1, id.toString());
                sql.setString(2, mod);
                sql.setString(3, name);
                sql.setString(4, type.toLowerCase());
                try {
                    sql.executeUpdate();
                    return mod;
                }
                catch (SQLException e) {
                    ++count;
                    if (mod.length() >= 15) {
                        mod = mod.substring(0, 14);
                    }
                    mod += count;
                }
            }
            finally {
                if (Collections.singletonList(sql).get(0) != null) {
                    sql.close();
                }
            }
        }
    }
}
