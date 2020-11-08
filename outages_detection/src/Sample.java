

public class Sample {
	
	double[] values = new double[18];
	int time;
	//ArrayList<ArrayList<Double>> values = new ArrayList<ArrayList<Double>>();
	
	Sample(int time, double values[]){
		this.time = time;
		this.values = values.clone();
	} // end of constructor
	
	@Override 
    /** Overrides the superclass' toString() method **/
	public String toString() { 
        return String.format("%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t%10.5s\t", this.values[0],this.values[1],this.values[2],this.values[3],this.values[4],this.values[5],this.values[6],this.values[7],this.values[8],this.values[9],this.values[10],this.values[11],this.values[12],this.values[13],this.values[14],this.values[15],this.values[16],this.values[17]);
	}  

} // end of class

/*package assignment2;

public class Sample {
	
	double[][] values = new double[9][3];
	//ArrayList<ArrayList<Double>> values = new ArrayList<ArrayList<Double>>();
	
	Sample(double values[][]){
		this.values = values;
	} // end of constructor

} // end of class*/