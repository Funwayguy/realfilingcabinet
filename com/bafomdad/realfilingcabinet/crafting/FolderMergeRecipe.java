package com.bafomdad.realfilingcabinet.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bafomdad.realfilingcabinet.init.RFCItems;
import com.bafomdad.realfilingcabinet.items.ItemFolder;
import com.bafomdad.realfilingcabinet.utils.StorageUtils;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

public class FolderMergeRecipe extends ShapelessRecipes implements IRecipe {
	
	public static List inputs = new ArrayList();
	
	static
	{
		inputs.add(new ItemStack(RFCItems.folder, 1, 0));
		inputs.add(new ItemStack(RFCItems.folder, 1, 0));
	}

	public FolderMergeRecipe() {
		
		super(new ItemStack(RFCItems.folder, 1, 0), inputs);
	}
	
	@Override
	public boolean matches(InventoryCrafting ic, World world) {
		
		ArrayList list = new ArrayList(this.inputs);
		
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				
				ItemStack stack = ic.getStackInRowAndColumn(j, i);
				if (stack != null)
				{
					boolean flag = false;
					Iterator iter = list.iterator();
					
					while (iter.hasNext())
					{
						ItemStack stack1 = (ItemStack)iter.next();
						
						if (stack.getItem() == stack1.getItem() && (stack1.getItemDamage() == 32767 || stack.getItemDamage() == stack1.getItemDamage()))
						{
							flag = true;
							list.remove(stack1);
							break;
						}
					}
					if (!flag)
						return false;
				}
			}
		}
		return list.isEmpty();
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting ic) {
		
		int hostFolder = -1;
		int mergeFolder = -1;
		
		for (int i = 0; i < ic.getSizeInventory(); i++) {
			
			ItemStack stack = ic.getStackInSlot(i);
			if (stack != null)
			{
				if (stack.getItem() == RFCItems.folder && stack.getItemDamage() == 0) {
					if (hostFolder == -1)
						hostFolder = i;
					if (hostFolder >= 0 && hostFolder != i)
						mergeFolder = i;
				}
			}
		}
		if (hostFolder >= 0 && mergeFolder >= 0)
		{
			ItemStack stack1 = ic.getStackInSlot(hostFolder);
			ItemStack stack2 = ic.getStackInSlot(mergeFolder);
			
			if (((ItemStack)ItemFolder.getObject(stack1)).getItem() == ((ItemStack)ItemFolder.getObject(stack2)).getItem() && ((ItemStack)ItemFolder.getObject(stack1)).getItemDamage() == ((ItemStack)ItemFolder.getObject(stack2)).getItemDamage())
			{
				long mergeCount = ItemFolder.getFileSize(stack2);
				if (mergeCount > 0)
				{
					StorageUtils.checkTapeNBT(stack1, true);
					StorageUtils.checkTapeNBT(stack2, true);
					
					ItemStack newFolder = stack1.copy();
					ItemFolder.add(newFolder, mergeCount);
					return newFolder;
				}
			}
		}
		return null;
	}
}
