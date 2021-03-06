package com.bafomdad.realfilingcabinet.init;

import com.bafomdad.realfilingcabinet.ConfigRFC;
import com.bafomdad.realfilingcabinet.RealFilingCabinet;
import com.bafomdad.realfilingcabinet.TabRFC;
import com.bafomdad.realfilingcabinet.items.*;
import com.bafomdad.realfilingcabinet.items.ItemFolder.FolderType;
import com.bafomdad.realfilingcabinet.items.ItemUpgrades.UpgradeType;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RFCItems {

	public static ItemEmptyFolder emptyFolder;
	public static ItemFolder folder;
	public static ItemMagnifyingGlass magnifyingGlass;
	public static ItemWhiteoutTape whiteoutTape;
	public static ItemUpgrades upgrades;
	public static ItemFilter filter;
	public static ItemKeys keys;
	public static ItemDebugger debugger;
	public static ItemMysteryFolder mysteryFolder;
	
	// Botania integration
	public static ItemManaFolder manaFolder;
	
	// Thaumcraft integration
	public static ItemAspectFolder aspectFolder;
	
	public static void init() {
		
		emptyFolder = new ItemEmptyFolder();
		folder = new ItemFolder();
		magnifyingGlass = new ItemMagnifyingGlass();
		whiteoutTape = new ItemWhiteoutTape();
		upgrades = new ItemUpgrades();
		filter = new ItemFilter();
		keys = new ItemKeys();
		debugger = new ItemDebugger();
		mysteryFolder = new ItemMysteryFolder();
		
		if (RealFilingCabinet.botaniaLoaded && ConfigRFC.botaniaIntegration) {
			manaFolder = new ItemManaFolder();
		}
		if (RealFilingCabinet.tcLoaded && ConfigRFC.tcIntegration) {
			aspectFolder = new ItemAspectFolder();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void initModels() {
		
		for (int i = 0; i < emptyFolder.folderType.length; ++i)
			ModelLoader.setCustomModelResourceLocation(emptyFolder, i, new ModelResourceLocation(emptyFolder.getRegistryName() + "_" + emptyFolder.folderType[i], "inventory"));
		
		for (int i = 0; i < FolderType.values().length; ++i)
			ModelLoader.setCustomModelResourceLocation(folder, i, new ModelResourceLocation(folder.getRegistryName() + "_" + FolderType.values()[i].toString().toLowerCase(), "inventory"));
		
		for (int i = 0; i < UpgradeType.values().length; ++i)
			ModelLoader.setCustomModelResourceLocation(upgrades, i, new ModelResourceLocation(upgrades.getRegistryName() + "_" + UpgradeType.values()[i].toString().toLowerCase(), "inventory"));
		
		ModelLoader.setCustomModelResourceLocation(magnifyingGlass, 0, new ModelResourceLocation(magnifyingGlass.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(whiteoutTape, 0, new ModelResourceLocation(whiteoutTape.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(filter, 0, new ModelResourceLocation(filter.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(debugger, 0, new ModelResourceLocation(debugger.getRegistryName(), "inventory"));
		
		ModelLoader.setCustomModelResourceLocation(keys, 0, new ModelResourceLocation(keys.getRegistryName() + "_" + keys.keyTypes[0], "inventory"));
		ModelLoader.setCustomModelResourceLocation(keys, 1, new ModelResourceLocation(keys.getRegistryName() + "_" + keys.keyTypes[1], "inventory"));
		
		ModelLoader.setCustomModelResourceLocation(mysteryFolder, 0, new ModelResourceLocation(mysteryFolder.getRegistryName(), "inventory"));
		
		if (RealFilingCabinet.botaniaLoaded && ConfigRFC.botaniaIntegration) {
			ModelLoader.setCustomModelResourceLocation(manaFolder, 0, new ModelResourceLocation(manaFolder.getRegistryName(), "inventory"));
		}
		if (RealFilingCabinet.tcLoaded && ConfigRFC.tcIntegration) {
			ModelLoader.setCustomModelResourceLocation(aspectFolder, 0, new ModelResourceLocation(aspectFolder.getRegistryName(), "inventory"));
		}
	}
}
