package cyano.electricadvantage.machines;

import cyano.electricadvantage.init.Power;
import cyano.poweradvantage.api.ConduitType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ElectricMachineBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine{

	/**
	 * Blockstate property
	 */
	public static final PropertyBool ACTIVE = PropertyBool.create("active");
	/**
	 * Blockstate property
	 */
	public static final PropertyBool POWERED = PropertyBool.create("powered");

	public ElectricMachineBlock() {
		this(Material.PISTON);
	}

	public ElectricMachineBlock(Material m, ConduitType type) {
		super(m, 0.75f, type);
		this.setDefaultState(getDefaultState().withProperty(ACTIVE, false).withProperty(POWERED, false).withProperty(FACING, EnumFacing.NORTH));
	}

	public ElectricMachineBlock(Material m) {
		this(m,Power.ELECTRIC_POWER);
	}
	
	/**
	 * Creates a blockstate
	 */
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { ACTIVE,FACING,POWERED });
	}

	/**
	 * Converts metadata into blockstate
	 */
	@Override
	public IBlockState getStateFromMeta(final int metaValue) {
		EnumFacing enumFacing = metaToFacing(metaValue);
		if (enumFacing.getAxis() == EnumFacing.Axis.Y) {
			enumFacing = EnumFacing.NORTH;
		}
		return this.getDefaultState().withProperty( FACING, enumFacing)
				.withProperty(ACTIVE, (metaValue & 0x4) != 0)
				.withProperty(POWERED, (metaValue & 0x8) != 0);
	}
	
	/**
	 * Converts blockstate into metadata
	 */
	@Override
	public int getMetaFromState(final IBlockState bs) {
		int extraBit;
		if((Boolean)(bs.getValue(ACTIVE))){
			extraBit = 0x4;
		} else {
			extraBit = 0;
		}
		if((Boolean)(bs.getValue(POWERED))){
			extraBit = extraBit | 0x8;
		}
		return facingToMeta((EnumFacing)bs.getValue( FACING)) | extraBit;
	}
	
	private int facingToMeta(EnumFacing f){
		switch(f){
			case NORTH: return 0;
			case WEST: return 1;
			case SOUTH: return 2;
			case EAST: return 3;
			default: return 0;
		}
	}
	private EnumFacing metaToFacing(int i){
		int f = i & 0x03;
		switch(f){
			case 0: return EnumFacing.NORTH;
			case 1: return EnumFacing.WEST;
			case 2: return EnumFacing.SOUTH;
			case 3: return EnumFacing.EAST;
			default: return EnumFacing.NORTH;
		}
	}
	
	@Override
	public abstract ElectricMachineTileEntity createNewTileEntity(World w, int m);

	@Override
	public int getComparatorInputOverride(IBlockState bs, World w, BlockPos p){
		return getComparatorInputOverride(w,p);
	}
	public int getComparatorInputOverride(World w, BlockPos p) {
		TileEntity te = w.getTileEntity(p);
		if(te instanceof ElectricMachineTileEntity){
			return ((ElectricMachineTileEntity)te).getComparatorOutput();
		}
		return 0;
		
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState bs){
		return hasComparatorInputOverride();
	}
	public boolean hasComparatorInputOverride() {
		return true;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof IInventory)
		{
			InventoryHelper.dropInventoryItems(world, pos, (IInventory)te);
			((IInventory)te).clear();
			world.updateComparatorOutputLevel(pos, this);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean isPowerSink(ConduitType e){
		return true;
	}

	@Override
	public boolean isPowerSource(ConduitType e){
		return false;
	}

	public ConduitType getType() {return Power.ELECTRIC_POWER;}
}
