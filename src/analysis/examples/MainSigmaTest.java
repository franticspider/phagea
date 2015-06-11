package analysis.examples;

//import com.phagea.landscapeType;
//import com.phagea.nkpLandscape;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.phagea.landscapeType;
import com.phagea.phageaConfig;
import com.phagea.phageaEngine;
import com.phagea.phageaLandscape;

public class MainSigmaTest {
	
	/**
	 * @param args
	 */
	
	public static void configToFile(phageaConfig config, phageaLandscape landscape, String outfilename){
		
		try {
			
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfilename, false)));
		    String strout = config.printConfig();
			out.println(strout);
			out.close();
		} catch (IOException e) {
			// oh noes!
		}
	}
	
	public static void trialToFile(int trialno, phageaConfig config, phageaLandscape landscape, String outfilename){

		if(trialno == 0){
			configToFile(config,landscape,outfilename);
			
		}
		
		
		try {
			

			
			
			/** Accumulate values over the number of reps */
			float obsmin=0,genmin=0,obsmode=0,genmax=0,obsmax=0;
			
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfilename, true)));
		    
		    
		    phageaEngine engine = new phageaEngine(config, landscape);
			engine.verbose = false;
			
			engine.runAlgorithm();
			
			//System.out.println(""+engine.getObsMin()+","+engine.getGenMin()+","+engine.getObsMode()+","+engine.getGenMax()+","+engine.getObsMax());
			obsmin = engine.getObsMin();
			genmin = engine.getGenMin();
			obsmode = engine.getObsMode();
			genmax = engine.getGenMax();
			obsmax = engine.getObsMax();

			// }

			// if(config.nReps>1)
			// System.out.println("scaled fit wont work with >1 rep - recoding needed");
			float scfit = (obsmax - landscape.nkp.fitMin)
					/ (landscape.nkp.fitMax - landscape.nkp.fitMin);

			if (scfit > 1.0000001) {
				System.out.println("fintess higher than max? ");
				float[] gene = engine.getModalGene();
				// for(int i=0;i<gene.length;i++){
				// System.out.print(""+gene[i]);
				// }
				// System.out.print("\n");
				for (int i = 0; i < gene.length; i++) {
					System.out.print("" + ((int) gene[i]));
				}
				System.out.print("\n");
			}

			String strout = "" + trialno + "," + landscape.nkp.fitMin + ","
					+ (obsmin / config.nReps) + "," + (genmin / config.nReps)
					+ "," + (obsmode / config.nReps) + ","
					+ (genmax / config.nReps) + "," + (obsmax / config.nReps)
					+ "," + landscape.nkp.fitMax + "," + scfit;
			// System.out.println("");
			
			System.out.println(strout);
			out.println(strout);
			out.close();
		} catch (IOException e) {
			// oh noes!
		}
	}
	
	
	
	public static void main(String[] args) {
		
		/** Load what we are doing from config */
		phageaConfig config = new phageaConfig(args[0]);
		
		//String name = config.getTypeName();	
		//phageaLandscape landscape = new phageaLandscape(name);
		phageaLandscape landscape = new phageaLandscape(config);
		
		/** Get/Scale the whole landscape if appropriate */
		int ndims = landscape.getNdims();
		System.out.print("Evaluation on a "+ndims+"-dimensional landscape\n");
		switch(ndims){
			case 1:
				landscape.get1DLandscape();
				break;
			case 2:
				landscape.get2DLandscape();
				break;
			default:
				//if(config.type == landscapeType.NKP && config.nkpN < 21)
				//	landscape.get1DnkpLandscape();
		}
			
		//TODO: We have to get the landscape before we initialise the fitness values - feels a bit cludgy...
		config.setInitFit(landscape.getNdims(),landscape.getMaxValue(),landscape.getMinValue());
		config.nReps = 1;
		
		//float[] fit = landscape.get1DLandscape();
		String configString = config.printConfig();
		System.out.print(configString);
		
		phageaConfig cfgRanRep = config;
		phageaConfig cfgFixRep = config;
		
		/** Set the init */
		cfgRanRep.setRandInit(true);
		cfgFixRep.setRandInit(false);
		
		/** Set the phage */
		cfgRanRep.setReplenish(true);
		cfgFixRep.setReplenish(true);
				
		
		for(float sig = (float) 8; sig<20; sig += 0.5){
		/** Now we can run a bunch of trials to gather data */
			cfgRanRep.setAffinitySigma(sig);
			cfgFixRep.setAffinitySigma(sig);
			for(int nt = 30; nt < 40 ; nt ++){
	
				//System.out.println("NKP configured with random seed of "+nt);
				landscape = null;
				landscape = new phageaLandscape(config,nt);
				
				if(landscape==null)
					System.out.println("NKP landscape is null at this point ");
					
				
				landscape.findNKPMaxMin();
				
				trialToFile(nt,cfgRanRep,landscape,"nkpN"+config.nkpN+"K"+config.nkpK+"P"+config.nkpP+"_Sig"+config.getSigma2()+"_RanReplenish.txt");
				trialToFile(nt,cfgFixRep,landscape,"nkpN"+config.nkpN+"K"+config.nkpK+"P"+config.nkpP+"_Sig"+config.getSigma2()+"_FixReplenish.txt");
							
			}
		}
	}
	
}
