package com.michaelelin.SafeHorses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_7_R3.GenericAttributes;

import org.bukkit.craftbukkit.v1_7_R3.entity.CraftHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HorseRegistry {

    private Map<Player, Horse> registry = new HashMap<Player, Horse>();
    private SafeHorsesPlugin plugin;

    public HorseRegistry(SafeHorsesPlugin plugin) {
        this.plugin = plugin;
    }

    public Horse getSafeHorse(Player player) {
        return registry.get(player);
    }

    public boolean hasSafeHorse(Player player) {
        return registry.containsKey(player);
    }

    public boolean isSafeHorse(Horse horse) {
        return registry.containsValue(horse);
    }

    public void registerSafeHorse(Player player, Horse horse) {
        registry.put(player, horse);
        horse.setOwner(player);
        horse.setAgeLock(plugin.LOCK_AGE);
        horse.setCustomNameVisible(plugin.VISIBLE_NAMES);
        horse.setBreed(false);
        horse.getUniqueId();
        // Bukkit metadata doesn't persist for some reason, so we
        // have to get inventive.
        horse.setMaxHealth(1);
        if (plugin.KEEP_STATE) {
            List<SafeHorseBean> matches = plugin.getDatabase().find(SafeHorseBean.class).where().eq("owner", player.getName()).query().findList();
            if (matches.isEmpty()) {
                horse.setCustomName(player.getName() + "'s Horse");
            }
            else {
                applyBean(horse, matches.get(0));
            }
        }
    }

    public boolean removeSafeHorse(Player player, boolean clear) {
        Horse horse = registry.remove(player);
        if (plugin.KEEP_STATE) {
            plugin.getDatabase().delete(plugin.getDatabase().find(SafeHorseBean.class).where().eq("owner", player.getName()).query().findList());
            if (!clear && horse != null) {
                plugin.getDatabase().save(toBean(horse));
            }
        }
        if (horse != null) {
            horse.remove();
        }
        return horse != null;
    }

    public void removeAllHorses() {
        for (Player p : registry.keySet()) {
            removeSafeHorse(p, false);
            registry.remove(p);
        }
    }

    public static void applyBean(Horse horse, SafeHorseBean bean) {
        horse.setCustomName(bean.getName());
        horse.setVariant(Variant.values()[bean.getVariant()]);
        horse.setColor(Color.values()[bean.getColor()]);
        horse.setStyle(Style.values()[bean.getStyle()]);
        horse.getInventory().setSaddle(new ItemStack(bean.getSaddle()));
        horse.getInventory().setArmor(new ItemStack(bean.getArmor()));
        horse.setAge(bean.getAge());
        ((CraftHorse) horse).getHandle().getAttributeInstance(GenericAttributes.d).setValue(bean.getSpeed() / 10000.0);
        horse.setJumpStrength(bean.getJump() / 10000.0);
    }

    public static SafeHorseBean toBean(Horse horse) {
        SafeHorseBean bean = new SafeHorseBean();
        bean.setOwner(horse.getOwner().getName());
        bean.setName(horse.getCustomName());
        bean.setVariant(horse.getVariant().ordinal());
        bean.setColor(horse.getColor().ordinal());
        bean.setStyle(horse.getStyle().ordinal());
        bean.setSaddle(horse.getInventory().getSaddle() == null ? 0 : horse.getInventory().getSaddle().getTypeId());
        bean.setArmor(horse.getInventory().getArmor() == null ? 0 : horse.getInventory().getArmor().getTypeId());
        bean.setAge(horse.getAge());
        bean.setSpeed((int) (((CraftHorse) horse).getHandle().getAttributeInstance(GenericAttributes.d).b() * 10000));
        bean.setJump((int) (horse.getJumpStrength() * 10000));
        return bean;
    }
}
