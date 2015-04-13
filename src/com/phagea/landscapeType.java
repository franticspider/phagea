package com.phagea;

public enum landscapeType {
	ONEPEAK,
	THREEHUMPS,
	UNKNOWN, 
	
	SOMBRERO,
	CONE,
	RIDGE,
	THREEHILL, 
	RASTRIGIN,

	NKP;
	
	public static landscapeType parseLandscapeType(String s){
		
		if (s.equals("ONEPEAK"))
			return ONEPEAK;
		
		if (s.equals("THREEHUMPS"))
			return THREEHUMPS;
		
		if (s.equals("SOMBRERO"))
			return SOMBRERO;
		
		if (s.equals("CONE"))
			return CONE;
		
		if (s.equals("RIDGE"))
			return RIDGE;
		
		if (s.equals("THREEHILL"))
			return THREEHILL;
		
		if (s.equals("RASTRIGIN"))
			return RASTRIGIN;
		
		if (s.equals("NKP"))
			return NKP;
		
		if (s.equals("UNKNOWN"))
			return UNKNOWN;
		
		System.out.print("Landscape type <"+s+"> not recognised - check config\n");
		return null;

	}
	
}
