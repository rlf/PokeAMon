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

package com.github.rlf.pokeamon.model.ai;

import dk.lockfuglsang.minecraft.reflection.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.inventivetalent.npclib.ai.AIAbstract;
import org.inventivetalent.npclib.npc.NPCAbstract;
import org.inventivetalent.npclib.npc.living.NPCLivingAbstract;
import org.inventivetalent.vectors.d3.Vector3DDouble;

import static dk.lockfuglsang.minecraft.reflection.ReflectionUtil.exec;

/**
 * Simply follows the player
 */
public class FollowPlayerAI extends AIAbstract {
    private static final double speed = 0.1;
    private static final double distance = 2d;
    private static final int jumpEvery = 40;
    private static final double burrowDepth = 0.2d;
    private long ticks = 0;
    private final Player player;
    public FollowPlayerAI(Player player, NPCAbstract npc) {
        this.player = player;
        setNpc(npc);
    }

    @Override
    public void tick() {
        if (player.isOnline() && player.getLocation() != null) {
            Location target = player.getLocation().clone();
            target = target.add(player.getEyeLocation().getDirection().clone().normalize().multiply(distance));
            while (!(target.getBlock().getRelative(BlockFace.UP).isEmpty())) {
                target.add(0, 1, 0);
            }
            while (!(target.getBlock().getType().isSolid() || target.getBlock().isLiquid())) {
                target.subtract(0, 1, 0);
            }
            //target.subtract(0, burrowDepth, 0);
            NPCAbstract npc = getNpc();
            if (npc instanceof NPCLivingAbstract) {
                NPCLivingAbstract living = (NPCLivingAbstract) npc;
                Location currentLocation = living.getBukkitEntity().getLocation();
                Vector movement = target.clone().subtract(currentLocation).toVector();
                double deltaY = movement.getY() > 0.7 ? 1.5 : movement.getY();
                movement.setY(deltaY);
                if (movement.length() > 10) {
                    setPosition(living, target);
                } else if (movement.length() > 0.5) {
                    movement = movement.normalize().multiply(speed);
                    if ((target.getY() - currentLocation.getY()) >= 0.9) {
                        movement.setY(1.5); // Jump
                    }
                    living.getBukkitEntity().setVelocity(movement);
/*                } else if (living.getBukkitEntity().isOnGround()) {
                    Location clone = currentLocation.clone();
                    clone.setY(clone.getBlockY() - burrowDepth);
                    setPosition(living, clone);*/
                }
                /*
                if ((ticks % jumpEvery) == 0) {
                    movement.setY(Math.max(1, movement.getY()));
                }
                */
                //living.setMotion(movement.getX(), movement.getY(), movement.getZ());
                living.lookAt(new Vector3DDouble(player.getLocation()));
            }
            ticks++;
        }
    }

    private void setPosition(NPCAbstract living, Location target) {
        Object handle = exec(living.getBukkitEntity(), "getHandle");
        exec(handle, "setPosition",
                new Class[]{Double.TYPE, Double.TYPE, Double.TYPE},
                target.getX(), target.getY(), target.getZ());
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
