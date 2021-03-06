package com.bafomdad.realfilingcabinet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.bafomdad.realfilingcabinet.ConfigRFC;
import com.bafomdad.realfilingcabinet.blocks.tiles.TileEntityRFC;
import com.bafomdad.realfilingcabinet.helpers.StringLibs;
import com.bafomdad.realfilingcabinet.init.RFCItems;
import com.bafomdad.realfilingcabinet.items.ItemFolder;

public class FluidUtils {

	public static boolean doPlace(World world, EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing facing) {
		
		if (!NBTUtils.getBoolean(stack, StringLibs.RFC_PLACEMODE, false))
			return false;
		
		if (!MobUtils.canPlayerChangeStuffHere(world, player, stack, pos, facing))
			return false;
		
		long count = ItemFolder.getFileSize(stack);
		if (count >= 1000)
		{
			pos = pos.offset(facing);
			Block hitblock = world.getBlockState(pos).getBlock();
			Block liquid = ((FluidStack)ItemFolder.getObject(stack)).getFluid().getBlock();
			Fluid fluid = FluidRegistry.getFluid(ItemFolder.getFileName(stack));
			int l = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
			
			if (!hitblock.isReplaceable(world, pos))
				return false;
			
			if (liquid != null && (hitblock != liquid || (hitblock == liquid && l != 0)))
			{
				if (liquid == Blocks.WATER && world.provider.doesWaterVaporize() && !ConfigRFC.waterNether) {
                    world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                    for (int k = 0; k < 8; ++k)
                    {
                        world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)pos.getX() + Math.random(), (double)pos.getY() + Math.random(), (double)pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D, new int[0]);
                    }
                    if (!player.world.isRemote && !player.capabilities.isCreativeMode)
                    	ItemFolder.remove(stack, 1000);
					return true;
				}
				if (!player.world.isRemote && !player.capabilities.isCreativeMode)
					ItemFolder.remove(stack, 1000);
				
				world.setBlockState(pos, liquid.getDefaultState(), 3);
				world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
				return true;
			}
			else if (fluid != null && (hitblock != fluid.getBlock() || (hitblock == fluid.getBlock() && l != 0)))
			{
				if (!player.world.isRemote && !player.capabilities.isCreativeMode)
					ItemFolder.remove(stack, 1000);
				
				world.setBlockState(pos, fluid.getBlock().getDefaultState(), 3);
				return true;
			}
		}
		return false;
	}
	
	public static boolean doDrain(World world, EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing facing) {
		
		if (NBTUtils.getBoolean(stack, StringLibs.RFC_PLACEMODE, false))
			return false;
		
		if (!MobUtils.canPlayerChangeStuffHere(world, player, stack, pos, facing))
			return false;
		
		Block block = world.getBlockState(pos).getBlock();
		int l = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
		
		if (block instanceof BlockLiquid && l == 0) {
			if (ItemFolder.getObject(stack) != null && ItemFolder.getFileName(stack).equals(block.getLocalizedName()))
			{
				if (!world.isRemote) {
					ItemFolder.add(stack, 1000);
					world.setBlockToAir(pos);
				}
				return true;
			}
		}
		else if (block instanceof IFluidBlock && l == 0) {
			Fluid fluid = ((IFluidBlock)block).getFluid();
			if (ItemFolder.getObject(stack) != null && ItemFolder.getFileName(stack).equals(fluid.getName()))
			{
				if (!world.isRemote) {
					ItemFolder.add(stack, 1000);
					world.setBlockToAir(pos);
				}
				return true;
			}
		}
		return false;
	}
	
	public static boolean canFill(World world, EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing facing) {
		
		if (!MobUtils.canPlayerChangeStuffHere(world, player, stack, pos, facing))
			return false;
		
		if (world.getTileEntity(pos) == null)
			return false;
		
		if (world.getTileEntity(pos).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
			IFluidHandler handler = world.getTileEntity(pos).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
			int fillamount = handler.fill((FluidStack)ItemFolder.getObject(stack), true);
			if (fillamount > 0) {
				if (!world.isRemote && !player.capabilities.isCreativeMode)
					ItemFolder.remove(stack, fillamount);
				return true;
			}
		}
		return false;
	}
	
	public static boolean canDrain(World world, EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing facing) {
		
		if (!MobUtils.canPlayerChangeStuffHere(world, player, stack, pos, facing))
			return false;
		
		if (world.getTileEntity(pos) == null)
			return false;

		if (world.getTileEntity(pos).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
			IFluidHandler handler = world.getTileEntity(pos).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
			FluidStack fluid = handler.drain(1000, false);
			if (fluid != null && ItemFolder.getFileName(stack).equals(fluid.getLocalizedName())) {
				handler.drain(1000, true);
				ItemFolder.add(stack, fluid.amount);
				return true;
			}
		}
		return false;
	}
	
	public static boolean canAcceptFluidContainer(ItemStack stack) {
		
		return FluidUtil.getFluidContained(stack) != null;
	}
	
	public static FluidStack getFluidFromFolder(TileEntityRFC tile, int slot) {
		
		ItemStack stack = tile.getInventory().getTrueStackInSlot(slot);
		if (stack != null && stack.getItem() == RFCItems.folder && stack.getItemDamage() == 4)
		{
			int count = (int)ItemFolder.getFileSize(stack);
			if (ItemFolder.getObject(stack) == null || !(ItemFolder.getObject(stack) instanceof FluidStack))
				return null;
			
			return (FluidStack)ItemFolder.getObject(stack);
		}
		return null;
	}
}
