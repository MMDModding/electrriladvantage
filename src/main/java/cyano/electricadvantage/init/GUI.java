package cyano.electricadvantage.init;

import cyano.poweradvantage.PowerAdvantage;
import cyano.poweradvantage.registry.MachineGUIRegistry;
import cyano.electricadvantage.gui.*;

public abstract class GUI {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		Entities.init();

		Blocks.steam_powered_generator.setGuiID(MachineGUIRegistry.addGUI(new PowerGeneratorGUI()), PowerAdvantage.getInstance());
		Blocks.photovoltaic_generator.setGuiID(MachineGUIRegistry.addGUI(new PowerGeneratorGUI()), PowerAdvantage.getInstance());
		
		Blocks.electric_furnace.setGuiID(MachineGUIRegistry.addGUI(new ArcFurnaceGUI()), PowerAdvantage.getInstance());
		
		initDone = true;
	}
}
