// QuickBelt - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.sargant.bukkit.quickbelt;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class QuickBeltPlayerListener implements Listener {
	private QuickBelt parent;

	public QuickBeltPlayerListener(QuickBelt instance) {
		parent = instance;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) { cleanup(event); }
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerKick(PlayerKickEvent event) { cleanup(event); }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent event) { invCheck(event); }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerAnimation(PlayerAnimationEvent event) { invCheck(event); }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) { parent.log.info("stuff"); invCheck(event); }
	
	private void invCheck(PlayerEvent pev) {
		
		// Check the player wants to use quickbelt
		Player player = pev.getPlayer();
		if(!parent.isPlayerEnabled(player)) return;
		
		// We use Lists for hash-checking
		List<ItemStack> current_inv = Arrays.asList(player.getInventory().getContents());
		List<ItemStack> previous_inv = parent.inventories.get(player.getName());
		
		// If user doesn't exist, insert them to the inventories
		// We may still want to update, so don't cancel
		if(previous_inv == null) {
			parent.inventories.put(player.getName(), current_inv);
			return;
			
		// If the user existed and hasn't changed, give up
		} else if(previous_inv.equals(current_inv)) {
			return;
		
		// Otherwise the user has changed, and we want to process
		} else {
			parent.inventories.put(player.getName(), current_inv);
		}
		
		if(current_inv.size() != 36) {
			parent.log.warning("Inventory is not 36 in size. I am broken. Sad face.");
			return;
		}
		
		// Given the split in the numbers, there are two easy ways of doing this, 
		// either slots first, columns second, or columns first, slots second
		// Doing it the second way to guarantee a vacant slot gets filled form whatever height
		algorithmDrop(player, previous_inv, current_inv);

		parent.inventories.put(player.getName(), current_inv);
		player.getInventory().setContents((ItemStack[]) current_inv.toArray());
	}
	
	private void cleanup(PlayerEvent event) {
		// No need to clean up just a few bytes...
	}
	
	private void algorithmDrop(Player player, List<ItemStack> previous_inv,List<ItemStack> current_inv) {
		
		for(Integer i=0; i<= 8; i++) {
			
			if(isAir(current_inv.get(i))) {
				Integer j;
				for (j=9; j<=35; j++) {
					if (!isAir(current_inv.get(j)) && !isAir(previous_inv.get(i))) {
						if (current_inv.get(j).getType() == previous_inv.get(i).getType()) {
						
			
							ItemStack swap = current_inv.get(j);
							current_inv.set(j, current_inv.get(i));
							current_inv.set(i, swap);
						
							if(!parent.silent) {
								player.sendMessage(ChatColor.AQUA.toString() + "Replenished slot " + (i+1) + ChatColor.WHITE.toString());
							}
							break;
						}
					}
				}
				//else identical item found
				if (j == 36 && !isAir(previous_inv.get(i))) {
					if(!parent.silent) {
						player.sendMessage(ChatColor.AQUA.toString() + "Depleted slot " + (i+1) + ChatColor.WHITE.toString());
					}
				}
			}
			
		}
		
		
	}
	
	
	private boolean isAir(ItemStack m) {
	    if(m == null) return true;
	    if(m.getType() == Material.AIR) return true;
	    return false;
	}
}
