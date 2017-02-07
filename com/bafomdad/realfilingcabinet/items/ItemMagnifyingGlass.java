package com.bafomdad.realfilingcabinet.items;

import com.bafomdad.realfilingcabinet.RealFilingCabinet;
import com.bafomdad.realfilingcabinet.TabRFC;
import com.bafomdad.realfilingcabinet.blocks.BlockRFC;
import com.bafomdad.realfilingcabinet.entity.EntityCabinet;
import com.bafomdad.realfilingcabinet.helpers.TextHelper;
import com.bafomdad.realfilingcabinet.init.RFCBlocks;
import com.bafomdad.realfilingcabinet.init.RFCItems;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemMagnifyingGlass extends Item {

	public ItemMagnifyingGlass() {
		
		setRegistryName("magnifyingglass");
		setUnlocalizedName(RealFilingCabinet.MOD_ID + ".magnifyingglass");
		setMaxStackSize(1);
		setCreativeTab(TabRFC.instance);
		GameRegistry.register(this);
	}
	
	@Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        
    	Block block = world.getBlockState(pos).getBlock();
    	if (player.isSneaking() && block == RFCBlocks.blockRFC) {
    		if (!world.isRemote)
        		world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockRFC.FACING, player.getHorizontalFacing().getOpposite()), 2);

    		return EnumActionResult.SUCCESS;
    	}
    	return EnumActionResult.PASS;
    }
    
	@Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
       
		if (target != null && target instanceof EntityCabinet) {
			
			EntityCabinet cabinet = (EntityCabinet)target;
			if (!player.isSneaking())
			{
				if (cabinet.getInventory() != null)
				{
					for (int i = 0; i < cabinet.getInventory().getSlots(); i++) {
						ItemStack stacky = cabinet.getInventory().getStackInSlot(i);
						if (stacky != null && stacky.getItem() == RFCItems.folder) {
							if (ItemFolder.getObject(stacky) != null)
							{
								String name = TextHelper.folderStr(stacky);
								long storedSize = ItemFolder.getFileSize(stacky);
								
								player.sendMessage(new TextComponentString(name + " - " + storedSize));
							}
						}
					}
					return true;
				}
			}
		}
		return false;
    }
}
