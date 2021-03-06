package com.bafomdad.realfilingcabinet.blocks;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.bafomdad.realfilingcabinet.RealFilingCabinet;
import com.bafomdad.realfilingcabinet.helpers.TextHelper;
import com.bafomdad.realfilingcabinet.init.RFCItems;
import com.bafomdad.realfilingcabinet.integration.BotaniaRFC;
import com.bafomdad.realfilingcabinet.items.ItemManaFolder;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ItemBlockMana extends ItemBlock {

	public ItemBlockMana(Block block) {
		
		super(block);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean whatisthis) {
		
		if (stack.hasTagCompound()) {
			list.add(TextHelper.localize("tooltip." + RealFilingCabinet.MOD_ID + ".itemblockrfc"));
			if ((Keyboard.isKeyDown(42)) || (Keyboard.isKeyDown(54)))
				listItems(stack, list);
			else if (list.size() > 2)
				list.remove(2);
		}
	}
	
	private void listItems(ItemStack stack, List list) {
		
		NBTTagList tagList = stack.getTagCompound().getTagList("inventory", 10);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound itemTag = tagList.getCompoundTagAt(i);
			ItemStack folder = ItemStack.loadItemStackFromNBT(itemTag);
			if (folder != null && folder.getItem() == RFCItems.manaFolder) {
				int count = ItemManaFolder.getManaSize(folder);
				list.add(BotaniaRFC.formatMana(count));
			}
		}
	}
}
