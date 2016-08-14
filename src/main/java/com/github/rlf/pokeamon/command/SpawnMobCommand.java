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

import com.github.rlf.pokeamon.model.MonsterLogic;
import com.github.rlf.pokeamon.model.MonsterType;
import dk.lockfuglsang.minecraft.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Test command, for trying out different mobs
 */
public class SpawnMobCommand extends AbstractCommand {
    private final MonsterLogic logic;

    public SpawnMobCommand(MonsterLogic logic) {
        super("spawnmob|mob", "pokeamon.mob", "mob-type monster-type", tr("spawn a poke-a-mon as a mob"));
        this.logic = logic;
    }

    @Override
    public boolean execute(CommandSender sender, String s, Map<String, Object> map, String... args) {
        if (args.length > 1 && sender instanceof Player) {
            String mobType = args[0];
            EntityType entityType = EntityType.valueOf(mobType.toUpperCase());
            Player player = (Player) sender;
            String name = args[1]; // TODO: 14/08/2016 - R4zorax: join?
            MonsterType type = logic.getMonsterType(name);
            if (type != null) {
                Entity entity = player.getLocation().getWorld().spawnEntity(player.getLocation(), entityType);
                if (entity instanceof LivingEntity) {
                    ItemStack skull = logic.getSpawnEgg(name);
                    LivingEntity livingEntity = (LivingEntity) entity;
                    livingEntity.getEquipment().setHelmet(skull);
                    livingEntity.getEquipment().setItemInMainHand(skull);
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000, 10, false, false));
                    if (livingEntity instanceof Zombie) {
                        ((Zombie)livingEntity).setBaby(true);
                    }
                }
                player.sendMessage(tr("spawned a {0} of type {1}", mobType, type));
                return true;
            }
        }
        return false;
    }
}
