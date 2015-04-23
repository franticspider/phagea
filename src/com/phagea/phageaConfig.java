package com.phagea;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class phageaConfig {

	//TODO: The default values should be set for a known landscape 

	/**SPECIFICATION OF THE SIMULATION */
	public landscapeType type = landscapeType.THREEHILL;

	/**control flags*/
	public boolean	rescaling = true;	//rescale the fitness range
	public boolean	replenish = true;	//replenish phage if it goes extinct
	public boolean	randinit = true;	//randomly initialise genomes

	float	algMin=(float)0.8;
	float	algMax=(float)1.2;
	float	algRange=algMax-algMin;
	
	/** The number of divisions in each dimension of a continuous landscape (for creating graphics etc.) */
	int		nbin		=			100;

	/**INITIALISATION VALUES */
	int		startPhageCount=		1000;
	int		startCellCount=			1000;
	int		T			=			5000;
	
	float[]	initFit;
	float 	initFitVal 	= (float) 	0.2;
	
	/**THE INTERNAL MODEL PARAMETERS */
	float	mutsigma	= (float) 	0.005;
	
	//K = 1000 (so theta = 0.002/K)
	float	Theta 		= (float) 	0.002/1000;//1.5e-6;//0.002/1000;;
	int		b 			= 			71;
	float	w			= (float) 	0.066;
	float	sigma2		= (float) 	0.005;
	float	rho			= (float) 	1.0;

	
	float	r0			= (float) 	2.2;//1.2;//2.2;;
	float	rEpsilon	= (float) 	2.6E-6;//5e-4;// 2.6e-6; /** see page 8 of dynamics.pdf */;
	float	rGamma		= (float) 	0.0213;//1.2;
	float	rOmega		= 			w; //TODO: :Get rid of one of these variables!
	float	rK			= 			4;

	/** NKP Landscape varibles */
	public int		nkpN = 10;
	public int		nkpK = 8;
	public float	nkpP = 0;

	/** Number of times we repeat a simulation */
	public int	nReps = 10;
	
	/** These values are for setting experiments up 
	public float 	exptFloat1;
	public float 	exptFloat2;
	public float 	exptFloat3;*/
	
	
	
	public phageaConfig(String fn) {
		Properties cfgfile = new Properties();
		try {
			//cfg.load(this.getClass().getClassLoader().getResourceAsStream(fn));
			//. . .
			FileInputStream in = new FileInputStream(fn);
			cfgfile.load(in);
			in.close();
			
			/** some_var = configFile.getProperty("some_key"); */
			rescaling 	= Boolean.parseBoolean(cfgfile.getProperty("RESCALING",""+rescaling));
			replenish 	= Boolean.parseBoolean(cfgfile.getProperty("REPLENISH",""+replenish));
			randinit 	= Boolean.parseBoolean(cfgfile.getProperty("RANDINIT",""+randinit));
			
			//TODO: Check that algMax > algMin, flag an error if not
			algMin 		= Float.parseFloat(cfgfile.getProperty("ALGMIN",""+algMin));
			algMax 		= Float.parseFloat(cfgfile.getProperty("ALGMAX",""+algMax));
			algRange 	= algMax-algMin;//"(float) 2

			nbin 		= Integer.parseInt(cfgfile.getProperty("NBIN",""+nbin));//1000;
			
			startPhageCount	= Integer.parseInt(cfgfile.getProperty("STARTPHAGECOUNT",""+startPhageCount));//1000;
			startCellCount	= Integer.parseInt(cfgfile.getProperty("STARTCELLCOUNT",""+startCellCount));//1000;
			//Timesteps
			T			= Integer.parseInt(cfgfile.getProperty("T",""+T));
			initFitVal 	= Float.parseFloat(cfgfile.getProperty("INITFIT",""+initFitVal));
			
			mutsigma 	= Float.parseFloat(cfgfile.getProperty("MUTSIGMA",""+mutsigma));
			Theta 		= Float.parseFloat(cfgfile.getProperty("THETA",""+Theta));		//"(float) 2.5e-6;//1.5e-6;//0.002/1000;
			b 			= Integer.parseInt(cfgfile.getProperty("B",""+b));			//71;
			w 			= Float.parseFloat(cfgfile.getProperty("W",""+w));			//(float) 0.066;
			sigma2		= Float.parseFloat(cfgfile.getProperty("SIGMA2",""+sigma2));		//(float) 0.005;
			rho			= Float.parseFloat(cfgfile.getProperty("RHO",""+rho));			//(float) 1.0;
			
			
			
			//Resource params:
			r0 			= Float.parseFloat(cfgfile.getProperty("R0",""+r0));//(float) 1.2;//2.2;
			rEpsilon 	= Float.parseFloat(cfgfile.getProperty("REPSILON",""+rEpsilon));//(float) 5e-4;// 2.6e-6; /** see page 8 of dynamics.pdf */
			rGamma 		= Float.parseFloat(cfgfile.getProperty("RGAMMA",""+rGamma));//(float) 1.2;//0.0213;//1.2 //TODO: Check which of these produces the values in dynamics.pdf
			rOmega 		= Float.parseFloat(cfgfile.getProperty("ROMEGA",""+rOmega));//w;
			rK 			= Float.parseFloat(cfgfile.getProperty("RK",""+rK));//4;
			
			nkpN 		= Integer.parseInt(cfgfile.getProperty("NKPN", ""+nkpN));
			nkpK 		= Integer.parseInt(cfgfile.getProperty("NKPK", ""+nkpK));
			nkpP 		= Float.parseFloat(cfgfile.getProperty("NKPP", ""+nkpP));
			
			
			type 		= landscapeType.parseLandscapeType(cfgfile.getProperty("LANDSCAPE", ""+type.name()));
			
		} catch (IOException e) {
			//Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** Getters and setters */
	public double getMutsigma(){
		return mutsigma;
	}
	
	public int getNbin(){
		return nbin;
	}
	
	public void setMutsigma(float mutsigma){
		this.mutsigma = mutsigma;
	}
	
	public void setPhageCount(int startPhageCount){
		this.startPhageCount = startPhageCount;
		if(startPhageCount<1)
			replenish = false;
	}
	
	/** GETTERS */
	
	public float getw(){
		return w;
	}
	public float getrGamma(){
		return rGamma;
	}
	
	public float getrEpsilon(){
		return rEpsilon;
	}
	
	public float getTheta(){
		return Theta;
	}
	
	
	
	
	public boolean getRescaling(){
		return rescaling;
	}

	public void setInitFit(int genomesize, float lmax, float lmin) {
		initFit = new float[genomesize];
		
		for(int i =0;i<initFit.length;i++){
			if(!randinit){
				initFit[i] = initFitVal;
			}
			else{
				initFit[i] = (float) (lmin + ((lmax-lmin)*Math.random()));
			}
		}
	}

	public String getTypeName() {
		if(type==null)
			System.out.print("Null type name - check config\n");
		return type.name();
	}

	public float getAlgMin() {
		return algMin;
	}

	public float getAlgMax() {
		return algMax;
	}


	public int getT() {
		return T;
	}
	
	public int getNKPN() {
		return nkpN;
	}
	

	public int getNKPK(){
		return nkpK;
	}
	
	public float getNKPP(){
		return nkpP;
	}
	
	public float getSigma2(){
		return sigma2;
	}
	
	
	/**DIAGNOSTICS 
	 * @return */
	public String printConfig(){

		String s = "";
		
		s = s + "landscape type = "+type.name()+"\n";
		if(type.name().equals("NKP")){
			s = s + "\tN = "+nkpN+"\n";
			s = s + "\tK = "+nkpK+"\n";
			s = s + "\tP = "+nkpP+"\n";
			
		}
		s = s +"\n";

		/**control flags*/
		s = s + "rescaling = "+rescaling+"\n";
		s = s + "replenish = "+replenish+"\n";
		s = s + "randinit  = "+randinit+"\n";
		s = s +"\n";

		s = s + "algMin    = "+algMin+"\n";
		s = s + "algMax    = "+algMax+"\n";
		s = s + "algScale  = "+algRange+"\n";
		
		/** The number of divisions in each dimension of a continuous landscape (for creating graphics etc.) */
		s = s + "nbin      = "+nbin+"\n";
		s = s + ""+"\n";
		
		/**INITIALISATION VALUES */
		s = s + "startPhageCount = "+startPhageCount+"\n";
		s = s + "startCellCount  = "+startCellCount+"\n";
		s = s + "T               = "+T+"\n";
		
		
		s = s + "initFitVal      = "+initFitVal+"\n"+"\n";
		
		/**THE INTERNAL MODEL PARAMETERS */
		s = s + "mutsigma        = "+mutsigma+"\n";
		
		//K = 1000 (so theta = 0.002/K)
		s = s + "Theta           = "+Theta+"\n";// 	0.002/1000;//1.5e-6;//0.002/1000;;
		s = s + "b               = "+b+"\n";//			71;
		s = s + "w               = "+w+"\n";//(float) 	0.066;
		s = s + "sigma2          = "+sigma2+"\n";//(float) 	0.005;
		s = s + "rho             = "+rho+"\n";
		s = s + ""+"\n";

		s = s + "r0              = "+r0+"\n";//(float) 	2.2;//1.2;//2.2;;
		s = s + "rEpsilon        = "+rEpsilon+"\n";//(float) 	5e-4;// 2.6e-6; /** see page 8 of dynamics.pdf */;
		s = s + "rGamma          = "+rGamma+"\n";//(float) 	1.2;
		s = s + "rOmega          = "+rOmega+"\n";
		s = s + "rK              = "+rK+"\n";//			4;
		
		return s;
	}

	public float getInitFitVal() {
		return initFitVal;
	}

	public void setRandInit(boolean c) {
		randinit = c;
	}

	public void setReplenish(boolean c) {
		replenish = c;
	}

	public void setRescaling(boolean c) {
		rescaling = c;
	}

	public void setStartPhageCount(int i) {
		startPhageCount = i;
	}

	public void setAffinitySigma(float sig) {
		sigma2 = sig;
	}
	
}

