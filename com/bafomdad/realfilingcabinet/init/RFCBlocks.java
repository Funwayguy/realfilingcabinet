package com.bafomdad.realfilingcabinet.init;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.bafomdad.realfilingcabinet.ConfigRFC;
import com.bafomdad.realfilingcabinet.blocks.*;
import com.bafomdad.realfilingcabinet.blocks.tiles.TileEntityRFC;
import com.bafomdad.realfilingcabinet.renders.RenderFilingCabinet;

public class RFCBlocks {

	public static BlockRFC blockRFC;
	public static BlockGarbageBin blockBin;
	
	public static void init() {
		
		blockRFC = new BlockRFC();
		if (ConfigRFC.binBlock)
			blockBin = new BlockGarbageBin();
	}
	
	@SideOnly(Side.CLIENT)
	public static void initModels() {

		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockRFC), 0, new ModelResourceLocation(blockRFC.getRegistryName(), "inventory"));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRFC.class, new RenderFilingCabinet());
		
		if (ConfigRFC.binBlock)
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockBin), 0, new ModelResourceLocation(blockBin.getRegistryName(), "inventory"));
	}
}
