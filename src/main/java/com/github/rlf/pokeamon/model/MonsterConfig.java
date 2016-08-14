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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Monster Configuration
 */
public class MonsterConfig {
    private final FileConfiguration config;
    private final Map<String, MonsterType> monsterTypes;
    private final Map<String, Category> categories;

    public MonsterConfig(FileConfiguration config) {
        this.config = config;
        monsterTypes = new HashMap<>();
        categories = new HashMap<>();
        ConfigurationSection section;

        // TODO: 13/08/2016 - R4zorax: Add validation of config
        // Categories
        section = config.getConfigurationSection("categories");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                Map<String, Double> weights = new HashMap<>();
                ConfigurationSection multiplier = section.getConfigurationSection("multiplier");
                if (multiplier != null) {
                    for (String cid : multiplier.getKeys(false)) {
                        weights.put(cid.toLowerCase(), multiplier.getDouble(cid, 1));
                    }
                }
                categories.put(id.toLowerCase(), new Category(id.toLowerCase(), weights));
            }
        }
        // Monsters
        section = config.getConfigurationSection("monsters");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                monsterTypes.put(id.toLowerCase(),
                        new MonsterType(section.getString(id + ".id", UUID.randomUUID().toString()),
                        section.getString(id + ".name", id),
                        section.getString(id + ".texture", null),
                        section.getString(id + ".signature", null),
                        categories.get(section.getString(id + ".category"))));
            }
        }
    }

    public List<String> getAllTypes() {
        return new ArrayList<>(monsterTypes.keySet());
    }

    public MonsterType getMonsterType(String id) {
        if (monsterTypes.containsKey(id.toLowerCase())) {
            return monsterTypes.get(id.toLowerCase());
        }
        return null;
    }
}
