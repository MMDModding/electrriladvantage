package cyano.electricadvantage.gui;

import cyano.electricadvantage.ElectricAdvantage;
import cyano.electricadvantage.machines.ElectricBatteryArrayTileEntity;
import cyano.poweradvantage.api.simple.SimpleMachineGUI;
import cyano.poweradvantage.math.Integer2D;
import net.minecraft.util.ResourceLocation;

public class BatteryArrayGUI extends SimpleMachineGUI{

	public BatteryArrayGUI() {
		super(new ResourceLocation(ElectricAdvantage.MODID+":textures/gui/container/electric_battery_array_gui.png"), 
				Integer2D.fromCoordinates(
						27, 32,
						27, 53,
						27, 74,
						27, 95,
						
						109, 32,
						109, 53,
						109, 74,
						109, 95
						));
	}
	
	/**
	 * Override this method to draw on the GUI window.
	 * <br><br>
	 * This method is invoked when drawing the GUI so that you can draw 
	 * animations and other foreground decorations to the GUI.
	 * @param srcEntity This is the TileEntity (or potentially a LivingEntity) 
	 * for whom we are drawing this interface
	 * @param guiContainer This is the instance of GUIContainer that is drawing 
	 * the GUI. You need to use it to draw on the screen. For example:<br>
	   <pre>
guiContainer.mc.renderEngine.bindTexture(arrowTexture);
guiContainer.drawTexturedModalRect(x+79, y+35, 0, 0, arrowLength, 17); // x, y, textureOffsetX, textureOffsetY, width, height)
	   </pre>
	 * @param x This is the x coordinate (in pixels) from the top-left corner of 
	 * the GUI
	 * @param y This is the y coordinate (in pixels) from the top-left corner of 
	 * the GUI
	 * @param z This is the z coordinate (no units) into the depth of the screen
	 */
	@Override
	public void drawGUIDecorations(Object srcEntity, GUIContainer guiContainer, int x, int y, float  z){
		if(srcEntity instanceof ElectricBatteryArrayTileEntity){
			ElectricBatteryArrayTileEntity te = (ElectricBatteryArrayTileEntity)srcEntity;
			float[] chargeLevels = te.batteryCharges();
			for(int n = 0; n < 4; n++){
				int i1 = n;
				int i2 = n + 4;
				GUIHelper.drawProgressBar(x+49,  y+36+21*n, chargeLevels[i1], guiContainer);
				GUIHelper.drawProgressBar(x+131, y+36+21*n, chargeLevels[i2], guiContainer);
			}
			
		}
	}

}
