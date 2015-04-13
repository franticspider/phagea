package com.phagea;

public class Agent {
	
	float[]	genome	= null;
	float	fitness	= 0;
	boolean	fitset	= false;
	boolean	hasdied	= false;
	
	public Agent(float[] genome){
		this.genome = genome;
	}
	
	public Agent getCopy(){
		Agent copy = new Agent(this.genome);
		return copy;
	}
	
}

