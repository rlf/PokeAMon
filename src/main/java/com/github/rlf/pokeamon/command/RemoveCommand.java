/*
 * Copyright (c) 2016. R4zorax. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 *
 */

package com.github.rlf.pokeamon.command;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.eventcallbacks.EventCallbacks;
import org.inventivetalent.eventcallbacks.PlayerEventCallback;
import org.inventivetalent.npclib.NPCLib;
import org.inventivetalent.npclib.npc.NPCAbstract;
import org.inventivetalent.npclib.registry.NPCRegistry;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Greatly inspired by the remove command in CompactNPCLib - full credit goes to inventivetalent.
 * See <a href="https://github.com/InventivetalentDev/CompactNPCLib/blob/master/Plugin/src/main/java/org/inventivetalent/npclib/command/SpawnCommands.java">https://github.com/InventivetalentDev/CompactNPCLib/blob/master/Plugin/src/main/java/org/inventivetalent/npclib/command/SpawnCommands.java</a>
 */
public class RemoveCommand extends AbstractCommand {
    private final JavaPlugin plugin;
    private final EventCallbacks eventCallbacks;
    private final NPCRegistry npcRegistry;

    public RemoveCommand(JavaPlugin plugin, EventCallbacks callbacks, NPCRegistry npcRegistry) {
        super("remove|rm", "pokeamon.remove", tr("removes a previously spawned pokeamon"));
        this.plugin = plugin;
        this.eventCallbacks = callbacks;
        this.npcRegistry = npcRegistry;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, Map<String, Object> map, String... args) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        Player player = (Player) commandSender;
        int nearbyCount = 0;
        for (Entity entity : player.getNearbyEntities(8, 4, 8)) {
            if (NPCLib.isNPC(entity)) {
                nearbyCount++;
            }
        }
        if (nearbyCount == 0) {
            player.sendMessage("§cNo NPCs found nearby. Please stand closer and try again.");
            return true;
        }
        eventCallbacks.listenFor(PlayerInteractEntityEvent.class, new PlayerEventCallback<PlayerInteractEntityEvent>(player) {
            @Override
            public void callPlayer(PlayerInteractEntityEvent event) {
                if (event.isCancelled()) {
                    return;
                }
                if (event.getPlayer().isSneaking()) {// Cancelled
                    player.sendMessage(tr("§aCancelled"));
                    return;
                }
                Entity entity = event.getRightClicked();
                NPCAbstract npc = NPCLib.getNPC(entity);
                if (npc == null) {
                    player.sendMessage(tr("§cThat's not a pokeamonster"));
                    return;
                }
                NPCAbstract removed = npcRegistry.removeNpc(entity.getUniqueId());
                if (removed != null) {
                    player.sendMessage(tr("§aNPC removed"));
                } else {
                    player.sendMessage(tr("§cCould not remove pokeamon, it belongs to {0}", npc.getPlugin()));
                    player.sendMessage(tr("§7right-click §athe pokeamonster to remove it, regardless"));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> eventCallbacks.listenFor(PlayerInteractEntityEvent.class, new PlayerEventCallback<PlayerInteractEntityEvent>(player) {
                        @Override
                        public void callPlayer(PlayerInteractEntityEvent playerInteractEntityEvent) {
                            if (event.isCancelled()) {
                                return;
                            }
                            if (event.getPlayer().isSneaking()) {// Cancelled
                                player.sendMessage(tr("§aCancelled"));
                                return;
                            }
                            Entity entity1 = event.getRightClicked();
                            NPCAbstract npc1 = NPCLib.getNPC(entity1);
                            if (npc1 == null) {
                                player.sendMessage(tr("§cThat's not a pokeamonster"));
                                return;
                            }
                            NPCRegistry registry = NPCRegistry.getRegistry(npc1.getPlugin());
                            if (registry != null && registry.removeNpc(npc1) != null) {
                                player.sendMessage(tr("§aNPC removed"));
                            } else {
                                player.sendMessage(tr("§cCould not remove pokeamon, it belongs to {0}", npc1.getPlugin()));
                            }
                        }
                    }), 10);
                }
            }
        });
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.sendMessage(tr("§ePlease §7right-click §athe pokeamonster to remove it, or §7hold shift & right-click §eto cancel.")), 0);
        return true;
    }
}
