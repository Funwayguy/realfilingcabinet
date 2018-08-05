package com.bafomdad.realfilingcabinet.items.capabilities;

import com.bafomdad.realfilingcabinet.NewConfigRFC.ConfigRFC;
import com.bafomdad.realfilingcabinet.RealFilingCabinet;
import com.bafomdad.realfilingcabinet.helpers.TextHelper;
import com.bafomdad.realfilingcabinet.items.ItemFolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.lwjgl.input.Keyboard;

import java.util.List;

// Funwayguy: Your new capability based class for dealing with folder stuff. Unique to each item rootStack so feel free to add/remove stuf.
public class CapabilityFolder implements INBTSerializable<NBTTagCompound>
{
    // The ItemStack instance you're working within (REFERENCE PURPOSES ONLY!)
    private final ItemStack rootStack;
    
    private Object contents;
    private long count = 0;
    private int remSize = 0;
    
    private boolean placeMode = false;
    private boolean ignoreNbt = false;
    
    public CapabilityFolder(ItemStack rootStack)
    {
        this.rootStack = rootStack;
    }
    
    public void addTooltips(World world, List<String> list, ITooltipFlag tooltipFlag)
    {
        if(rootStack.getItemDamage() == ItemFolder.FolderType.FLUID.ordinal() && isFluidStack())
        {
            FluidStack fluid = getFluidStack();
            list.add(count + "mb " + fluid.getLocalizedName());
            list.add(placeMode ? TextFormatting.GREEN + TextHelper.localize("tooltip." + RealFilingCabinet.MOD_ID + ".placemode.on") : TextFormatting.RED + TextHelper.localize("tooltip." + RealFilingCabinet.MOD_ID + ".placemode.off"));
        } else if(rootStack.getItemDamage() == ItemFolder.FolderType.MOB.ordinal() && isEntity())
        {
            EntityLivingBase entity = getEntity();
            list.add(count + " " + entity.getName());
            if(!ConfigRFC.mobUpgrade)
            {
                list.add(TextHelper.localize("message." + RealFilingCabinet.MOD_ID + ".disabled"));
            }
        } else if(isItemStack())
        {
            ItemStack item = getItemStack();
    
            list.add((Keyboard.isKeyDown(42)) || (Keyboard.isKeyDown(54)) ? count + " " + item.getDisplayName() : TextHelper.format(count) + " " + item.getDisplayName());
    
            if(rootStack.getItemDamage() == ItemFolder.FolderType.DURA.ordinal())
            {
                list.add("Durability: " + remSize + " / " + item.getItemDamage());
                list.add(ignoreNbt ? TextFormatting.GREEN + TextHelper.localize("tooltip." + RealFilingCabinet.MOD_ID + ".ignorenbt.true") : TextFormatting.RED + TextHelper.localize("tooltip." + RealFilingCabinet.MOD_ID + ".ignorenbt.false"));
            }
        }
    }
    
    public ItemStack extractItems(long amount, boolean sim)
    {
        if(!isItemStack() || count <= 0)
        {
            return ItemStack.EMPTY;
        }
        
        ItemStack items = getItemStack().copy();
        items.setCount((int)Math.min(count, items.getMaxStackSize()));
        
        if(!sim)
        {
            count -= items.getCount();
        }
        
        return items;
    }
    
    public ItemStack insertItems(ItemStack items, boolean sim)
    {
        if(!isItemStack() || count <= 0)
        {
            return items;
        }
        
        ItemStack stack = getItemStack();
        
        if(!ItemStack.areItemsEqual(stack, items))
        {
            return items;
        }
        
        items.copy();
        items.setCount(0); // TODO: Add capcity/transfer limits here
        
        if(!sim)
        {
            count += items.getCount();
        }
        
        return items; // Return left overs that didn't fit
    }
    
    public boolean setContents(Object obj)
    {
        if(this.contents != null)
        {
            return false;
        } else if(obj instanceof EntityLivingBase)
        {
            EntityLivingBase entity = (EntityLivingBase)obj;
            
            if(entity instanceof EntityPlayer || (entity instanceof IEntityOwnable && ((IEntityOwnable)entity).getOwnerId() != null) || !entity.isNonBoss() || !(entity instanceof EntityZombie || !entity.isChild()))
            {
                return false;
            }
            
            this.contents = entity;
            this.count = 1;
            return true;
        } else if(obj instanceof ItemStack)
        {
            ItemStack stack = ((ItemStack)obj).copy();
            this.contents = stack.copy();
            this.count = stack.getCount();
            stack.setCount(1);
            
            if(rootStack.getItemDamage() == ItemFolder.FolderType.DURA.ordinal())
            {
                this.remSize = stack.getItemDamage();
            } else if(rootStack.getItemDamage() != ItemFolder.FolderType.NBT.ordinal())
            {
                // TODO: Test NBT folder
                stack.setTagCompound(null); // Delete the tags if this folder doesn't support it
            }
            
            return true;
        } else if(obj instanceof FluidStack)
        {
            this.contents = ((FluidStack)obj).copy();
            this.count = ((FluidStack)obj).amount;
            return true;
        }else if(obj instanceof IBlockState)
        {
            this.contents = obj;
            this.count = 1;
            return true;
        }
        
        return false;
    }
    
    @Deprecated // Really shouldn't be using this anymore
    public String getContentID()
    {
        if(isItemStack())
        {
            return getItemStack().getItem().getRegistryName().toString();
        } else if(isBlock())
        {
            return getBlock().getBlock().getRegistryName().toString();
        } else if(isFluidStack())
        {
            // WARNING: This is still slow!
            return FluidRegistry.getFluidName(getFluidStack());
        } else if(isEntity())
        {
            return EntityRegistry.getEntry(getEntity().getClass()).getRegistryName().toString();
        }
        
        return "";
    }
    
    public Object getContents()
    {
        return contents;
    }
    
    public boolean isItemStack()
    {
        return this.getContents() instanceof ItemStack;
    }
    
    public long getCount()
    {
        return this.count;
    }
    
    public void setCount(long value)
    {
        this.contents = value;
    }
    
    public int getRemaining()
    {
        return this.remSize;
    }
    
    public void setRemaining(int value)
    {
        this.remSize = value;
    }
    
    public ItemStack getItemStack()
    {
        return isItemStack() ? (ItemStack)getContents() : ItemStack.EMPTY;
    }
    
    public boolean isFluidStack()
    {
        return this.getContents() instanceof FluidStack;
    }
    
    public FluidStack getFluidStack()
    {
        return isFluidStack() ? (FluidStack)getContents() : null;
    }
    
    public boolean isBlock()
    {
        return this.getContents() instanceof IBlockState;
    }
    
    public IBlockState getBlock()
    {
        return isBlock() ? (IBlockState)getContents() : Blocks.AIR.getDefaultState();
    }
    
    public boolean isEntity()
    {
        return this.getContents() instanceof EntityLivingBase;
    }
    
    public EntityLivingBase getEntity()
    {
        return isEntity() ? (EntityLivingBase)getContents() : null;
    }
    
    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        
        if(getContents() == null) // Fail fast if there's nothing in here
        {
            nbt.setByte("folderType", (byte)0);
        } else if(isItemStack())
        {
            ItemStack item = getItemStack();
            
            nbt.setByte("folderType", (byte)1);
            nbt.setTag("objectData", item.writeToNBT(new NBTTagCompound()));
        } else if(isBlock())
        {
            IBlockState blockState = getBlock();
            
            nbt.setByte("folderType", (byte)2);
            NBTTagCompound dataTag = new NBTTagCompound();
            dataTag.setString("blockId", blockState.getBlock().getRegistryName().toString());
            dataTag.setInteger("blockMeta", blockState.getBlock().getMetaFromState(blockState));
            nbt.setTag("objectData", dataTag);
        } else if(isFluidStack())
        {
            FluidStack fluid = getFluidStack();
            
            nbt.setByte("folderType", (byte)3);
            nbt.setTag("objectData", fluid.writeToNBT(new NBTTagCompound()));
        } else if(isEntity())
        {
            EntityLivingBase entity = getEntity();
            nbt.setByte("folderType", (byte)4);
            NBTTagCompound dataTag = new NBTTagCompound();
            entity.writeToNBTAtomically(dataTag);
            nbt.setTag("objectData", dataTag);
        }
        
        nbt.setLong("folderSize", this.count);
        nbt.setInteger("folderRem", this.remSize);
        
        return nbt;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        // TODO: Add support for converting over the old NBT tags.
        if(rootStack.hasTagCompound() && rootStack.getTagCompound().hasKey("fileName", 8))
        {
            // Legacy conversion. Ignores NBT parameter and reads directly off the stack
            // Notes:
            // - Remove old tags so this doesn't run again and overwrite the new values.
            // - Not using NBTUtils for this because it'd be unnecessary at this point
            NBTTagCompound rootTag = rootStack.getTagCompound();
            String fName = rootTag.getString("fileName");
            int fMeta = rootTag.getInteger("fileMeta");
            this.count = rootTag.getLong("fileSize");
            this.remSize = rootTag.getInteger("leftoverSize");
            NBTTagCompound tags = rootTag.getCompoundTag("itemTagCompound");
            
            // Now to figure out what it was without breaking it
            if(rootStack.getItemDamage() == ItemFolder.FolderType.FLUID.ordinal())
            {
                // Fluid blocks aren't really a thing players should normally get so I'm going to going to ignore it
                Fluid fluid = FluidRegistry.getFluid(fName);
                this.contents = new FluidStack(fluid, 1);
                
                return;
            }
    
            ResourceLocation res = new ResourceLocation(fName);
            Item item = Item.REGISTRY.getObject(res);
            
            if(item != null && item != Items.AIR)
            {
                this.contents = new ItemStack(item, 1, fMeta);
                
                if(!tags.isEmpty())
                {
                    ((ItemStack)contents).setTagCompound(tags);
                }
                
                return;
            }
            
            Block block = Block.REGISTRY.getObject(res);
            
            if(block != null && block != Blocks.AIR)
            {
                contents = block.getStateFromMeta(fMeta);
            }
            
            return;
        }
        
        // Back to normal read/write
        
        byte type = nbt.getByte("fileType");
        
        switch(type)
        {
            case 0: // Empty. Reset everything ignore reading the rest
            {
                this.contents = null;
                this.count = 0;
                this.remSize = 0;
                break;
            }
            case 1: // ItemStack
            {
                this.contents = new ItemStack(nbt.getCompoundTag("objectData"));
                break;
            }
            case 2: // BlockState
            {
                NBTTagCompound tags = nbt.getCompoundTag("objectData");
                Block block = Block.REGISTRY.getObject(new ResourceLocation(tags.getString("blockId")));
                this.contents = block.getStateFromMeta(tags.getInteger("blockMeta"));
                break;
            }
            case 3: // FluidStac
            {
                this.contents = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("objectData"));
                break;
            }
            case 4: // Entity
            {
                // TODO: Test this (null world may cause issues)
                this.contents = EntityList.createEntityFromNBT(nbt.getCompoundTag("objectData"), null);
            }
        }
        
        this.count = nbt.getLong("folderSize");
        this.remSize = nbt.getInteger("folderRem");
    }
}
