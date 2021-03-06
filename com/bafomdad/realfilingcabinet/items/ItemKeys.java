package com.bafomdad.realfilingcabinet.items;

import java.util.List;
import java.util.UUID;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.bafomdad.realfilingcabinet.RealFilingCabinet;
import com.bafomdad.realfilingcabinet.TabRFC;
import com.bafomdad.realfilingcabinet.helpers.StringLibs;
import com.bafomdad.realfilingcabinet.utils.NBTUtils;

public class ItemKeys extends Item {
	
	public String[] keyTypes = new String[] { "master", "copy" };

	public ItemKeys() {
		
		setRegistryName("key");
		setUnlocalizedName(RealFilingCabinet.MOD_ID + ".rfckey");
		setHasSubtypes(true);
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(TabRFC.instance);
		GameRegistry.register(this);
	}
	
	public String getUnlocalizedName(ItemStack stack) {
		
		return getUnlocalizedName() + "_" + keyTypes[stack.getItemDamage()];
	}
	
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		
		for (int i = 0; i < keyTypes.length; ++i)
			list.add(new ItemStack(item, 1, i));
	}
	
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean whatisthis) {
		
		if (stack.getItemDamage() == 1 && (stack.hasTagCompound() && stack.getTagCompound().hasKey(StringLibs.RFC_COPY)))
		{
			EntityPlayer onlinePlayer = player.world.getPlayerEntityByUUID(UUID.fromString(NBTUtils.getString(stack, StringLibs.RFC_COPY, "")));
			if (onlinePlayer != null)
				list.add("Original Owner: " + onlinePlayer.getName());
			else
				list.add("Original Owner: " + NBTUtils.getString(stack, StringLibs.RFC_FALLBACK, ""));
		}
	}
	
	@SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack stack) {
    	
		if (stack.getItemDamage() == 0)
			return EnumRarity.UNCOMMON;
		
		return EnumRarity.COMMON;
    }
}
