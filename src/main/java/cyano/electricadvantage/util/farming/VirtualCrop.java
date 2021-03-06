package cyano.electricadvantage.util.farming;

import cyano.electricadvantage.util.crafting.ItemRecord;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import java.lang.reflect.Field;
import java.util.*;

public class VirtualCrop {

	private static final Map<ItemRecord,VirtualCrop> customRecipes = new HashMap<>();

	public static void addCustomCrop(ItemStack seed, int numberGrowthStages, Collection<ItemStack> harvest){
		ItemStack single = seed.copy();
		single.setCount(1);
		customRecipes.put(new ItemRecord(seed), new VirtualCrop(single,numberGrowthStages,harvest));
	}
	
	public static void removeCustomCrop(ItemStack seed){
		customRecipes.remove(new ItemRecord(seed));
	}
	
	public static VirtualCrop createVirtualCrop(ItemStack stack, World w, BlockPos pos){
		if(stack == null || stack.getItem() == null) return null;
		ItemRecord r = new ItemRecord(stack);
		ItemStack seed = stack.copy();
		seed.setCount(1);
		Item i = stack.getItem();
		Block b;
		if(i instanceof ItemBlock){
			b = ((ItemBlock)i).getBlock();
		} else {
			b = null;
		}
		if(customRecipes.get(r) != null){
			return customRecipes.get(r).copy();
		} else if(i == Items.REEDS
				|| b instanceof net.minecraft.block.BlockMushroom
				|| b instanceof net.minecraft.block.BlockCactus
				|| b instanceof net.minecraft.block.BlockBush
				|| b instanceof net.minecraft.block.BlockGrass
				|| b instanceof net.minecraft.block.BlockVine
				|| b instanceof net.minecraft.block.BlockLilyPad){
			// duplicate
			ItemStack product = seed.copy();
			product.setCount(2);
			return new VirtualCrop(seed,8,Arrays.asList(product));
		} else if(i instanceof IPlantable){
			IPlantable plantable = (IPlantable)i;
			IBlockState startingState = plantable.getPlant(w, pos);
			Block block = startingState.getBlock();
			int numStages = getNumberOfGrowthStages(startingState);
			if(block instanceof BlockStem){
				Block product = getBlockFieldByReflection(block);
				if(product == null) {
					return null;
				}
				return new VirtualCrop(seed,numStages,Arrays.asList(new ItemStack(product,1,0)));
			} else {
				IBlockState agedState = ageToMax(startingState);
				return new VirtualCrop(startingState.getBlock().getDrops(w, pos, startingState, 0),
						getNumberOfGrowthStages(startingState),
						agedState.getBlock().getDrops(w, pos, agedState, 0));
			}
		}
		return null;
	}
	
	
	public VirtualCrop copy(){
		return new VirtualCrop(this.prematureHarvest,this.numberOfStages, this.harvest);
	}
	

	private static int getNumberOfGrowthStages(IBlockState startingState) {
		for(Object o : startingState.getProperties().entrySet()){
			Map.Entry e = (Map.Entry)o;
			if(e.getKey() instanceof PropertyInteger){
				PropertyInteger prop = (PropertyInteger)e.getKey();
				if("age".equals(prop.getName())){
					return prop.getAllowedValues().size();
				}
			}
		}
		return 8;
	}
	

	private static IBlockState ageToMax(IBlockState startingState) {
		for(Object o : startingState.getProperties().entrySet()){
			Map.Entry e = (Map.Entry)o;
			if(e.getKey() instanceof PropertyInteger){
				PropertyInteger prop = (PropertyInteger)e.getKey();
				if("age".equals(prop.getName())){
					int max = 0;
					for(Object i : prop.getAllowedValues()){
						if((Integer)i > max){max = (Integer)i;}
					}
					return startingState.withProperty(prop, Integer.valueOf(max));
				}
			}
		}
		return startingState;
	}
	
	private static Block getBlockFieldByReflection(Object target) {
		Class<Block> type = Block.class;
		Field[] fields = target.getClass().getDeclaredFields();
		try {
			for (Field f : fields){
				if(type.isAssignableFrom(f.getType())){
					// found a block member variable, I hope it is the right one!
					f.setAccessible(true);
					Object got;
					got = f.get(target);
					return (Block)got;
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
		// no block member variables found
		return null;
	}

	private final byte numberOfStages;
	private byte stage = 0;
	private final List<ItemStack> harvest;
	private final List<ItemStack> prematureHarvest;

	public VirtualCrop(ItemStack item, int numGrowthStages, Collection<ItemStack> harvest){
		this.numberOfStages = (byte)(numGrowthStages & 0x7F);
		this.prematureHarvest = Arrays.asList(item);
		this.harvest = removeInvalidItemStacks(harvest);
		if(harvest.isEmpty()){
			ItemStack pity = item.copy();
			pity.setCount(2);
			harvest.add(pity);
		}
	}
	
	public VirtualCrop(Collection<ItemStack> earlyHarvest, int numGrowthStages, Collection<ItemStack> harvest){
		this.numberOfStages = (byte)(numGrowthStages & 0x7F);
		this.prematureHarvest = new ArrayList<>(earlyHarvest);
		this.harvest = removeInvalidItemStacks(harvest);
	}
	
	public List<ItemStack> getHarvest(){
		if(stage >= numberOfStages){
			return harvest;
		} else {
			return prematureHarvest;
		}
	}
	/**
	 * Grows this crop by 1 growth stage
	 * @return True if the crop is ready for harvest, false otherwise
	 */
	public boolean grow(){
		stage++;
		return stage >= numberOfStages;
	}

	public byte getCurrentGrowth(){
		return this.stage;
	}
	public void setCurrentGrowth(byte newValue){
		this.stage = (newValue > getMaxGrowth() ? getMaxGrowth() : newValue);
	}
	
	public byte getMaxGrowth(){
		return this.numberOfStages;
	}
	
	private static List<ItemStack> removeInvalidItemStacks(Collection<ItemStack> items){
		/// CURSE YOU, MASTERCHEF MOD! DON'T YOU KNOW THAT ITEMSTACKS CAN'T HAVE NULL ITEMS?!?!
		List<ItemStack> valid = new ArrayList<>(items.size());
		for(ItemStack i : items){
			if(i.getItem() != null && i.getCount() > 0){
				valid.add(i);
			}
		}
		return valid;
	}
	
}
