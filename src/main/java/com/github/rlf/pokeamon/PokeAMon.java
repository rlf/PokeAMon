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

package com.github.rlf.pokeamon;

import com.github.rlf.pokeamon.command.MainCommand;
import com.github.rlf.pokeamon.model.MonsterLogic;
import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.npclib.NPCLib;
import org.inventivetalent.npclib.registry.NPCRegistry;

/**
 * The Poke-A-Monster plugin
 */
public class PokeAMon extends JavaPlugin {
    private NPCRegistry npcRegistry;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        FileUtil.setDataFolder(getDataFolder());
        I18nUtil.setDataFolder(getDataFolder());
        npcRegistry = NPCRegistry.getRegistry(this);
        if (npcRegistry == null) {
            npcRegistry = NPCLib.createRegistry(this);
        }
        MonsterLogic monsterLogic = new MonsterLogic(getConfig(), npcRegistry);
        getCommand("pokeamon").setExecutor(new MainCommand(this, monsterLogic));
    }

    @Override
    public FileConfiguration getConfig() {
        return FileUtil.getYmlConfiguration("config.yml");
    }

    @Override
    public void onDisable() {
        npcRegistry.destroy();
        npcRegistry = null;
    }
}