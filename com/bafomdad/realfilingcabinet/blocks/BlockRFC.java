package com.bafomdad.realfilingcabinet.blocks;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.bafomdad.realfilingcabinet.RealFilingCabinet;
import com.bafomdad.realfilingcabinet.TabRFC;
import com.bafomdad.realfilingcabinet.api.IFilingCabinet;
import com.bafomdad.realfilingcabinet.api.IFolder;
import com.bafomdad.realfilingcabinet.api.IUpgrades;
import com.bafomdad.realfilingcabinet.blocks.tiles.TileEntityRFC;
import com.bafomdad.realfilingcabinet.helpers.StringLibs;
import com.bafomdad.realfilingcabinet.helpers.UpgradeHelper;
import com.bafomdad.realfilingcabinet.init.RFCItems;
import com.bafomdad.realfilingcabinet.items.ItemFolder;
import com.bafomdad.realfilingcabinet.items.ItemKeys;
import com.bafomdad.realfilingcabinet.utils.AutocraftingUtils;
import com.bafomdad.realfilingcabinet.utils.EnderUtils;
import com.bafomdad.realfilingcabinet.utils.FluidUtils;
import com.bafomdad.realfilingcabinet.utils.MobUtils;
import com.bafomdad.realfilingcabinet.utils.NBTUtils;
import com.bafomdad.realfilingcabinet.utils.StorageUtils;

public class BlockRFC extends Block implements IFilingCabinet {
	
	static float f = 0.0625F;
	protected static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.0D + f, 0.0D, 0.0D + f, 1.0D - f, 1.0D - f, 1.0D - f);

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
	public BlockRFC() {
		
		super(Material.IRON);
		setRegistryName("modelcabinet");
		setUnlocalizedName(RealFilingCabinet.MOD_ID + ".filingcabinet");
		setHardness(5.0F);
		setResistance(1000.0F);
		setCreativeTab(TabRFC.instance);
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlockRFC(this), getRegistryName());
		GameRegistry.registerTileEntity(TileEntityRFC.class, "tileFilingCabinet");
		
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		
		return new BlockStateContainer(this, FACING);
	}
	
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB aabb, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity) {
        
    	addCollisionBoxToList(pos, aabb, collidingBoxes, BASE_AABB);
    }
    
    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    	
    	if (side == EnumFacing.DOWN || side.getIndex() == state.getValue(FACING).getIndex()) {
    		return false;
    	}
    	return true;
    }
	
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
    	
    	TileEntityRFC tileRFC = (TileEntityRFC)world.getTileEntity(pos);
    	if (UpgradeHelper.getUpgrade(tileRFC, StringLibs.TAG_MOB) == null)
    		return;
    	
    	if (!(entity instanceof EntityLivingBase) || entity instanceof EntityPlayer)
    		return;
    	
    	EntityLivingBase elb = (EntityLivingBase)entity;
    	if (!elb.isNonBoss() || (elb.isChild() && !(elb instanceof EntityZombie)))
    		return;
    	
    	String entityName = EntityList.getEntityString(elb);
    	for (int i = 0; i < tileRFC.getInventory().getSlots(); i++) {
    		ItemStack folder = tileRFC.getInventory().getTrueStackInSlot(i);
    		if (folder != null && folder.getItem() == RFCItems.folder) {
    			if (folder.getItemDamage() == 3 && ItemFolder.getObject(folder) != null)
    			{
    				if (ItemFolder.getObject(folder).equals(entityName)) {
    					MobUtils.dropMobEquips(world, elb);
    					elb.setDead();
    					ItemFolder.add(folder, 1);
    					break;
    				}
    			}
    		}
    	}
    }
	
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		
		TileEntity tile = world.getTileEntity(pos);
		if (!world.isRemote && !player.capabilities.isCreativeMode)
		{
			if (tile != null && tile instanceof TileEntityRFC)
				leftClick(tile, player);
		}
	}
	
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
    	TileEntity tile = world.getTileEntity(pos);
    	if (tile != null && tile instanceof TileEntityRFC)
    		rightClick(tile, player, side, hitX, hitY, hitZ);
    	
        return true;
    }
    
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    	
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
	
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		
		world.setBlockState(pos, state.withProperty(FACING, entity.getHorizontalFacing().getOpposite()), 2);
		TileEntity tile = world.getTileEntity(pos);
		if (tile != null && tile instanceof TileEntityRFC)
		{
			if (stack.hasTagCompound())
				((TileEntityRFC)tile).readInv(stack.getTagCompound());
		}
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		
		return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		
		return state.getValue(FACING).getHorizontalIndex();
	}
	
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
    	
    	if (player.capabilities.isCreativeMode && !world.isRemote) {
    		this.harvestBlock(world, player, pos, state, world.getTileEntity(pos), player.getActiveItemStack());
    	}
    	return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
	
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity tile, ItemStack stack) {
		
		if (tile instanceof TileEntityRFC)
		{
			ItemStack s = new ItemStack(this);
			NBTTagCompound tag = new NBTTagCompound();
			((TileEntityRFC)tile).writeInv(tag, true);
			if (!((TileEntityRFC)tile).upgrades.isEmpty())
			{
				ItemStack upgrade = UpgradeHelper.stackTest((TileEntityRFC)tile);
				if (upgrade != null && upgrade.stackSize == 0)
					upgrade.stackSize = 1;
				world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), upgrade));
			}
			if (!tag.hasNoTags())
			{
				s.setTagCompound(tag);
				world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), s));
				return;
			}
		}
		super.harvestBlock(world, player, pos, state, tile, stack);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		return new TileEntityRFC();
	}

	@Override
	public void leftClick(TileEntity tile, EntityPlayer player) {
		
		if (player.capabilities.isCreativeMode)
			return;
		
		TileEntityRFC tileRFC = (TileEntityRFC)tile;
		
		if (tileRFC.isCabinetLocked()) {
			if (!tileRFC.getCabinetOwner().equals(player.getUniqueID())) {
				if (!tileRFC.hasKeyCopy(player, tileRFC.getCabinetOwner()))
					return;
			}
		}
		if (player.isSneaking() && player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() == RFCItems.magnifyingGlass)
		{
			if (!tileRFC.getWorld().isRemote) {
				UpgradeHelper.removeUpgrade(player, tileRFC);
			}
			tileRFC.markBlockForUpdate();
			return;
		}
		if (UpgradeHelper.getUpgrade(tileRFC, StringLibs.TAG_CRAFT) == null)
			StorageUtils.extractStackManually(tileRFC, player, player.isSneaking());

		else
		{
			ItemStack toCraft = tileRFC.getFilter().copy();
			if (toCraft != null && toCraft.isItemDamaged())
				toCraft.setItemDamage(0);
			
			if (AutocraftingUtils.canCraft(toCraft, tileRFC))
			{
				ItemStack stack = toCraft;
				stack.stackSize = AutocraftingUtils.getOutputSize();
				if (!UpgradeHelper.isCreative(tileRFC))
					AutocraftingUtils.doCraft(tileRFC.getFilter(), tileRFC.getInventory());
				if (!player.inventory.addItemStackToInventory(stack))
					player.dropItem(stack.getItem(), 1);
			}
		}
	}

	@Override
	public void rightClick(TileEntity tile, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {

		TileEntityRFC tileRFC = (TileEntityRFC)tile;
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		
		if (tileRFC.isCabinetLocked()) {
			if (!tileRFC.getCabinetOwner().equals(player.getUniqueID()))
			{
				if (!tileRFC.hasKeyCopy(player, tileRFC.getCabinetOwner()))
					return;
			}
		}
		if (tileRFC.calcLastClick(player))
		{
			StorageUtils.addAllStacksManually(tileRFC, player);
		}
		if (!player.isSneaking() && stack != null)
		{
			if (stack.getItem() instanceof ItemKeys)
			{
				if (!tileRFC.isCabinetLocked()) {
					if (stack.getItemDamage() == 0)
						tileRFC.setOwner(player.getUniqueID());
				}
				else {
					if (tileRFC.getCabinetOwner().equals(player.getUniqueID()) && stack.getItemDamage() == 0) {
						tileRFC.setOwner(null);
						return;
					}
					if (tileRFC.getCabinetOwner().equals(player.getUniqueID()) && stack.getItemDamage() == 1) {
						if (!stack.hasTagCompound() || (stack.hasTagCompound() && !stack.getTagCompound().hasKey(StringLibs.RFC_COPY))) {
							NBTUtils.setString(stack, StringLibs.RFC_COPY, player.getUniqueID().toString());
							NBTUtils.setString(stack, StringLibs.RFC_FALLBACK, player.getDisplayNameString());
						}
					}
				}
				return;
			}
			if (stack.getItem() instanceof IFolder && tileRFC.isOpen)
			{
				if (stack.getItemDamage() == 1 && UpgradeHelper.getUpgrade(tileRFC, StringLibs.TAG_ENDER) != null)
				{
					player.setHeldItem(EnumHand.MAIN_HAND, null);
					return;
				}
				if (stack.getItemDamage() == 4 && UpgradeHelper.getUpgrade(tileRFC, StringLibs.TAG_FLUID) != null)
				{
					for (int i = 0; i < tileRFC.getInventory().getSlots(); i++) {
						ItemStack tileStack = tileRFC.getInventory().getTrueStackInSlot(i);
						if (tileStack == null)
						{
							tileRFC.getInventory().setStackInSlot(i, stack);
							player.setHeldItem(EnumHand.MAIN_HAND, null);
							tileRFC.markBlockForUpdate();
							break;
						}
					}
					return;
				}
				else if (stack.getItemDamage() != 1 && !tileRFC.getWorld().isRemote)
				{
					if (UpgradeHelper.getUpgrade(tileRFC, StringLibs.TAG_FLUID) != null && !FluidUtils.canAcceptFluidContainer(stack))
					{
						return;
					}
					for (int i = 0; i < tileRFC.getInventory().getSlots(); i++)
					{
						ItemStack tileStack = tileRFC.getInventory().getTrueStackInSlot(i);
						if (tileStack == null)
						{
							tileRFC.getInventory().setStackInSlot(i, stack);
							player.setHeldItem(EnumHand.MAIN_HAND, null);
							tileRFC.markBlockForUpdate();
							break;
						}
					}
				}
				return;
			}
			if (stack.getItem() instanceof IUpgrades) {
				if (!tileRFC.getWorld().isRemote) {
					UpgradeHelper.setUpgrade(player, tileRFC, stack);
				}
				tileRFC.markBlockForUpdate();
				return;
			}
			else
			{
				StorageUtils.addStackManually(tileRFC, player, stack);
			}
		}
		if (!player.isSneaking() && stack == null)
		{	
			if (!tileRFC.getWorld().isRemote)
			{
				if (tileRFC.isOpen)
					tileRFC.isOpen = false;
				else
					tileRFC.isOpen = true;
				tileRFC.markDirty();
			}
			tileRFC.markBlockForUpdate();
		}
		if (player.isSneaking() && stack == null && tileRFC.isOpen)
		{		
			if (UpgradeHelper.getUpgrade(tileRFC, StringLibs.TAG_ENDER) != null)
			{
				EnderUtils.extractEnderFolder(tileRFC, player);
				return;
			}
			StorageUtils.folderExtract(tileRFC, player, side, hitX, hitY, hitZ);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		
		return false;
	}
	
	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		
		return false;
	}
	
	@Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
	
	@Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }
	
	@Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
		
		TileEntityRFC tileRFC = (TileEntityRFC)world.getTileEntity(pos);
		if (tileRFC != null && tileRFC.isCabinetLocked()) {
			if (!tileRFC.getCabinetOwner().equals(player.getUniqueID()))
				return -1.0F;
		}
		return ForgeHooks.blockStrength(state, player, world, pos);
	}
}
