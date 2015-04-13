package com.phagea;

import java.util.List;


/** This class handles the calculation of fitness in phagEA
 * The class contains a number of test cases. These are:
 * 
	ONEPEAK,
	THREEHUMPS,
	UNKNOWN
	
	
 * @author sjh
 *
 */
public class phageaLandscape {
	
	landscapeType type;
	
	private int sDim = 1000; //TODO: This should ALWAYS be cfg.ndims

	/** Scaling factor variables - to set fitness range between 0.8 and 1.2 */
	//final float n3hmax = (float) 1.0;
	//final float n3hmin = (float) 0.0;
	
	/** These are values within which the landscape must sit */
	//final float minRange = (float) 0.8; 
	//final float maxRange = (float) 1.2;
	

	
	/** These were used to calculate the fitness range of a population of agents*/
	//private float fitRangeMin;
	//private float fitRangeMax;
	//private float fitRangeScale;
	
	/** these are the OBSERVED landscape within minRange and maxRange */
	boolean gotScale = false;
	float minValue;
	float maxValue;
		
	private boolean	noRange;
	//private boolean is2D = false;

	//NKP landscape params
	public nkpLandscape nkp = null;

	//private boolean[]	nkpDone;
	//private float[]	nkpFitness;
	//private float[]	nkpfitfunc;
	
	
	
	public phageaLandscape(landscapeType type) {
		this.type = type;
	}

	public phageaLandscape(String type){
		
		if(type.equalsIgnoreCase("ONEPEAK"))
			this.type = landscapeType.ONEPEAK;
		else if(type.equalsIgnoreCase("THREEHUMPS"))
			this.type = landscapeType.THREEHUMPS;
		else if(type.equalsIgnoreCase("RIDGE"))
			this.type = landscapeType.RIDGE;
		else if(type.equalsIgnoreCase("CONE"))
			this.type = landscapeType.CONE;
		else if(type.equalsIgnoreCase("SOMBRERO"))
			this.type = landscapeType.SOMBRERO;
		else if(type.equalsIgnoreCase("THREEHILL"))
			this.type = landscapeType.THREEHILL;
		else if(type.equalsIgnoreCase("RASTRIGIN"))
			this.type = landscapeType.RASTRIGIN;
		else if(type.equalsIgnoreCase("NKP")){
			this.type = landscapeType.NKP;
		}
		else
			this.type = landscapeType.UNKNOWN;
		
	}

	public phageaLandscape(phageaConfig config) {
		this(config,0);
	}

	public phageaLandscape(phageaConfig config, int rseed) {
		this(config.type.name());
		sDim = config.nbin;
		if(type == landscapeType.NKP){
			nkp = new nkpLandscape(config.nkpN,config.nkpK,config.nkpP,rseed);
			sDim = getNKPdim();
		} 
	}
	

	public void getFitness(List<Agent> cells) {
		//TODO: Check compatibility of landscape with genome
		for(Agent cell:cells){
			cell.fitness = getFitness(cell.genome);
			cell.fitset = true;
		}	
	}
	
	private float ridgefitness(float[] genome){
		float s=0;
		float x = (float) Math.abs(genome[0]-0.5);
		
		s = (float) (1.2 - (0.4 * (x/0.5)));
		
		return s;
	}
	
	

	private float rastriginFitness(float[] genome){
		
		float x=0;
		
		int A = 10;
		
		/**
	for(size_t i = 0 ; i < dimension ; ++i)
	  fit += pow(params[i],2.0) + 10.0*(1.0 - cos(2.0*M_PI*params[i]));
	return fit;
		*/

		for(int i=0;i<genome.length;i++){
			//for(int n=0;n<A;n++){
				float y = 0;
				float v = (float) (-5.12 + (genome[i] * (2* 5.12)));

			    // Vectorized function for contouring.
			    //y = x1.^2 + x2.^2-cos(12*x1)-cos(18*x2)
				
				y = (float) ((v*v) - (A*Math.cos(v*2*Math.PI)));
				
				x+=y;
		}
			
		
		return -x;
	}
	
	
	
	private float threeHillfitness(float[] genome){
		
		float[] pos = {(float) 0.25,(float) 0.5,(float) 0.75};
		float[] weight = {(float) 0.5,(float) 1.0,(float) 1.5};
		
		
		float x = 0;
		
		float B = (float) 10.;
		
		for(int i = 0;i<pos.length;i++){
			float r = (float) ((float) Math.sqrt( Math.pow(genome[0]-pos[i], 2)+Math.pow(genome[1]-pos[i],2)));

			x += weight[i] * Math.exp(-B * r	);
			//x += weight[i] * (float) Math.sqrt(Math.pow(Math.abs(genome[0]-pos[i]),2)+Math.pow(Math.abs(genome[1]-pos[i]),2));
		}	
		//s = (float) (1.2 - (0.4 * (x/0.5)));
		
		return x;
	}
	
	
	
	
	
	
	private float conefitness(float[] genome){
		float s=0;
		
		float x = (float) Math.sqrt(Math.pow(Math.abs(genome[0]-0.5),2)+Math.pow(Math.abs(genome[1]-0.5),2));
		
		s = (float) (1.2 - (0.4 * (x/0.5)));
		
		return s;
	}
	
	
	private float sombrerofitness(float[] genome){
		
		/**
		 
		 f(x,y) = A * cos^2(sqrt(x^2 + y^2)) * exp(-B * sqrt(x^2 + y^2))
		 
		 */
		
		float A = (float) 36;
		float B = (float) 0.02;
		
		float r = (float) ((float) A * Math.sqrt( Math.pow(genome[0]-0.5, 2)+Math.pow(genome[1]-0.5,2)));
		
		float s = 	(float) ((float)
					(1+Math.pow(Math.cos(r),2))
					* 
					Math.exp(-B * r	)
					);
		return s;
	}
	
	/*TODO: NKP - this was how I originally designed it - OBSOLETE now...
	public void setupNKP(int N, int K, int P){
		
		nkp = new nkpLandscape(N,K,P);
				
		int nkpn = (int) Math.pow(2, N);
		
		//TODO: if N>25, there's too many to evaluate...
		if(N<25){
		
			nkpDone = new boolean[nkpn];
			
			for(int i = 0;i<nkpn;i++)
				nkpDone[i]=false;
		}
		
		// Now set up the fitness landscape 
		if(K<10){
			this.nkpfitfunc = new float[(int)Math.pow(2,K)];
			for(int i = 0;i<nkpfitfunc.length;i++){
				nkpfitfunc[i] = (float) Math.random();
			}
		}
		else{
			System.out.println("K is bigger than 10 - can't initialise");
		}
	}*/

	

	public float[] get1DnkpLandscape() {

		
		float[] landscape = new float[sDim];
		
		for(int i=0;i<sDim;i++){
			float[] g = nkp.intToGenome(i);
			landscape[i] = nkp.getFitness(g);
			System.out.println(""+landscape[i]);
		}
		if (!gotScale) {
			
			setMinMax(landscape);
			
		}
		
		
		return landscape;
	}
	
	
	
	public float[] get1DLandscape(){
		float[] landscape = new float[sDim];
		float[] genome = new float[1];
		
		/** Escape the non-1D landscapes */
		switch(type){
			/** these are all the 2D landscapes */
			case ONEPEAK:
			case THREEHUMPS:
				break;
				
			default:
				System.out.println("Landscape type "+type.name()+" can't be evaluated on 1D!");
				return null;
		}
		for(int x=0;x<sDim;x++){
			genome[0] = (float) x/sDim;		
			switch(type){
				case ONEPEAK:
					landscape[x] = fitnessOnePeak(genome);
					break;
				case THREEHUMPS:
					landscape[x] = fitnessThreeHumps(genome);//(g);
					break;
					
				default:
					System.out.println("Landscape type "+type.name()+" can't be evaluated on 2D!");
					landscape[x] = 0;
					//return null;
			}
		}
		
		/** get the normalisation data */
		if (!gotScale) {
			
			setMinMax(landscape);
			
		}
		
		return landscape;
	}
	
	void setMinMax(float[] landscape){

		minValue = maxValue = landscape[0];
		for (int x = 0; x < sDim; x++) {
			minValue = landscape[x] < minValue ? landscape[x] : minValue;
			maxValue = landscape[x] > maxValue ? landscape[x] : maxValue;
		}
		gotScale = true;
		
		System.out.println("min and max values in landscape are "
				+ minValue + " and " + maxValue);
		
	}
	
	
	
	
	/** We sometimes need to create the whole landscape for two reasons: 
	 * 
	 *  Firstly, we need to get the range of values if we want to rescale the landscape to the algorithmic Range
	 *  Secondly, we need to get the range of values if we want to PLOT the landscape
	 *   
	 *  this is obviously NOT realistic for real GA problems, where we only want to get the values for a particular genome.
	 *  hence we make iterateve calls to particular functions (e.g. sombrerofitness()), so that we use the SAME function to calculate the whole landscape as we 
	 *  do when we are calculating the fitness of a particular genome
	 * @return
	 */
	public float[][] get2DLandscape(){
		float[][] l = new float[sDim][sDim];
		float[] g = new float[2];
	
		switch(type){
			/** these are all the 2D landscapes */
			case SOMBRERO:
			case CONE:
			case RIDGE:
			case THREEHILL:
			case RASTRIGIN:
				break;
				
			default:
				System.out.println("Landscape type "+type.name()+" can't be evaluated on 2D!");
				return null;
		}
		
		
		
		for(int x=0;x<sDim;x++){
			g[0] = (float) x/sDim;
			for(int y=0;y<sDim;y++){
				g[1] = (float) y/sDim;		
				switch(type){
					case SOMBRERO:
						l[x][y] = sombrerofitness(g);
						break;
					case CONE:
						l[x][y] = conefitness(g);
						break;
					case RIDGE:
						l[x][y] = ridgefitness(g);
						break;
					case THREEHILL:
						l[x][y] = threeHillfitness(g);
						break;
					case RASTRIGIN:
						l[x][y] = rastriginFitness(g);
						break;
						
					default:
						System.out.println("Landscape type "+type.name()+" can't be evaluated on 2D!");
						l[x][y] = 0;
						return null;
				}
				
			}
		}
		
		/** get the normalisation data */
		if(!gotScale){
			

			/**/
			//}
			
			minValue = maxValue = l[0][0];
			for(int x=0;x<sDim;x++){
				for(int y=0;y<sDim;y++){
					minValue = l[x][y]<minValue?l[x][y]:minValue;
					maxValue = l[x][y]>maxValue?l[x][y]:maxValue;
				}
			}
			gotScale = true;
	
			System.out.println("min and max values in landscape are "+minValue+" and "+maxValue);

			
			/** We don't normalise to the fitness range anymore
			if(!rescaling){
				float scale = algMax - algMin;
				for(int x=0;x<sDim;x++){
					for(int y=0;y<sDim;y++){
						l[x][y] = normalise(l[x][y], algMin);
						l[x][y] = algMin + (scale * (l[x][y]-minValue) / (maxValue-minValue));
					}
				}
				minValue = algMin;
				maxValue = algMax;
				
			}
			*/
			
		}
		
		return l;
		
	}
	

	float fitnessOnePeak(float[] genome){
		float fmin = (float) 0.8,fmax = (float) 1.2;
		return (float) (fmin + ((1-(2*Math.abs(0.5-genome[0]))) * (fmax-fmin)));
	}
	
	/**#==========================
		#normalisation values for 3 humps:
		def calc3hwave(f):
		    t1 = f - 0.2
		    t2 = f - 0.5
		    t3 = f - 0.8
		    f = exp(-100*t1*t1) + 2*exp(-50*t2*t2) + 3*exp(-50*t3*t3)
		    return f
		
		#==========================
		#normalisation values for 3 humps:
		def set3hmaxmin(frange):
		    frange = calc3hwave(frange)
		    global n3hmax 
		    n3hmax = frange.max()
		    global n3hmin 
		    n3hmin = frange.min()
		    print 'max in 3wave=', n3hmax, ' min = ',n3hmin
		    
		
		
		
		#==========================
		#return fitnesses 
		def fitness_threehumps(CellArray):
		    fmin = 0.8
		    fmax = 1.2
		    #t1 = CellArray - 0.2
		    #t2 = CellArray - 0.5
		    #t3 = CellArray - 0.8
		    #f = exp(-100*t1*t1) + 2*exp(-50*t2*t2) + 3*exp(-50*t3*t3)
		    #print 'min f is ',f.min(), ' max f is ',f.max()
		    
		    f =  calc3hwave(CellArray)
		    f = (f - n3hmin)/(n3hmax-n3hmin)
		    
		    return fmin + ((fmax-fmin)*f)
	*/
	

	float fitnessThreeHumps(float[] genome){
		/** NB: no need to normalise this now - we do it in the engine */
	    float t1 = (float) (genome[0] - 0.2);
	    float t2 = (float) (genome[0] - 0.5);
	    float t3 = (float) (genome[0] - 0.8);
	    float f = (float) 20.0 + (float) (Math.exp(-100*t1*t1) + 2*Math.exp(-50*t2*t2) + 3*Math.exp(-50*t3*t3));
	    return f;
	}
	
	
	
	
	
	
	
	
	/**
	#===============================================
	def fitness(CellArray,Type):
		if Type == fitness_models.ONEPEAK:
			return fitness_onepeak(CellArray)
		elif Type == fitness_models.THREEHUMPS:
			return fitness_threehumps(CellArray)
		print 'Bad choice of fitness function: ', Type
	*/
	public float getFitness(float[] genome) {
		float fitness = 0;
		switch(type){
			case ONEPEAK:
				/**
				def fitness_onepeak(CellArray):
					fmin = 0.8
					fmax = 1.2
					return fmin + ((1-(2*abs(0.5-CellArray))) * (fmax-fmin))
				*/
				fitness = fitnessOnePeak(genome);
				break;
			case THREEHUMPS:
				fitness = fitnessThreeHumps(genome);
				break;
			case SOMBRERO:
				fitness =  sombrerofitness(genome);
				break;
			case CONE:
				fitness =  conefitness(genome);
				break;
			case RIDGE:
				fitness =  ridgefitness(genome);
				break;
			case THREEHILL:
				fitness =  threeHillfitness(genome);
				break;
			case RASTRIGIN:
				fitness =  rastriginFitness(genome);
				break;
			case NKP:
				fitness = nkp.getFitness(genome);
				if(fitness>nkp.fitMax+0.02)
					System.out.println("impossible fitness value!");
				break;
			default:
				fitness=0;
				break;
		}
		
		/*R_TODO: scaling now happens after we get the fitness, need to check that gotScale is set somewhere
		 * 
		 * the reason we DON'T do this when rescaling is as follows - if we aren't rescaling, we assume that the
		 * whole landscape is known and that we've calculated the global scaling values (e.g. in get2Dlandscape), which 
		 * we'll use to make the model work. It saves a lot of calculations if we just rescale the landscape now
		 * 
		 * If we ARE rescaling, then the scaling factors are held in the Engine, not the Landscape, as they are updated at
		 * every iteration of the algorithm, so we shouldn't rescale the raw landscape
		 * 
		 * UPDATE: We have since abandonned rescaling here at all - it was getting too complicated!
		 * 
		 */
		//if(gotScale){
		//	if(!rescaling){
		//		fitness = minValue + ((maxValue-minValue) * (fitness-minValue) / (maxValue-minValue) );
		//	}
		//}
		//else{
		//	System.out.println("Attempting to get fitness when we haven't yet got the scaling factor");
		//}
			
		
		return fitness;
	}

	public int getNdims() {
		switch(type){
			case ONEPEAK:
			case THREEHUMPS:
				return 1;
			case RIDGE:
			case CONE:
			case SOMBRERO:
			case THREEHILL:
			case RASTRIGIN:
				return 2;
			case NKP:
				return nkp.getNDims();
			default:
				System.out.println("Unable to get dimensions for type "+type.name());
				return 0;
		}
	}

	public void setNoRange(boolean b) {
		noRange = b;
	}

	public boolean getNoRange() {
		return noRange;
	}

	//private float normalise(float val, float minRange){
	//	return minRange + ((maxValue-minValue) * (val-minValue) / (maxValue-minValue));
	//}
	/*
	public void setRange(float frMin, float frMax) {
		noRange = false;

		fitRangeMin = frMin;
		fitRangeMax = frMax;
		fitRangeScale = frMax - frMin;
	}
			
	

	public landscapeType getType() {
		return type;
	}

	public float getFitScale() {
		return fitRangeScale;
	}
	*/

	public float getMinValue() {
		return minValue;
	}

	public float getRange() {
		return maxValue-minValue;
	}

	public float getMaxValue() {
		return maxValue;
	}

	/** NDims is the length of the genome. NKPdim is 2^genome length - the number of possible values in the landscape */
	public int getNKPdim() {
		return (int) Math.pow(2, nkp.getNDims());
	}

	public int getGenomeVal(float[] genome) {
		
		int v=0;
		int B = (int) Math.pow(2, genome.length-1);
		for(int i=0;i<genome.length;i++){
			if(genome[i]>0.5){
				v += B;
			}
			B = B/2;
		}
		return v;
	}
	
	public int[] getNKPNodes(){
		return nkp.getNodes();
	}
	
	public int[][] getNKPEdges(){
		int[][] e;
		e = nkp.getEdges();
		return e;
	}
	
    public float[] getNKPScores(){
    	return nkp.getScores();
    }
    
    public float getNKPScoreMax(){
    	return nkp.getScoreMax();
    }
    
    public float getNKPScoreMin(){
    	return nkp.getScoreMin();
    }

	public void findNKPMaxMin() {
		if(nkp != null)
			nkp.findMaxMin();
		else
			System.out.println("nkp is null!");
	}
	
    
    
    
    
	

}

/**
n3hmax = 1.0
n3hmin = 0.0




#===============================================
def fitness_onepeak(CellArray):
    fmin = 0.8
    fmax = 1.2
    
    return fmin + ((1-(2*abs(0.5-CellArray))) * (fmax-fmin))

#===============================================
def fitness(CellArray,Type):
    if Type == fitness_models.ONEPEAK:
        return fitness_onepeak(CellArray)
    elif Type == fitness_models.THREEHUMPS:
        return fitness_threehumps(CellArray)
    print 'Bad choice of fitness function: ', Type


#===============================================
FitMap = zeros(nbin, float)
for i in range(0,nbin):
    FitMap[i] =  1.0 * i/nbin
set3hmaxmin(FitMap)
FitMap = fitness(FitMap,landscape)


*/