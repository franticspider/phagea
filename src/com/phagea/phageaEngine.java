package com.phagea;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.Random;

//import javax.swing.text.html.HTMLDocument.Iterator;

public class phageaEngine {
	
	List<Agent>		cells		= new ArrayList<Agent>();
	List<Agent>		phage		= new ArrayList<Agent>();
	
	// Variables for rand number gen
	private Random	fRandom		= new Random();
	
	phageaConfig	cfg			= null;
	phageaLandscape	landscape	= null;
	phageaStats		stats 		= null;
	
	float			resource;
	
	int 			replencount = 0;
	
	/** Observed fitness value stats */
	public float 	obsMin;
	public float 	obsMax;
	public float 	obsLim = (float) 0.0000001; // The smallest observable range (in case obsMin=obsMax)
	public float	obsRange;

	/** fitness value stats per generation */
	public float 	genMin;
	public float	genMax;
	
	private float	phageMax;
	private float	phageMin;

	//Default reporting is verbose
	public boolean  verbose = true;
	
	public phageaEngine(phageaConfig config, phageaLandscape landscape){
		
		this.cfg = config;
		resource = cfg.getR0();
		this.landscape = landscape;
		init();
	}
	
	private double getGaussian(double aMean, double aVariance) {
		return aMean + fRandom.nextGaussian() * aVariance;
	}

	/**
	#===============================================
	#return array of n zero-mean, mutsigma std dev normally distributed numbers
	def mutation(n):
    return random.normal(0. , mutsigma, n)
    */	
	public float mutation(float value) {// , float mean, float variance) {

		if(cfg.type == landscapeType.NKP){
			//TODO: check out whether we are using mutsigma correckly here
			if(Math.random()<cfg.mutsigma)
				if(value<0.5)
					return 1;
				else
					return 0;
			else
				return value;
		}
		else{
			/** return random.normal(0. , mutsigma, n) */
			float v = (float) getGaussian(0, cfg.mutsigma);
			
			value += v;
			return value;
		}
	}
	

	/** #===============================================
	#return set of mutated cells 
	def mutCell(CellArray):
	    n = CellArray.size
	    new = CellArray + mutation(n)
	    new = minimum(new, ones( (n) ) )
	    new = maximum(new, zeros( (n) ) )
	    return new
	*/
	float[] mutateGenome(float[] genome){
		int n = genome.length;
		float[] na = new float[n];
		for(int i=0;i<n;i++){
			//TODO: We'll have to make this a switch between "binary" and floating point genomes...
			if(cfg.type == landscapeType.NKP){
				na[i] = mutation(genome[i]);
			}
			else{
				na[i] = mutation(genome[i]);
				//TODO: need to check if this still applies for all the landscapes we have...
				na[i] = na[i]>1?1:na[i];
				na[i] = na[i]<0?0:na[i];
			}
		}
		return na;
	}

	/**
	#===============================================
	#return set of b mutated phages v
	def mutPhage(b,v):
	return mutCell(ones( (b) ) * v)
	 */
	float[] mutPhage(int N, float parent){
		float[] kids = new float[N];

		for(int i=0;i<N;i++){
			kids[i] = parent;
		}
		
		mutateGenome(kids);
		return kids;
	}
	
	
	/** We scale the fitness when we work how to divide
	 *  GammaFunction should call scaleFitness when the fitness evaluation is used
	 *
	float scaleFitness(float fitness){
		
		if(landscape.getNoRange()){
			return (cfg.algMin + cfg.algMax)/2;
		}
		else{
			return  cfg.algMin + (cfg.algScale * (fitness-landscape.getMinValue())/landscape.getRange());
		}
		
	}
	*/
	
	
	
	
	/**
	#===============================================
	delta_i : array of cell fitnesses
	def gammafunction(N_i,delta_i,Resource,gamma,RK):
	    gam = (gamma * Resource * delta_i)/(Resource + RK)
	    return gam 
	*/
	float GammaFunction(Agent cell){
		float gamf = (cfg.getrGamma() * resource * scaledFitness(cell.fitness)/*cell.fitness*/)/(resource + cfg.getrK() );
		//float gamf = (cfg.rGamma * resource * scaleFitness(cell.fitness))/(resource + cfg.rK);
		return gamf;
	}
	
	
	private float scaledFitness(float fitness) {
		//R_TODO: This is where we make use of the observed fitness range to scale the fitness value to fit

		if(Math.abs(obsMax-obsMin)<0.000001){//TODO: make this a constant
			return (cfg.algMin + cfg.algMax)/2;
		}
		else{
			//return  cfg.algMin + (cfg.algScale * (fitness-landscape.getMinValue())/landscape.getRange());
		
		
			/** normalise the fitness to the observed range: */
			float f = (fitness - obsMin)/(obsRange);
			
			return cfg.algMin + (f*(cfg.algRange));
		}
		
		
	}

	float [] initGenome(){
		float[] init = new float[landscape.getNdims()];
		for(int j=0;j<init.length;j++){
			if(cfg.randinit)
				if(cfg.type == landscapeType.NKP){
					if(Math.random()<0.5)
						init[j] = 0;
					else
						init[j] = 1;
				}
				else{
					init[j] = (float) Math.random();
				}
			else
				if(cfg.type == landscapeType.NKP){
					//Go for an alternating pattern of zeros and ones:
					if(j%2 == 0 )
						init[j] = 0;
					else
						init[j] = 1;
				}
				else{
					init[j] = (float) cfg.getInitFitVal();
				}
		}
		return init;
	}
	
	
	
	/**
	#================================================ 
	#init
	#Susan's values
	initfit = 0.2
	#Cell = ones( (KK))*initfit
	#Phage = ones( (2) ) *(initfit)#(8.1e4) ) *initfit

	#initfit = 0.1
	Cell = ones( (1000))*initfit
	Phage = empty(0)
	#Phage = ones((1000))*initfit#empty(0) #ones( (200*KK) ) *(initfit)#(8.1e4) ) *initfit4.6e3) ) * initfit
	*/
	void init(){
		
		//_TODO: we should make this work for N dimensional genome
		
		for(int i=0;i<cfg.startCellCount;i++){

			float [] initGene = null;
			initGene = initGenome();
			
			Agent c = new Agent(initGene);
			cells.add(c);
		}
				
		for(int i=0;i<cfg.startPhageCount;i++){

			float [] initGene = null;
			initGene = initGenome();
			
			Agent p = new Agent(initGene);
			phage.add(p);
		}
		
		landscape.getFitness(cells);
		
		if(cfg.type == landscapeType.NKP)
			//TODO: Have to be careful where N is > 12 - can't allocate memory for all the stats we'd do for a known landscape
			if(cfg.nkpN<14)
				stats = new phageaStats(landscape.getNKPdim(),cfg.T,landscape.getNdims());
			else
				stats = new phageaStats(-1,cfg.T,landscape.getNdims());
		else
			stats = new phageaStats(cfg.nbin,cfg.T,landscape.getNdims());
	}



	/**
	#===============================================
	# main algorithm 
	 */
	public void runAlgorithm(){
		/**
		 #-----------------------------------------------
		 # calculate initial resource concentration
		 Fit = fitness(Cell,landscape)
		                            #gammafunction(N_i,delta_i,Resource,gamma,RK):
		 Resource = R0 - (Repsilon*sum(gammafunction(Cell, Fit, Resource,Rgamma,RK)))
		 print 'Resource = ',Resource
		*/
		landscape.getFitness(cells);
		
		//R_TODO: Update the fitness range if rescaling
		if (cfg.rescaling){
			setFitRange();
			stats.setupRescaleRecs();
		}
		else{
			obsMin = landscape.getMinValue();//cfg.algMin;
			obsMax = landscape.getMaxValue();
			obsRange = obsMax-obsMin;
		}
		
		updateResource(); 
		
		for(int t=0; t<cfg.T; t++){			
			lysis();
			washout();
			divideCells();

			if(cells.size()>0){
				updateFitRange();
			}
			updateResource();
			update_stats(t);
			if(verbose)
				print_stats(t);
			if(cfg.replenish && phage.size() == 0){
				replenishPhage();
				replencount++;
			}
		}
		
		//set the zero values in the stats to -1/8th the range
		stats.rezero(landscape.getNdims());
		
	}



	/*R_TODO: When rescaling, this function is *supposed* to reset the fitness range,
	 * but 
			landscape.calculateFitRange(cells);
	 * appears to do the same job!!
	 * 
	 * This should update the range of *observed* values, and should use phageaEngine class variables to store them
	 * 
	 * 
	 */
	private void updateFitRange() {


		genMin = genMax = cells.get(0).fitness;
		for (int i = 0; i < cells.size(); i++) {
			float f = cells.get(i).fitness;

			genMin = genMin < f ? genMin : f;
			genMax = genMax > f ? genMax : f;
			if(cfg.rescaling){
				obsMin = obsMin < f ? obsMin : f;
				obsMax = obsMax > f ? obsMax : f;
			}
		}
		if(cfg.rescaling){
			if(Math.abs(obsMax-obsMin)<obsLim){
				obsRange = obsLim;
				
			}
			else{
				obsRange = obsMax - obsMin;
			}
		}

		//TODO: This should be in print_stats..
		if(verbose)
			System.out.println("frMin = "+obsMin+",\t frMax = "+obsMax);
		
	}


	private void setFitRange(){
		if(cfg.randinit){
			boolean started = false;
			for(Agent cell:cells){
				float f = landscape.getFitness(cell.genome);
				if(!started){
					obsMin = obsMax = f;
					started = true;
				}
				else{
					obsMin = f<obsMin?f:obsMin;
					obsMax = f>obsMax?f:obsMax;
				}
				
			}
		}
		else{
			float f = landscape.getFitness(cells.get(0).genome);
			obsMin = obsMax = f;
		}
		updateFitRange();
	}
	
	
	
	
	private void replenishPhage() {
		float[] gene = stats.getModalGene();

		for(int i=0;i<cfg.startPhageCount;i++){
			Agent p = new Agent(gene);
			phage.add(p);
		}
		
	}

	private void updateUniqueCells(Agent c, List<Agent> unique){
		boolean found = false;
		
		int i= 0,match=0;

		for(Agent u:unique){
			boolean gm = true;
			for(int j=0;j<u.genome.length;j++){
				if(Math.abs(c.genome[j]-u.genome[j])> 0.5){
					gm = false; 
					break;
				}
			}
			if(gm){
				match = i;
				found = true;
				break;
			}
			
			i++;
		}
		if(!found){
			Agent b = c.getCopy();
			b.fitness = 1;
			unique.add(b);
		}
			
		else{
			Agent u = unique.get(match);
			u.fitness += 1;
		}
	}
	
	private void update_stats(int t) {
		stats.cellPopDy[t] = cells.size();
		stats.phagePopDy[t] = phage.size();
		stats.rDy[t] = resource;
		
		List<Agent>		uniqueCells = null;
		
		int [][] mf = null;
		if(stats.cellHist != null)
			mf = new int[stats.cellHist.length][stats.cellHist[0].length];
		else{
			uniqueCells = new ArrayList<Agent>();
		}
		
		float fmax=0,fmin=0;
		if(cells.size() > 0){
			fmax=fmin= cells.get(0).fitness;
		}
			
		for(Agent c:cells){
			int ii = (int) ((c.genome[0] * cfg.nbin)-0.5);
			if(ii<0)
				System.out.println("Array index is -1 for cells");
			else
				switch(landscape.getNdims()){
					case 1: 
						stats.cellHist[ii][t]++;
						break;
					case 2:
						int x = (int) ((c.genome[0] * cfg.nbin)-0.5);
						int y = (int) ((c.genome[1] * cfg.nbin)-0.5);
						stats.cellHist[x][y] = t;
						mf[x][y]++;
						break;
					default:
						if(stats.cellHist != null){
							//TODO: this needs to work for NKP only...	
							int gval = landscape.getGenomeVal(c.genome);
							stats.cellHist[gval][0]=t;
						}
						else{
							updateUniqueCells(c,uniqueCells);
						}
						break;
							
				}
			fmin = fmin<c.fitness?fmin:c.fitness;
			fmax = fmax>c.fitness?fmax:c.fitness;
		}
		
		if(cfg.rescaling){
			stats.obsMin[t] = obsMin;
			stats.obsMax[t] = obsMax;
			stats.fMin[t] = fmin;
			stats.fMax[t] = fmax;
		}
		

		stats.modalGeneIndex = 0;
		
		switch(landscape.getNdims()){
		//TODO: case 1 is a copy of case 2 with one less dim on stats.modalGene[0] - there may be other problems to deal with 
			case 1: 
				if ( mf !=null) {
					int cmax = 0, mx = 0, my = 0;
					for (int i = 0; i < stats.cellHist.length; i++) {
						for (int j = 0; j < stats.cellHist[0].length; j++) {
							if (mf[i][j] > cmax) {
								cmax = mf[i][j];
								mx = i;
								my = j;
							}
						}
					}
					stats.modalGene[0] = (float) ((float) mx / cfg.nbin);
					//TODO: This should be in print_stats..
					if(verbose)
						System.out.println("Modal gene is "+stats.modalGene[0]);
				}
				break;
			case 2:
				if ( mf !=null) {
					int cmax = 0, mx = 0, my = 0;
					for (int i = 0; i < stats.cellHist.length; i++) {
						for (int j = 0; j < stats.cellHist[0].length; j++) {
							if (mf[i][j] > cmax) {
								cmax = mf[i][j];
								mx = i;
								my = j;
							}
						}
					}
					stats.modalGene[0] = (float) ((float) mx / cfg.nbin);
					stats.modalGene[1] = (float) ((float) my / cfg.nbin);
					//TODO: This should be in print_stats..
					if(verbose)
						System.out.println("Modal gene is "+stats.modalGene[0]+","+stats.modalGene[1]);
				}
				break;
			default:
				if(stats.cellHist == null){
					//use the uniqueCells array
					boolean started = false;
					float maxct = 0;
					int i=0;
					for(Agent u:uniqueCells){
						if(!started){
							maxct = u.fitness;
							stats.modalGeneIndex = i; 
							started = true;
						}
						else{
							if(maxct<u.fitness){
								maxct = u.fitness;
								stats.modalGeneIndex = i; 
							}
						}
						i++;
					}
					//TODO: This should be in print_stats..
					if(verbose){
						System.out.print("Modal gene is "+stats.modalGeneIndex+" from "+uniqueCells.size()+" unique \n" );
					}
					if(uniqueCells.size() > 0)
						stats.modalGene = uniqueCells.get(stats.modalGeneIndex).genome;
					else{
						//TODO: Think what to do with the modal gene if there are no cells left...
					}
					stats.nUnique[t] = uniqueCells.size();
					//TODO: do this properly stats.diversity[t] = calcDiversity();
						
				}
		}
		
		for(Agent p:phage){
			int ii = (int) ((p.genome[0] * cfg.nbin)-0.5);
			if(ii<0){
				//TODO: This should be in print_stats..
				if(verbose)
					System.out.println("Array index is -1 for phage");
			}
			else
				switch(landscape.getNdims()){
					case 1:
						stats.phageHist[ii][t]++;
						break;
					case 2:
						int x = (int) ((p.genome[0] * cfg.nbin)-0.5);
						int y = (int) ((p.genome[1] * cfg.nbin)-0.5);
						if(x<0 || y<0)
							System.out.println("Array index is "+x+","+y+" for phage");
						stats.phageHist[x][y] = t;
						break;
					default:
						if(stats.phageHist != null){
							//TODO: this needs to work for NKP only...	
							int gval = landscape.getGenomeVal(p.genome);
							stats.phageHist[gval][0]=t;
						}
						break;
				}
		}
		
		
	}


	
	
	private float calcDiversity() {
		
		int[] diffct = new int[cells.get(0).genome.length];
		
		//first, count all the ones at each locus in a population:

		for(Agent c:cells){
			for(int i=0;i<c.genome.length;i++)
				if(c.genome[i] > 0.5)
					diffct[i]++;
		}
		
		return 0;
	}

	/**
	#================================================ 
	#report init
	print 'cells: ', Cell.size, '  phages: ', Phage.size
	print 'least fit cell: ', (fitness(Cell,landscape)).min(), 'fittest cell: ', (fitness(Cell,landscape)).max()
	print 'min genotype: ', Cell.min(), ' max genotype: ', Cell.max()
	if Phage.size>0:
	    print 'least fit phage:', (fitness(Phage,landscape)).min(), 'fittest phage:', (fitness(Phage,landscape)).max()
	else:
	    print 'no phage!'
	 * @param t 
	*/
	public void print_stats(int t){
		System.out.print("\n\nTime "+t+"\n");
		System.out.print("Cells: "+cells.size()+", Phage: "+phage.size()+"\n");
		
		/*R_TODO: when rescaling, we need to do this every time, but when NOT rescaling, this isn't necessary
		 * Need to develop a consistent way of reporting this..
		 */
		//if(cfg.rescaling)
		//	landscape.calculateFitRange(cells);
		
		System.out.print("current L range: "+landscape.getMinValue()              +" ...to: "+landscape.getMaxValue()+"\n");
		System.out.print("current F range: "+obsMin              +" ...to: "+ obsMax +"\n");
		System.out.print("scaled         : "+scaledFitness(landscape.getMinValue())+" ...to: "+scaledFitness(landscape.getMaxValue())+"\n\n");
		System.out.print("host fit range : "+genMin+" ...to: "+genMax+"\n");
		System.out.print("scaled.........: "+scaledFitness(genMin)+" ...to: "+scaledFitness(genMax)      +"\n");
		
		if(phage.size()>0){
			//landscape.calculateFitRange(phage);//This doesn't work for phage because fitness is not calculated!
			/*TODO: We can only *estimate* the fitness of phage by looking at those that have successfully predated host - but this 
			 * does give us an idea of where the population has been...
			 */
			getPhageMaxMin();
			System.out.print("least fit phage: "+phageMin+" fittest phage: "+phageMax+"\n");
		}
		else{
			System.out.print("No phage left\n");
		}
		System.out.print("Resource = "+resource+"\n");
	}
	
	
	
	private void getPhageMaxMin() {
		boolean found=false;
		
		for(Agent p:phage){
			if(p.fitset){
				if(!found){
					phageMin = phageMax = p.fitness;
					found = true;
				}
				else{
					phageMin = phageMin < p.fitness ? phageMin: p.fitness;
					phageMax = phageMax > p.fitness ? phageMax: p.fitness;
				}
			}
		}	
	}

	/**
	 #-----------------------------------------------
	 # cell lysis, based on phage affinity
	 nCell = Cell.size
	
	 if nCell > 0 :
		 nPhage = Phage.size
		 n = int(Theta * nCell * nPhage)
		 print 'n = ', n
		 
		 bCell = Cell < 2  # array of True values (ones not lysed)
		 bPhage = Phage < 2
		 newPhage = array([])
		 
		 for i in range(n):
	         if nCell>1:
	             ci = random.randint(0,nCell-1)      # random cell index
	         else:
	             ci = 0          
	         c = Cell[ci]                        # cell genome
	         vi = random.randint(0,nPhage-1)
	         v = Phage[vi]        
	         p = exp(-(v-c)*(v-c)/sigma2)        # probability
	         if random.random() < p:
	             bCell[ci] = False               # mark for deletion
	             bPhage[vi] = False
	             newPhage = hstack([newPhage,mutPhage(b,v)]) # build up mutant phage array
	     Cell = Cell[bCell]                      # delete marked cells
	     Phage = hstack([Phage[bPhage],newPhage])    #print newPhage
	     print 'cells lysed:  ', nCell - Cell.size, '  phages made: ', Phage.size - nPhage
	*/
	private void lysis() {
		int nPhage = phage.size();
		int nCells = cells.size();
		if(!cells.isEmpty()){
			int n = (int) (cfg.getTheta() * cells.size() * phage.size());
			List<Agent> newPhage = new ArrayList<Agent>();
			
			for(int i=0;i<n;i++){
				int ci;
				if(cells.size()>1){
					ci= (int) (Math.random()*(cells.size()-1));
				}
				else{
					ci=0;
				}
				Agent c = cells.get(ci);
				int vi = (int) (Math.random()*(phage.size()-1));
				Agent v = phage.get(vi);
				float p = affinity(c,v);
				
				/**
				if random.random() < p:
					bCell[ci] = False               # mark for deletion
					bPhage[vi] = False
					newPhage = hstack([newPhage,mutPhage(b,v)]) # build up mutant phage array
				*/
				if(Math.random() < p){
					c.hasdied = true;
					v.hasdied = true;
					/**newPhage = hstack([newPhage,mutPhage(b,v)]) # build up mutant phage array*/
					newPhage.addAll(mutPhage(v));
					
				}
			}
			
			/**
			 * Cell = Cell[bCell] # delete marked cells Phage =
			 * hstack([Phage[bPhage],newPhage]) #print newPhage print 'cells
			 * lysed: ', nCell - Cell.size, ' phages made: ', Phage.size -
			 * nPhage
			 */
			
			removeDead();
			phage.addAll(newPhage);
			
			//TODO: This should be in print_stats..
			if(verbose)
				System.out.println("Cells lysed: "+(nCells - cells.size())+" Phage made: "+ (phage.size()-nPhage));
		}
	}


	/**
	#-----------------------------------------------
	# wash out proportion of cell and phage contents
	nCell = Cell.size
	nPhage = Phage.size
	if nCell > 0:
	    #Cell = Cell[w *( 1 - ((KK - Cell.size)/KK)) < random.random( (Cell.size) )]
	    Cell = Cell[w < random.random( (Cell.size) )]
	
	Phage = Phage[w < random.random( (Phage.size) )]
	print 'cells washed: ', nCell - Cell.size, '  phages washed: ', nPhage - Phage.size
	 */
	private void washout() {
		int nCell = cells.size();
		int nPhage = phage.size();
		if(nCell>0){
			for(Agent c:cells){
				float r = (float) Math.random();
				/** the python way of doing this KEPT cells rather than KILLING them! */
				if(cfg.getTheta() >= r){
					c.hasdied=true;
				}
			}
		}
		for(Agent v:phage){
			float r = (float) Math.random();
			if(cfg.getTheta() >= r)
				v.hasdied=true;
		}
		removeDead();

		//TODO: This should be in print_stats..
		if(verbose)
			System.out.println("Cells washed: "+(nCell-cells.size())+"\tPhage washed: "+(nPhage-phage.size()));
	}
	

	/**
	 * #----------------------------------------------- # cell division and
	 * mutation, based on fitness #Fit = fitness(Cell,landscape) * rho * (1.0 -
	 * Cell.size/float(KK))
	 * 
	 * nCell = Cell.size if nCell > 0: #sort cell array for checking Cell =
	 * sort(Cell)
	 * 
	 * #UNCOMMENT THIS TO USE THE NON-RESOURCE VERSION #Fit =
	 * fitness(Cell,landscape) * rho * (1.0 - Cell.size/float(KK)) #fmax[t] =
	 * Fit.max() #fmin[t] = Fit.min() #print 'Old fit: min = ',Fit.min(),' max =
	 * ',Fit.max()
	 * 
	 * 
	 * # Get the fitness delta of each cell delta = fitness(Cell,landscape)
	 * #GammaTerm = (Rgamma * Resource * Cell * delta)/(Resource + RK)#KK) #def
	 * gammafunction(N_i,delta_i,Resource,gamma,RK): gFit = gammafunction(Cell,
	 * delta, Resource, Rgamma, RK) fmax[t] = gFit.max() fmin[t] = gFit.min()
	 * print 'New fit: min = ',gFit.min(),' max = ',gFit.max()
	 * 
	 * myr = random.random( (Cell.size) )
	 * breed = Cell[myr < Fit] # breed fittest cells
	 * 
	 * #_TODO: try a tournament of the bred cells
	 * 
	 * #for i in range(0,Cell.size): # if myr[i] < Fit[i]: # print 'BRED ',i,
	 * 'cell[i]= ',Cell[i],' fit[i]= ',Fit[i], 'rand = ',myr[i] # else: # print
	 * ' ',i, 'cell[i]= ',Cell[i],' fit[i]= ',Fit[i], 'rand = ',myr[i]
	 * 
	 * 
	 * Cell = hstack([Cell, mutCell(breed)])
	 * 
	 * @return
	 */
	private void divideCells() {
		int nCell = cells.size();
		
		float g,gmin=0,gmax=0;
		boolean first = true;
		
		if (nCell > 0) {
			
			List<Agent> breed = new ArrayList<Agent>();
			
			for (Agent cell : cells) {
				cell.fitness = landscape.getFitness(cell.genome);// *
																// GammaFunction(cell);
				g = GammaFunction(cell);
				if(first){
					gmin=gmax=g;
					first=false;
				}else{
					gmin=g<gmin?g:gmin;
					gmax=g>gmax?g:gmax;
				}
				
				
				if (g > Math.random()) {
					//TODO we aren't using mutsigma to 
					Agent b = new Agent(cell.genome);
					b.genome = mutateGenome(cell.genome);
					b.fitness = landscape.getFitness(b.genome);
					b.fitset = true;
					breed.add(b);
				}
			}
			//TODO: This should be in print_stats..
			if(verbose)
				System.out.print("Bred "+breed.size()+" new cells, gmin = "+gmin+" gmax = "+gmax+"\n");
			cells.addAll(breed);
		}
	}
	
	
	
	private void removeDead() {

		Iterator<Agent> it = cells.iterator();
		while (it.hasNext()) {
			Agent c = it.next();
			if (c.hasdied){
				it.remove();
			}
		}
		
		cells.removeAll(Collections.singleton(null));
		
		it = phage.iterator();
		while (it.hasNext()) {
			Agent v = it.next();
			if (v.hasdied)
				it.remove();
		}
		
		phage.removeAll(Collections.singleton(null));
	}

	private Collection<? extends Agent> mutPhage(Agent v) {

		List<Agent>		lysed		= new ArrayList<Agent>();
		for(int b=0;b<cfg.getBurstSize();b++){
			Agent l = new Agent(v.genome);
			l.genome  = mutateGenome(l.genome);
			lysed.add(l);
		}
		
		return lysed;
	}

	
	private float affinity(Agent c, Agent v) {
		/** This extends the 1D affinity to the dimensionality of the genome. */
		float p = 0;
		for(int e=0;e<c.genome.length;e++){
			//p = exp(-(v-c)*(v-c)/sigma2)        # probability
			float diff = v.genome[e]-c.genome[e];
			p+= diff*diff;
		}
		
		return (float) Math.exp(-p/cfg.sigma2);
	}

	private void updateResource() {
		
		float sum=0;
		
		resource += -(cfg.getrOmega() *(resource - cfg.getR0()));
		
		for(Agent cell:cells){
			sum += GammaFunction(cell);
		}
		
		//resource = cfg.r0 - (cfg.rEpsilon * sum);
		resource += -(cfg.getrEpsilon() * sum);
	}

	public int[][] getCellHist(){
		for(int i=0;i<stats.cellHist.length;i++){
			for(int j=0;j<stats.cellHist[i].length;j++){
				if(stats.cellHist[i][j]<1)
					stats.cellHist[i][j] = -1000;
			}
		}
		return stats.cellHist;
	}
	
	public int[][] getPhageHist(){
		return stats.phageHist;
	}
	
	public int[] getCellPopdy(){
		return stats.cellPopDy;
	}
	
	public int[] getPhagePopdy(){
		return stats.phagePopDy;
	}

	public float[] getFMaxT(){
		return stats.fMax;
	}
	public float[] getFMinT(){
		return stats.fMin;
	}
	public float[] getObsMaxT(){
		return stats.obsMax;
	}
	public float[] getObsMinT(){
		return stats.obsMin;
	}
	
	public float[] getRDy(){
		return stats.rDy;
	}
	
	public int getReplenCount(){
		return replencount;
	}

	public float getObsMin() {
		return obsMin;
	}

	public float getObsMode() {
		float[] gene = stats.getModalGene();
		return landscape.getFitness(gene);
	}

	public float getObsMax() {
		return obsMax;
	}

	public float getGenMin() {
		return genMin;
	}

	public float getGenMax() {
		return genMax;
	}

	public float[] getModalGene() {
		// TODO Auto-generated method stub
		return stats.getModalGene();
	}
	
}




/** IMPORTANT STUFF

#===============================================
# main algorithm


//_TODO: Might need this for plotting the fitness map - but might not!
xx = zeros(nbin)
for i in range (0,nbin):
    xx[i] = i

#-----------------------------------------------
# calculate initial resource concentration
Fit = fitness(Cell,landscape)

                            #gammafunction(N_i,delta_i,Resource,gamma,RK):
Resource = R0 - (Repsilon*sum(gammafunction(Cell, Fit, Resource,Rgamma,RK)))
print 'Resource = ',Resource




#---------------------------------------------------
#Now start the algorithm

for t in range(T): 
    print 't = ', t
    breed = empty(0)
    
    #-----------------------------------------------
    # cell lysis, based on phage affinity
    nCell = Cell.size
    
    if nCell > 0 :
        nPhage = Phage.size
        n = int(Theta * nCell * nPhage)
        print 'n = ', n
        
        bCell = Cell < 2  # array of True values (ones not lysed)
        bPhage = Phage < 2
        newPhage = array([])
        for i in range(n):
            if nCell>1:
                ci = random.randint(0,nCell-1)      # random cell index
            else:
                ci = 0          
            c = Cell[ci]                        # cell genome
            vi = random.randint(0,nPhage-1)
            v = Phage[vi]        
            p = exp(-(v-c)*(v-c)/sigma2)        # probability
            if random.random() < p:
                bCell[ci] = False               # mark for deletion
                bPhage[vi] = False
                newPhage = hstack([newPhage,mutPhage(b,v)]) # build up mutant phage array
        Cell = Cell[bCell]                      # delete marked cells
        Phage = hstack([Phage[bPhage],newPhage])    #print newPhage
        print 'cells lysed:  ', nCell - Cell.size, '  phages made: ', Phage.size - nPhage
    
    #-----------------------------------------------
    # wash out proportion of cell and phage contents
    nCell = Cell.size
    nPhage = Phage.size
    if nCell > 0:
        #Cell = Cell[w *( 1 - ((KK - Cell.size)/KK)) < random.random( (Cell.size) )]
        Cell = Cell[w < random.random( (Cell.size) )]
    
    Phage = Phage[w < random.random( (Phage.size) )]
    print 'cells washed: ', nCell - Cell.size, '  phages washed: ', nPhage - Phage.size

    #-----------------------------------------------
    # cell division and mutation, based on fitness
    #Fit = fitness(Cell,landscape) * rho * (1.0 - Cell.size/float(KK)) 
    
    nCell = Cell.size
    if nCell > 0:
        #sort cell array for checking
        Cell = sort(Cell)
         
        #UNCOMMENT THIS TO USE THE NON-RESOURCE VERSION
        #Fit = fitness(Cell,landscape) * rho * (1.0 - Cell.size/float(KK))
        #fmax[t] = Fit.max()
        #fmin[t] = Fit.min()
        #print 'Old fit: min = ',Fit.min(),' max = ',Fit.max()
        
        
        # Get the fitness delta of each cell 
        delta = fitness(Cell,landscape)
        #GammaTerm = (Rgamma * Resource * Cell * delta)/(Resource + RK)#KK)
        #def gammafunction(N_i,delta_i,Resource,gamma,RK):
        Fit = gammafunction(Cell, delta, Resource, Rgamma, RK)
        fmax[t] = Fit.max()
        fmin[t] = Fit.min()
        print 'New fit: min = ',Fit.min(),' max = ',Fit.max()
        
        #-----------------------------------------------
        # update resource concentration   
        Resource += -(Romega * (Resource - R0))
        nCell = Cell.size
        #NB gammafunction calculated twice here - could use sum(Fit)
        sr = sum(gammafunction(Cell, delta, Resource, Rgamma,RK))
        Resource += - (Repsilon*sr)
        
    
        #Fit = 0.5 + (0.5 * (Fit - Fit.min())/(Fit.max()-Fit.min()))
        
        myr = random.random( (Cell.size) )
        breed = Cell[myr < Fit]        # breed fittest cells
        
        #_TODO: try a tournament of the bred cells
        
        #for i in range(0,Cell.size):
        #    if myr[i] < Fit[i]:
        #        print 'BRED ',i, 'cell[i]= ',Cell[i],' fit[i]= ',Fit[i], 'rand = ',myr[i]
        #    else:    
        #        print '     ',i, 'cell[i]= ',Cell[i],' fit[i]= ',Fit[i], 'rand = ',myr[i]
        
            
        Cell = hstack([Cell, mutCell(breed)])
    
    #plt.figure(1)
    #plt.subplot(121)
    #plt.plot(Cell,Fit,label = msg)#, origin = 'left' ,aspect='auto', label='cell')    
    #plt.scatter(Cell,Fit)
    #plt.subplot(122)
    #plt.scatter(breed,breed)
    #plt.show()
    
    
 
    #-----------------------------------------------
    # Print out some stats:
    
    print 'Resource = ',Resource
    if breed.size > 0:
        print 'cells bred: ', breed.size#, Cell
    else:
        print 'cells bred: 0'#, Cell
    
    print 'cells: ', Cell.size, '  phages: ', Phage.size
    if Cell.size >0:
        print 'least fit cell: ', (fitness(Cell,landscape)).min(), 'fittest cell: ', (fitness(Cell,landscape)).max()
    else:
        print 'no cells left!'
        
    if Phage.size>0:
        print 'least fit phage:', (fitness(Phage,landscape)).min(), 'fittest phage:', (fitness(Phage,landscape)).max()
    else:
        print 'no phage left!'
        
        
        
        
    #-----------------------------------------------
    # statistics for plot
    for i in range(nbin):
        CellHist[i,t] = logical_and(i/fnbin < Cell , Cell < (i+1)/fnbin).sum()
        PhageHist[i,t] = logical_and(i/fnbin < Phage , Phage < (i+1)/fnbin).sum()
        
        cr = 0
        if CellHist[i,t] > 0:
            cr = CellHist[i,t] / (CellHist[i,t] + (PhageHist[i,t]/71.))
        else:
            if PhageHist[i,t] > 0:
                cr = 1
                    
        cpratioHist[i,t] = cr
    cellpopdy[t]= nCell
    phagepopdy[t] = nPhage
    rdy[t] = Resource


# plot

plt.figure(2)

pst = 'w=',w,' Rgamma=',Rgamma,' Repsilon=',Repsilon,' theta=',Theta
plt.suptitle(pst)
plt.subplot(241)
plt.imshow(CellHist, origin = 'lower' ,aspect='auto', label='cell')

plt.subplot(242)
plt.imshow(PhageHist, origin = 'lower' ,aspect='auto', label='phage')

plt.subplot(243)
plt.imshow(cpratioHist, origin = 'lower' ,aspect='auto', label='phage/cell ratio')

plt.subplot(244)
plt.plot(FitMap,xx,label = "fitness")

plt.subplot(245)
plt.plot(cellpopdy)

plt.subplot(246)
plt.plot(phagepopdy)

plt.subplot(247)
plt.plot(rdy)
plt.plot(fmax)
plt.plot(fmin)

plt.show()

*/