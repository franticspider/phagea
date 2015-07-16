package com.phagea;

/** we'll need these if we export to file
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
*/

public class phageaStats {
	
	final int	nbin;
	float[] fnbin = null;
	int[][] cellHist = null;
	int[][] phageHist = null;
	float[][] cpRatioHist = null; 
	
	int[] cellPopDy = null;
	int[] phagePopDy = null;
	float[] rDy = null;
	float[] fMax = null;
	float[] fMin = null;
	float[] modalGene = null;
	public int	modalGeneIndex;
	
	float[] obsMax = null;
	float[] obsMin = null;
	
	/** Stats on diversity */
	int[] nUnique = null;
	float[] diversity = null;
	
	
	final int T;
	
	/**
	 # for heat plot 
	 nbin = 100 
	 fnbin = float(nbin) 
	 CellHist = zeros([nbin,T],int) 
	 PhageHist = zeros([nbin,T], int) 
	 cpratioHist = zeros([nbin,T],float)
	# for the popdy plot
	cellpopdy = zeros(T)
	phagepopdy = zeros(T)
	rdy = zeros(T)
	fmax = zeros(T)
	fmin = zeros(T)
	 */
	public phageaStats(int nbin, int T, int ndim) {
		this.T = T;
		
		//TODO: pass this in as a parameter
		this.nbin = nbin;
		if(nbin>0){
			switch(ndim){
				case 1:
					cellHist = new int[nbin][T];
					phageHist = new int[nbin][T];
					break;
				case 2:
					cellHist = new int[nbin][nbin];
					phageHist = new int[nbin][nbin];
					break;
				default:
					//TODO: this is currently only the nkp landscape - check it works for others!
					/** nbin will have to be a different value - e.g. in nkp, nbin is 2^N */
					cellHist = new int[nbin][1];
					phageHist = new int[nbin][1];
						
			}

			modalGene = new float[ndim];
			fnbin = new float[nbin];
			cpRatioHist = new float[nbin][T];
		}
		cellPopDy = new int[T];
		phagePopDy = new int[T];
		rDy = new float[T];
		fMax = new float[T];
		fMin = new float[T];
		
		nUnique = new int[T];
		diversity = new float[T];
	}
	
	public void setupRescaleRecs(){
		obsMax = new float[T];
		obsMin = new float[T];
	}
	

	
	
	
	/**TODO: not sure why this is commented out - is it elsewhere??
	void ExportToFiles(String froot){
		//1: create a popdy with host,phage and res
		FileOutputStream fop = null;
		File file;
 
		file = new File(froot + "pd000.dat");
		try {
			fop = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// if file doesnt exists, then create it
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}*/


	public float[] getModalGene() {
		// TODO Auto-generated method stub
		return modalGene;
	}

	public void rezero(int ndim) {
		for(int t =0;t<T;t++){
			switch(ndim){
			case 1:
				break;
			case 2:
				break;
			}
		}
	}
	
	public float meanCellPop(final int minT){
		float mmean=0;
		float tot=0;
		int count=0;
		
		if(minT>=cellPopDy.length){
			//TODO: flag an error
			System.out.println("ERROR: No events after time "+minT+" returning -2");
			return -2;
		}
		
		for(int i=minT; i<cellPopDy.length; i++){
			tot += cellPopDy[i];
			count++;
		}
		
		if(count<1){
			//TODO: flag an error
			return -1;
		}
		
		mmean = tot/count;
		return mmean;
	}
	
	
	
	
	
	
	
}


/**
#---------------------------------------------------
#Plot some of the parameters for visual testing...
#plt.figure(1)
#plt.subplot(111)
#plt.plot(FitMap,xx,label = "fitness")
#plt.subplot(121)
#for i in range(1,10):
#    BreedMap = FitMap *  rho #* (1.0 - float(KK*0.1*i)/float(KK))
#    msg = 'KK *', (i/10) 
#    plt.plot(BreedMap,label = msg)#, origin = 'left' ,aspect='auto', label='cell')    
#plt.show()
*/

