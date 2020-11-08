public class ClustSample extends Sample {
	
	double[] distances;
	int clusterNum;
	int time;

	public ClustSample(int time, double[] values, double[] distances, int clusterNum) {
		super(time, values);
		this.distances = distances;
		this.clusterNum = clusterNum;
	}
	
}
