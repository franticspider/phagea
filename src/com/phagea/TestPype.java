package com.phagea;

public class TestPype {
	
	    private String msg;
	 
	    public TestPype() {
	        msg = "nothing so far...";
	        System.out.println("public constuctor called");
	    }
	 
	    public static void speak(String msg) {
	        System.out.println(msg);
	    }
	 
	    public void setString(String s) {
	        msg = s;
	    }
	 
	    public String getString() {
	        return msg;
	    }
	
}
