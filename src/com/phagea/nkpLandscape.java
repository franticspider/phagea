package com.phagea;

import java.util.Random;

public class nkpLandscape {
	
	private int N,K,
				algK;				/** The algorithm runs such that if K=0, algK = 1 */
	private float P;
	private int[] nodes = null;		/** the integer value of a node in the hypercube */
	private int[][] edges = null;	/** the edges connecting the hypercube */
	
	private float[] scores = null;	/** the NKp fitness score for a particular node */
	float scoreMax,scoreMin;
	public float fitMax,fitMin;//TODO: make sure we aren't repeating things done with scoreMin and Max;
	boolean gotScores = false;
	
	private float[] Kscores = null;
	private int[] pows = null;
	
	String d;
	
	
	public nkpLandscape(int N, int K, float P, int seed){
		this.setN(N);
		this.K = K;
		this.algK = K+1;
		this.P = P;
		init(seed);
	}

	public nkpLandscape(int N, int K, float P){
		this(N,K,P,0);
	}
	
	
	
	
	private void init(int seed) {
		Random myrand = new Random();
		
		myrand.setSeed(seed);//436);
		int klen = (int) Math.pow(2, algK);
		
		if(K > 25)
			System.out.println("Very large K = "+K+" - might not be able to allocate enough memory!");
		
		Kscores = new float[klen];
		pows = new int[algK];
		
		/** Generate the random values */
		for(int i=0;i<klen;i++){
			Kscores[i] = myrand.nextFloat();
		}
		
		/** Now flatten them with probability P */
		for(int i=0;i<klen;i++){
			if(P>myrand.nextFloat())
				Kscores[i] =0;
		}
		
		
		
		/** create a simple means to generate the index */
		for(int i=0;i<algK;i++){
			if(i==0)
				pows[algK-i-1]=1;
			else
				pows[algK-i-1]=2*pows[algK-i];
		}
	}

	float getFitness(float bits[]){
		
		if(bits.length != getN()){
			System.out.println("Trying to get the fitness of a genome not of length N="+getN());
			return 0;
		}
		
		float ff = 0;
		for(int n = 0;n<bits.length;n++){
			int kidx = 0;
			for(int k=0;k<algK;k++){
				int pos = k+n;
				pos = pos<N?pos:pos-N;
				if(bits[pos]>=0.5)
					kidx += pows[k];
			}
			ff += Kscores[kidx];
		}
		
		return ff;
	}

	public int[] getNodes() {
		if(nodes == null){
			nodes = new int[(int) Math.pow(2, getN())];
			for(int i=0;i<nodes.length;i++)
				nodes[i] = i;
		}
		return nodes;
	}

	public int[][] getEdges() {
		if (edges == null) {
			edges = new int[nodes.length][nodes.length];
			for (int i = 0; i < nodes.length; i++) {
				char[] s1 = nString(i);
				for (int j = i; j < nodes.length; j++) {
					char[] s2 = nString(j);
					int d = 0;
					for (int p = 0; p < s1.length; p++) {
						if (s1[p] != s2[p]) {
							d++;
						}
						if (d > 1)
							break;
					}
					if (d == 1)
						edges[i][j] = 1;
				}
			}
		}
		return edges;
	}

	private char[] nString(int i) {
		
		String ib = Integer.toBinaryString(i);
		String out = new String(new char[getN()]).replace('\0', '0');
		
		char[] bb = out.toCharArray();
		char[] bi = ib.toCharArray();
		
		int offset = getN()-ib.length();
		for(int j = 0;j<bi.length;j++){
			bb[offset+j]=bi[j];
		}
		
		return bb;
	}

	public float[] getScores() {
		if (scores == null) {
			scores = new float[nodes.length];
			for (int i = 0; i < nodes.length; i++) {
				float[] genome = intToGenome(i);
				
				
				float s = scores[i]=getFitness(genome);
				if(i==0){
					scoreMax = scoreMin = s;
				}
				else{
					scoreMax = s>scoreMax?s:scoreMax;
					scoreMin = s<scoreMin?s:scoreMin;
				}
			}
			gotScores = true;
		}
		return scores;
	}
	
	public float getScoreMax(){
		return scoreMax;
	}
	
	public float getScoreMin(){
		return scoreMin;
	}
	

	public void print() {
		for(int i=0;i<nodes.length;i++){
			System.out.print(""+nodes[i]+"\t");
			System.out.print(""+scores[i]+"\t");
			for(int j=0;j<nodes.length;j++){
				if(edges[i][j]==1)
					System.out.print("1");
				else
					System.out.print(".");
			}
			System.out.print("\n");
		}
		
	}

	public int getNDims() {
		// TODO Auto-generated method stub
		return getN();
	}

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}

	public float[] intToGenome(int ii) {
		
		long i = (long) Math.pow(2,N-1);
		float[] g = new float[N];
		
		for(int n = 0; n<N; n++){
			
			long d = ii/i;
			if(d>0){
				g[n] = 1;
				ii -= i;
			}
			i/=2;
		}
		return g;
	}

	public void findMaxMin() {
		long end = (long) Math.pow(2, N)-1;
		
		 
		float fmax = 0,fmin = 0;
		boolean found = false;
		
		for(int i=0;i<=end;i++){
			float[] genome = intToGenome(i);
			float fit = getFitness(genome);
			if(!found){
				fmax=fmin=fit;
				found = true;
			}
			else{
				fmax=fmax>fit?fmax:fit;
				fmin=fmin<fit?fmin:fit;
			}
		}
		fitMax = fmax;
		fitMin = fmin;
		
	}

	
}
