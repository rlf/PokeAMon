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

package com.github.rlf.pokeamon.model;

import com.github.rlf.pokeamon.model.ai.FollowPlayerAI;
import dk.lockfuglsang.minecraft.nbt.NBTUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.inventivetalent.npclib.NPCType;
import org.inventivetalent.npclib.ObjectContainer;
import org.inventivetalent.npclib.ai.AIAbstract;
import org.inventivetalent.npclib.npc.NPCAbstract;
import org.inventivetalent.npclib.npc.living.human.NPCPlayer;
import org.inventivetalent.npclib.registry.NPCRegistry;
import org.inventivetalent.npclib.skin.SkinLayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.reflection.ReflectionUtil.*;

/**
 * Responsible for creating spawning etc. of monsters
 */
public class MonsterLogic {
    private static final Logger log = Logger.getLogger(MonsterLogic.class.getName());
    private final MonsterConfig monsterConfig;
    private final NPCRegistry npcRegistry;
    private Map<UUID, Monster> activeMonsters = new HashMap<>();

    public MonsterLogic(FileConfiguration config, NPCRegistry npcRegistry) {
        monsterConfig = new MonsterConfig(config);
        this.npcRegistry = npcRegistry;
    }

    public List<String> getMonsterNames() {
        return monsterConfig.getAllTypes();
    }

    public boolean spawnMonster(Player player, String monsterType) {
        Location spawnLocation = player.getLocation().clone();
        spawnLocation = spawnLocation.add(player.getEyeLocation().getDirection().normalize());
        Vector direction = spawnLocation.clone().subtract(player.getLocation()).toVector().normalize();
        spawnLocation.setDirection(direction);
        Monster monster = createMonster(monsterType, spawnLocation);
        monster.getNpc().registerAI(new FollowPlayerAI(player, monster.getNpc()));
        UUID uuid = player.getUniqueId();
        if (monster != null) {
            if (activeMonsters.containsKey(uuid)) {
                activeMonsters.remove(uuid).despawn();
            }
            activeMonsters.put(uuid, monster);
        }
        return monster != null;
    }

    private Monster createMonster(String monsterType, Location location) {
        MonsterType type = monsterConfig.getMonsterType(monsterType);
        if (type != null && location != null) {
            NPCAbstract npc = npcRegistry.spawnNPC(location, NPCType.ZOMBIE);
            Entity entity = npc.getBukkitEntity();
            if (entity instanceof Zombie) {
                Zombie zombie = (Zombie) entity;
                zombie.setBaby(true);
                zombie.getEquipment().setHelmet(createSpawnEgg(type));
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
                zombie.setInvulnerable(true);
                zombie.setCollidable(false);
                zombie.setCanPickupItems(false);
                zombie.setCustomNameVisible(true);
                zombie.setCustomName(type.getName());
                zombie.getLocation().add(0, -0.3, 0);
                zombie.setGravity(true);
            }
            return new Monster(type, npc);
        }
        return null;
    }

    private void setHelmet(NPCPlayer npcPlayer, MonsterType type) {
        try {
            Object entity = getField(npcPlayer, "npcEntity");
            if (entity != null) {
                Object inventory = getField(entity, "inventory");
                if (inventory != null) {
                    Object size = exec(inventory, "getSize");
                    ItemStack skull = createSpawnEgg(type);
                    Object nmsCopy = execStatic(Class.forName(cb() + ".inventory.CraftItemStack"), "asNMSCopy", skull);
                    exec(inventory, "setItem",
                            new Class[]{Integer.TYPE, Class.forName(nms() + ".ItemStack")},
                            ((int) size) - 2, nmsCopy);
                }
            }
        } catch (Exception e) {
            log.info("Unable to set helmet on npc: " + e);
        }
    }

    public ItemStack getSpawnEgg(String monsterType) {
        MonsterType type = monsterConfig.getMonsterType(monsterType);
        if (type != null) {
            return createSpawnEgg(type);
        }
        return null;
    }

    private ItemStack createSpawnEgg(MonsterType type) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        return NBTUtil.addNBTTag(item, "{display:{Name:\"" + type.getName() + "\"},"
                +"SkullOwner:{Id:\"" + type.getId() + "\",Properties:{textures:[{Value:\""
                + type.getTexture()
                + "\"}]}}}");
    }

    public NPCRegistry getNPCRegistry() {
        return npcRegistry;
    }

    public MonsterType getMonsterType(String name) {
        return monsterConfig.getMonsterType(name);
    }
}
