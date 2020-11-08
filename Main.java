package assignment2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Main {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		int K=4;
		ArrayList<Sample> samples = SQLdatabase.getSamples("subtables", "root", "root", "measurements");
		ArrayList<Sample> samplesTest = SQLdatabase.getSamples("subtables", "root", "root", "analog_values");
		ArrayList<ClustSample> samplesClustered = new ArrayList<ClustSample>(); // <- we will store clustered (using kMean) samples here
		ArrayList<ClustSample> samplesTestClustered = new ArrayList<ClustSample>(); // <- we will store clustered  (using kMean) test-samples here
		ArrayList<ClustSample> samplesTestClusteredKNN = new ArrayList<ClustSample>(); // <- we will store clustered (using kNN) test-samples here
		ArrayList<Graph> graphskMean = new ArrayList<Graph>(); //kMean graphs array
		ArrayList<Graph> graphsTest = new ArrayList<Graph>(); //kMean graphs array
		String[] clusterNames = new String[K+1]; // <- Array containing the name of each cluster for k-Means algorithm
//		String[] clusterNamesKNN = new String[K+1]; // <- Array containing the name of each cluster for kNN algorithm
		int parameters = samples.get(0).values.length; // <- get the number of parameters (18 for the assignment 2)
		int iterations = 100; // <- adjust accordingly, probability of finding good clusters is 0,93 , meaning that for 10 iterations, less than 1 will be wrong

		// get the best-fit centroids
		// Getting best-fit centroids must be outside of the functions ('clusterSamples') to ensure both samples and samplesTest are clustered using the same nomenclature (otherwise we cannot test the kNN algorithm)
		//System.out.println("GETTING THE BEST-FIT CENTROIDS...");
		double[][] centroidsBest = getBestFitCentroids(iterations, K, parameters, samples, samplesClustered);
		//System.out.println("BEST-FIT CENTROIDS FOUND!");

		// cluster samples using kMean algorithm
		boolean letsPlot = false;
		samplesClustered = clusterSamples(iterations,K,parameters,centroidsBest,samples,samplesClustered,letsPlot);
		graphskMean = plot(iterations,K,parameters,centroidsBest,samples,samplesClustered,letsPlot);

		// cluster Test samples using kMean algorithm (just for testing if kNN works fine)
		letsPlot = false;
		samplesTestClustered = clusterSamples(iterations,K,parameters,centroidsBest,samplesTest,samplesTestClustered,letsPlot);
		

		// cluster Test samples using kNN
		int kNearest = 5; // <- check which one is the best
		ArrayList<Result> resultList = new ArrayList<Result>();
		samplesTestClusteredKNN = kNNGetClusters(K,kNearest,samplesTest,samplesClustered,samplesTestClusteredKNN,resultList);

		graphsTest = plot(iterations,K,parameters,centroidsBest,samplesTest,samplesTestClusteredKNN,letsPlot);
		
		// test if kNN has clustered test-samples properly
		testkNN(samplesTestClustered,samplesTestClusteredKNN);
		
		// print cluster names
		assignClusterNames(clusterNames, centroidsBest);
		String[] colorNames = new String[K];
			colorNames[0] = "Blue";
			colorNames[1] = "Red";
			colorNames[2] = "Yellow";
			colorNames[3] = "Green";
		
		clusterNames[K]="";
		for (int i = 0; i < K; i++) {
			clusterNames[K] = clusterNames[K] + colorNames[i] + " cluster corresponds to \"" + clusterNames[i] + "\"\n\n";
			System.out.println(colorNames[i] + " cluster corresponds to \"" + clusterNames[i] + "\"");
		}
		System.out.println("\n");
		
		GUI gui = new GUI(graphskMean, graphsTest, clusterNames[K]);
		gui.setVisible(true);
		gui.move(1200, 0);
		gui.setSize(gui.getWidth(), 500);
		
		for (int i=0; i<samplesClustered.size(); i++) {
			samplesClustered.get(i).time = i+1;
		}
		for (int i=0; i<samplesTestClustered.size(); i++) {
			samplesTestClustered.get(i).time = i+1;
		}
		
		for (ClustSample cs: samplesClustered) {
			System.out.println("Sample " + cs.time + " clustered as " + clusterNames[cs.clusterNum]);
		}
		System.out.println();
		for (ClustSample cst: samplesTestClustered) {
			System.out.println("Test Sample " + cst.time + " clustered as " + clusterNames[cst.clusterNum]);
		}
		

	} // end of main

	// Function to create a centroids using random existing samples (probably the best approach!)
	public static double[][] createRandomCentroids(ArrayList<Sample> samples, int K) {
		Random r = new Random();
		ArrayList<Sample> samp= new ArrayList<Sample>(samples);
		int bound, randomNum;
		double[][] centroids= new double[K][samples.get(0).values.length];
		//		System.out.println("Created " + K + " random centroids:");
		for (int i=0; i<K; i++) {
			bound = samp.size();
			randomNum=r.nextInt(bound);
			centroids[i]=(samp.get(randomNum)).values.clone(); // <- adding a random sample as a centroid
			//			System.out.println(Arrays.toString(centroids[i]));
			samp.remove(randomNum);
		}
		return centroids;
	}

	// Function to cluster the data using kMean-> returning average distance in order to evaluate its accuracy
	public static double kMeanGetAverage(int K, ArrayList<Sample> samples, ArrayList<ClustSample> samplesClustered, double[][] centroids) {

//		System.out.println("Initializing variables...");
		double avarageDistance = 0; // <- to store the avarage distance for all samples to their assigned centroids (for final determination if centroids were assigned properly)
		double distanceTotal; // <- to store total distance from all samples to their assigned centroids
		int samplesTotal; // <- to store total samples assigned to all centroids EDIT: WE ALREADY KNOW ITS 200 FROM THE SQLdatabase FUNCTION
		double differenceMax = 0.41; // <- this is just to make sure that we enter the while loop
		int iterations = 0; // <- to keep track how many iterations we did
		int closest = 0; // <- number of centroid that is closest to sample f
		double distances[] = new double[centroids.length]; // <- array for distances to each of the centroids
		double distanceMin; // <- distance to closest centroid
		double sum; // <- is used later to calculate distance from centroids to samples (as in the equation)
		double distance; // <- is used later to calculate distance from centroids to samples
		double sampleToCentroid[] = new double[K]; // <- array for storing distance between sample -> each centroid
		double[][] centroidsNew = new double[K][samples.get(0).values.length]; // <- array for new centroids
		int[] counter = new int[centroids.length]; // <- array for counting how many flowers belong to each cluster
		double[] difference = new double[K]; // <- array for differences between new and old centroids
//		System.out.println("Entering while loop...\n");

		double[] maxValue, minValue, dValue;
		maxValue = new double[samples.get(0).values.length];
		minValue = new double[samples.get(0).values.length];
		dValue = new double[samples.get(0).values.length];

		for (int i = 0; i < samples.get(0).values.length; i++) {
			maxValue[i]=Double.MIN_VALUE;
			minValue[i]=Double.MAX_VALUE;
		}

		for (Sample sample : samples) {
			for (int i = 0; i < sample.values.length; i++) {
				maxValue[i] = Math.max(maxValue[i], sample.values[i]);
				minValue[i] = Math.min(minValue[i], sample.values[i]);
				dValue[i] = maxValue[i] - minValue[i];
				if (dValue[i]==0 || dValue[i]==9) {
					dValue[i]=1;
				}
			}
		}

		while(differenceMax >= 0.3 && iterations<100) {

			iterations++;

			Arrays.fill(sampleToCentroid, 0); // <- filling the array with zeros
			//System.out.println("Iteration " + iterations);

			//System.out.println("Clearing out clustered samples list...");
			samplesClustered.clear();

			//System.out.println("Calculating distance of each sample to every cenroid...");
			// calculate distance of each flower from the centroids
			for (Sample s: samples) {
				closest = 0; // <- number of centroid that is closest to sample f
				distanceMin = 1000; // <- distance to closest centroid (super high value initially)

				for (int i=0; i<centroids.length; i++) {
					sum = 0; // <- start from 0 for every centroid
					distance = 0; // <- start from zero for every centroid

					for (int j=0; j<s.values.length; j++) {

						//							sum = sum + (Math.pow(((s.values[j]- minValue[j])/(dValue[j]) - (centroids[i][j]- minValue[j])/(dValue[j])),2));
						//sum = sum + (Math.pow((s.values[j]-minValue[j])/dValue[j] - ((centroids[i][j])-minValue[j])/dValue[j],2));	
						sum = sum + Math.pow((s.values[j] - centroids[i][j]),2);

					} // end of for (every parameter of the sample s)

					distance = Math.sqrt(sum); // <- distance between flower f and centroid i
					distances[i] = distance; // fill the array 

					if (distance < distanceMin) { // find the closest centroid
						distanceMin = distance;
						closest = i; // <- assign centroid as closest
						//sampleToCentroid[i] = distance; // <- array storing a distance between each sample and its closest centroid
					}
				} // end for every centroid

				sampleToCentroid[closest] = sampleToCentroid[closest] + distanceMin;

				// place the value in the closest cluster
				samplesClustered.add(new ClustSample(s.time,s.values,distances,closest));	

			} // end of for (every sample)

			//System.out.println("Calculating new centroids...");
			// calculate the new centroids
			//System.out.println("Resetting new centroids...");
			for (double[] row : centroidsNew) {
				Arrays.fill(row, 0); // <- filling the array with zeros
			}

			//System.out.println("Reseting counter...");
			Arrays.fill(counter, 0); // <- filling the array with zeros

			//System.out.println("Counting samples that belong to every cluster and summing their dimensions...");
			for (ClustSample cs: samplesClustered) {
				for (int i=0; i<centroids.length; i++) {
					if (cs.clusterNum==i) {
						counter[i]++; // <- counting how many samples belong to a certain cluster
						for (int j=0; j<cs.values.length; j++) {
							centroidsNew[i][j] = centroidsNew[i][j] + cs.values[j]; // <- summing up dimensions of every sample that belongs to a certain cluster
						} // end for every parameter (= centroid dimension)
					} // end if centroid = sample's clusterNo
				} // end for every centroid
			} // end for every clustered sample

			//System.out.println("Calculating new centroids...");
			for (int i=0; i<centroidsNew.length; i++) {
				//System.out.println("To centroid " + (i+1) + " belong " + counter[i] + " samples");
				for (int j=0; j<centroidsNew[i].length; j++) {
					if (counter[i]!=0){
						centroidsNew[i][j] = centroidsNew[i][j] / counter[i]; // <- mean value of every dimension for every centroid
					}
					else { //TO AVOID DIVISIONS BY 0
						centroidsNew[i][j] = centroidsNew[i][j]; 
					}	
					//					System.out.println("New-centroid " + (i+1) + " dimension " + j + " is " + centroidsNew[i][j]);
				} // end for every new centroid's dimension
			} // end for every new centroid

			//System.out.println("Calculating distances between old and new centroids...");
			// calculate difference between new and old centroids
			differenceMax = 0;
			for (int i=0; i<centroids.length; i++) {
				difference[i] = 0;
				for (int j=0;j<centroids[0].length;j++){

					//						difference[i] += Math.pow((centroids[i][j]- minValue[j])/(dValue[j])-(centroidsNew[i][j]- minValue[j])/(dValue[j]), 2);
					//difference[i] += Math.pow(((centroids[i][j]-minValue[j])/dValue[j]-(centroidsNew[i][j]-minValue[j])/dValue[j]), 2);
					difference[i] += Math.pow((centroidsNew[i][j] - centroids[i][j]),2);

				}
				difference[i]=Math.sqrt(difference[i]);

				if (difference[i]>differenceMax) {
					differenceMax = difference[i]; // <- updating maximal difference
				}
				// update old centroids
				//System.out.println("Updating old centroids...");
				//System.out.println("Difference between old and new centroid " + i + " was " + difference[i]);
				for (int j=0; j<centroids[i].length; j++) {
					centroids[i][j] = centroidsNew[i][j];
				}
			} // end for every centroid
			//System.out.println("Maximal difference was: " + differenceMax);

			//System.out.println("Iteration executed...\n");

		} // end of while
		//System.out.println("Loop executed...\n");

		//System.out.println("Calculating average distance from all samples to their assigned centroids...");
		distanceTotal = 0; // <- start from 0
		samplesTotal = 0; // <- start from 0
		for (int i=0; i<centroids.length; i++) {
//			System.out.println("Sum of distances of " + counter[i] + " samples to centroid " + (i+1) + " is " + sampleToCentroid[i]);
//			System.out.println("Avarage distance to centroid " + (i+1) + " is " + (sampleToCentroid[i]/counter[i]));
			distanceTotal += sampleToCentroid[i]; // <- we want to know the summation of ALL distances of samples to their assigned centroids...
			samplesTotal += counter[i]; // <- ... and the summation of all samples
		} // end for every centroid
		avarageDistance = distanceTotal / samplesTotal; // <- avarage distance to all clusters EDIT: HERE WE SHOULD MAKE A WEIGHTED AVARAGE PROBABLY!!


		//System.out.println("Avarage distance is " + avarageDistance + "\n\n");

		return avarageDistance;
	}

	// Function to cluster the data using kMean -> returning clustered samples as an ArrayList (use it having determined best-fit centroids)
	public static ArrayList<ClustSample> kMeanGetClusters(int K, ArrayList<Sample> samples, ArrayList<ClustSample> samplesClustered, double[][] centroids) {

//		System.out.println("Initializing variables...");
		double avarageDistance = 0; // <- to store the avarage distance for all samples to their assigned centroids (for final determination if centroids were assigned properly)
		double distanceTotal; // <- to store total distance from all samples to their assigned centroids
		int samplesTotal; // <- to store total samples assigned to all centroids EDIT: WE ALREADY KNOW ITS 200 FROM THE SQLdatabase FUNCTION
		double differenceMax = 0.41; // <- this is just to make sure that we enter the while loop
		int iterations = 0; // <- to keep track how many iterations we did
		int closest = 0; // <- number of centroid that is closest to sample f
		double distances[] = new double[centroids.length]; // <- array for distances to each of the centroids
		double distanceMin; // <- distance to closest centroid
		double sum; // <- is used later to calculate distance from centroids to samples (as in the equation)
		double distance; // <- is used later to calculate distance from centroids to samples
		double sampleToCentroid[] = new double[K]; // <- array for storing distance between sample -> each centroid
		double[][] centroidsNew = new double[K][samples.get(0).values.length]; // <- array for new centroids
		int[] counter = new int[centroids.length]; // <- array for counting how many flowers belong to each cluster
		double[] difference = new double[K]; // <- array for differences between new and old centroids
//		System.out.println("Entering while loop...\n");

		double[] maxValue, minValue, dValue;
		maxValue = new double[samples.get(0).values.length];
		minValue = new double[samples.get(0).values.length];
		dValue = new double[samples.get(0).values.length];

		for (int i = 0; i < samples.get(0).values.length; i++) {
			maxValue[i]=Double.MIN_VALUE;
			minValue[i]=Double.MAX_VALUE;
		}

		for (Sample sample : samples) {
			for (int i = 0; i < sample.values.length; i++) {
				maxValue[i] = Math.max(maxValue[i], sample.values[i]);
				minValue[i] = Math.min(minValue[i], sample.values[i]);
				dValue[i] = maxValue[i] - minValue[i];
				if (dValue[i]==0 || dValue[i]==9) {
					dValue[i]=1;
				}
			}
		}

		while(differenceMax >= 0.3 && iterations<100) {

			iterations++;

			Arrays.fill(sampleToCentroid, 0); // <- filling the array with zeros
//			System.out.println("Iteration " + iterations);

//			System.out.println("Clearing out clustered samples list...");
			samplesClustered.clear();

//			System.out.println("Calculating distance of each sample to every cenroid...");
			// calculate distance of each flower from the centroids
			for (Sample s: samples) {
				closest = 0; // <- number of centroid that is closest to sample f
				distanceMin = 1000; // <- distance to closest centroid (super high value initially)

				for (int i=0; i<centroids.length; i++) {
					sum = 0; // <- start from 0 for every centroid
					distance = 0; // <- start from zero for every centroid

					for (int j=0; j<s.values.length; j++) {

						//							sum = sum + (Math.pow(((s.values[j]- minValue[j])/(dValue[j]) - (centroids[i][j]- minValue[j])/(dValue[j])),2));
						//sum = sum + (Math.pow((s.values[j]-minValue[j])/dValue[j] - ((centroids[i][j])-minValue[j])/dValue[j],2));	
						sum = sum + Math.pow((s.values[j] - centroids[i][j]),2);

					} // end of for (every parameter of the sample s)

					distance = Math.sqrt(sum); // <- distance between flower f and centroid i
					distances[i] = distance; // fill the array 

					if (distance < distanceMin) { // find the closest centroid
						distanceMin = distance;
						closest = i; // <- assign centroid as closest
						//sampleToCentroid[i] = distance; // <- array storing a distance between each sample and its closest centroid
					}
				} // end for every centroid

				sampleToCentroid[closest] = sampleToCentroid[closest] + distanceMin;

				// place the value in the closest cluster
				samplesClustered.add(new ClustSample(s.time, s.values,distances,closest));	

			} // end of for (every sample)

//			System.out.println("Calculating new centroids...");
			// calculate the new centroids
//			System.out.println("Resetting new centroids...");
			for (double[] row : centroidsNew) {
				Arrays.fill(row, 0); // <- filling the array with zeros
			}

//			System.out.println("Reseting counter...");
			Arrays.fill(counter, 0); // <- filling the array with zeros

//			System.out.println("Counting samples that belong to every cluster and summing their dimensions...");
			for (ClustSample cs: samplesClustered) {
				for (int i=0; i<centroids.length; i++) {
					if (cs.clusterNum==i) {
						counter[i]++; // <- counting how many samples belong to a certain cluster
						for (int j=0; j<cs.values.length; j++) {
							centroidsNew[i][j] = centroidsNew[i][j] + cs.values[j]; // <- summing up dimensions of every sample that belongs to a certain cluster
						} // end for every parameter (= centroid dimension)
					} // end if centroid = sample's clusterNo
				} // end for every centroid
			} // end for every clustered sample

//			System.out.println("Calculating new centroids...");
			for (int i=0; i<centroidsNew.length; i++) {
				//System.out.println("To centroid " + (i+1) + " belong " + counter[i] + " samples");
				for (int j=0; j<centroidsNew[i].length; j++) {
					if (counter[i]!=0){
						centroidsNew[i][j] = centroidsNew[i][j] / counter[i]; // <- mean value of every dimension for every centroid
					}
					else { //TO AVOID DIVISIONS BY 0
						centroidsNew[i][j] = centroidsNew[i][j]; 
					}	
					//					System.out.println("New-centroid " + (i+1) + " dimension " + j + " is " + centroidsNew[i][j]);
				} // end for every new centroid's dimension
			} // end for every new centroid

//			System.out.println("Calculating distances between old and new centroids...");
			// calculate difference between new and old centroids
			differenceMax = 0;
			for (int i=0; i<centroids.length; i++) {
				difference[i] = 0;
				for (int j=0;j<centroids[0].length;j++){

					//						difference[i] += Math.pow((centroids[i][j]- minValue[j])/(dValue[j])-(centroidsNew[i][j]- minValue[j])/(dValue[j]), 2);
					//difference[i] += Math.pow(((centroids[i][j]-minValue[j])/dValue[j]-(centroidsNew[i][j]-minValue[j])/dValue[j]), 2);
					difference[i] += Math.pow((centroidsNew[i][j] - centroids[i][j]),2);

				}
				difference[i]=Math.sqrt(difference[i]);

				if (difference[i]>differenceMax) {
					differenceMax = difference[i]; // <- updating maximal difference
				}
				// update old centroids
//				System.out.println("Updating old centroids...");
//				System.out.println("Difference between old and new centroid " + i + " was " + difference[i]);
				for (int j=0; j<centroids[i].length; j++) {
					centroids[i][j] = centroidsNew[i][j];
				}
			} // end for every centroid
//			System.out.println("Maximal difference was: " + differenceMax);
//
//			System.out.println("Iteration executed...\n");	

		} // end of while
//		System.out.println("Loop executed...\n");

//		System.out.println("Calculating average distance from all samples to their assigned centroids...");
		distanceTotal = 0; // <- start from 0
		samplesTotal = 0; // <- start from 0
		for (int i=0; i<centroids.length; i++) {
//			System.out.println("Sum of distances of " + counter[i] + " samples to centroid " + (i+1) + " is " + sampleToCentroid[i]);
//			System.out.println("Avarage distance to centroid " + (i+1) + " is " + (sampleToCentroid[i]/counter[i]));
			distanceTotal += sampleToCentroid[i]; // <- we want to know the summation of ALL distances of samples to their assigned centroids...
			samplesTotal += counter[i]; // <- ... and the summation of all samples
		} // end for every centroid
		avarageDistance = distanceTotal / samplesTotal; // <- avarage distance to all clusters EDIT: HERE WE SHOULD MAKE A WEIGHTED AVARAGE PROBABLY!!


//		System.out.println("Avarage distance is " + avarageDistance + "\n\n");

		return samplesClustered;
	}

	// Function to cluster the data using kNN -> returns clustered samples as an ArrayList
	public static ArrayList<ClustSample> kNNGetClusters(int K, int kNearest, ArrayList<Sample> samplesTest, ArrayList<ClustSample> samplesClustered, ArrayList<ClustSample> samplesTestClusteredKNN, ArrayList<Result> resultList) {
		int[] clusters = new int[K]; // <- array for counting how many samples every cluster are near to the query sample
		int clusterNum = 0;
		double[] query;

		int[] counter = new int[K]; // < array for counting samples clusterd to each centroid
		Arrays.fill(counter, 0); // <- filling the array with zeros

		for (Sample s: samplesTest) {

			query = s.values;

			for (ClustSample cs : samplesClustered) {
				double sum = 0;
				double distance = 0;
				for (int j=0; j<cs.values.length; j++) {
					sum = sum + Math.pow((cs.values[j] - query[j]),2);
				} // end of for j
				distance = Math.sqrt(sum);
				resultList.add(new Result(distance, cs.clusterNum));
			}
			Collections.sort(resultList, new DistanceComparator());

			for (int r=0; r<kNearest; r++) {
				for (int c=0; c<clusters.length; c++) {
					if (resultList.get(r).clusterNum == c) {
						clusters[c]++;
					}
				} // end for counting occurances of samples of a particular cluster among the closest samples
			} // end for kNearest closest samples

			int maximum = getMax(clusters);

			for (int c=0; c<clusters.length; c++) {
				if (maximum == clusters[c]) {
					clusterNum = c;
					counter[c]++;
				}
			}

			samplesTestClusteredKNN.add(new ClustSample(s.time, query, null, clusterNum));

			resultList.clear();

			for (int c=0; c<clusters.length; c++) {
				clusters[c] = 0;
			}

//			System.out.println("TestSample clustered as " + clusterNum);

		}

		for (int c=0; c<counter.length; c++) {
//			System.out.println("To cluster " + c + " belongs " + counter[c] + " test-samples");
		}

		return samplesTestClusteredKNN;

	} // end of kNN

	// Function for getting the maximum value in an Array
	public static int getMax(int[] inputArray){ 
		int maxValue = inputArray[0]; 
		for(int i=1;i < inputArray.length;i++){ 
			if(inputArray[i] > maxValue){ 
				maxValue = inputArray[i]; 
			} 
		} 
		return maxValue; 
	}

	// Function for getting the best-fit centroids
	public static double[][] getBestFitCentroids(int iterations, int K, int parameters, ArrayList<Sample> samples, ArrayList<ClustSample> samplesClustered) { 
		double averageDistance; // <- averageDistance of samples to centroids assigned to them (as a measure of quality of our algorithm)
		double averageDistanceMin = 1000; // <- initially high value, to start looping correctly (when looking for the best-fit centroids)
		double[][] centroids = new double[K][parameters];
		double[][] centroidsBest= new double[K][parameters];
		for (int i=0; i<iterations; i++) {
//			System.out.println("------------------------ Run number " + iterations + " ------------------------");
			centroids = createRandomCentroids(samples,K);
			averageDistance = kMeanGetAverage(K, samples, samplesClustered, centroids);
			if (averageDistance < averageDistanceMin) {
				averageDistanceMin = averageDistance; // <- re-assign minimum avg distance
				centroidsBest = centroids; // <re-assign best centroids to the ones with smalles avg distance
			} // end if avgDist smaller than the minimum avgDist
		} // end for all iterations
//		System.out.println("Smalles average distance was: " + averageDistanceMin);
		return centroidsBest;
	}

	public static ArrayList<ClustSample> clusterSamples(int iterations, int K, int parameters, double[][] centroidsBest, ArrayList<Sample> samples, ArrayList<ClustSample> samplesClustered, boolean letsPlot) {


		// cluster samples using best-fit centroids
//		System.out.println("CLUSTERING SAMPLES...");
		samplesClustered = kMeanGetClusters(K, samples, samplesClustered, centroidsBest);
//		System.out.println("SAMPLES CLUSTERED!");


		// return arrayList of clustered samples
		return samplesClustered;
	}
	
	public static ArrayList<Graph> plot(int iterations, int K, int parameters, double[][] centroidsBest, ArrayList<Sample> samples, ArrayList<ClustSample> samplesClustered, boolean letsPlot) {
		
//		System.out.println("CLUSTERING SAMPLES...");
		samplesClustered = kMeanGetClusters(K, samples, samplesClustered, centroidsBest);
//		System.out.println("SAMPLES CLUSTERED!");

			ArrayList<Graph> graphs = new ArrayList<Graph>();
			
			for (int i = 1; i < 10; i++) {
				graphs.add(new Graph(samplesClustered, centroidsBest, i));
				graphs.get(i-1).createAndShowGui();
				graphs.get(i-1).hide();
			}

		return graphs;
	}

	public static void testkNN(ArrayList<ClustSample> samplesTestClustered, ArrayList<ClustSample> samplesTestClusteredKNN) {
		int correct = 0;
		System.out.println("\nTesting kNN...");
		
		for (int i=0; i<samplesTestClusteredKNN.size(); i++) {

			if (samplesTestClusteredKNN.get(i).clusterNum == samplesTestClustered.get(i).clusterNum) {
				correct++;
			} // end if the clusters are the same for kNN and kMean

		} // end for every test-sample clustered with kMean
		
		System.out.println("Correct was " + correct + " out of " + samplesTestClusteredKNN.size());
	}
	
	// method for assigning the cluster names depending on the samples in each cluster
	public static void assignClusterNames(String [] clusterNames, double[][] centroidsBest) {
		/* High load rate during peak hours
		 * Shut down of generator for maintenance 
		 * Low load rate during night
		 * Disconnection of a line for maintenance
		 * 
		 * Load Buses: {5, 7, 9} 
		 * Generator Buses: {1, 2, 3} */
		int K = centroidsBest.length;
		double[] loadVoltageSum = new double[K];
		double minDiffBetweenBuses = Double.MAX_VALUE;
		int minLoadVoltageCluster = 0;
		int maxLoadVoltageCluster = 0;
		int generatorDisconnectedCluster = -1;
		
		for (int i = 0; i < K; i++) {
			loadVoltageSum[i] = centroidsBest[i][5-1]
					+ centroidsBest[i][7-1]
					+ centroidsBest[i][9-1];
//			System.out.println("centroid " + (i+1) + " load voltage sum: " + loadVoltageSum[i]);
			
			if(voltageDistance(centroidsBest[i], 2, 8) < minDiffBetweenBuses){
				minDiffBetweenBuses = voltageDistance(centroidsBest[i], 2, 8);			// approx. no current between buses 2 and 8
				generatorDisconnectedCluster = i;
			}
			if (voltageDistance(centroidsBest[i], 3, 6) < minDiffBetweenBuses) {
				minDiffBetweenBuses = voltageDistance(centroidsBest[i], 3, 6);			// approx. no current between buses 3 and 6
				generatorDisconnectedCluster = i;
			}
			if (voltageDistance(centroidsBest[i], 1, 4) < minDiffBetweenBuses){
				minDiffBetweenBuses = voltageDistance(centroidsBest[i], 1, 4);			// approx. no current between buses 1 and 4
				generatorDisconnectedCluster = i;
			}
			
			if (i > 0) {
				if (loadVoltageSum[i] > loadVoltageSum[maxLoadVoltageCluster]){
//					System.out.println("greater than, i: " + i + "   " + loadVoltageSum[i] + "  " + loadVoltageSum[i - 1]);
					maxLoadVoltageCluster = i;
				}
				if (loadVoltageSum[i] < loadVoltageSum[minLoadVoltageCluster]){
//					System.out.println("less than, i: " + i + "   " + loadVoltageSum[i] + "  " + loadVoltageSum[i - 1]);
					minLoadVoltageCluster = i;
				}
			}
			
		} //end of for
//		System.out.println("minload: " + (minLoadVoltageCluster+1));
//		System.out.println("maxload: " + (maxLoadVoltageCluster+1));
//		System.out.println("disconected generator: " + (generatorDisconnectedCluster+1));
		//Check that all names were assigned
		if (minLoadVoltageCluster * maxLoadVoltageCluster * generatorDisconnectedCluster >= 0){
			for (int i = 0; i < K; i++) {
				clusterNames[i] = "Disconnection of a line for maintenance";
			}
			clusterNames[minLoadVoltageCluster] = "High load rate during peak hours";
			clusterNames[maxLoadVoltageCluster] = "Low load rate during night";
			clusterNames[generatorDisconnectedCluster] = "Shut down of generator for maintenance";
			//System.out.println("All clusters were assigned a name.");
		}
		else {
			//System.out.println("Something went wrong when assigning cluster names.");
		}
		
	} // end of assignClusterNames
	
	public static double voltageDistance(double[] centroidsBest_i, int bus1, int bus2){
		double distance;
		distance = Math.sqrt(Math.pow((centroidsBest_i[bus1-1]-centroidsBest_i[bus2-1]), 2) + Math.pow((centroidsBest_i[bus1+8]-centroidsBest_i[bus2+8]), 2));
//		System.out.println("\nbus " + bus1 + " and bus " + bus2);
//		System.out.println("voltage 1: " + centroidsBest_i[bus1-1] + " voltage 2: " + centroidsBest_i[bus2-1]);
//		System.out.println("angle 1: " + centroidsBest_i[bus1+8] + " angle 2: " + centroidsBest_i[bus2+8]);
//		System.out.println(distance);
		return distance;
	}

} // end of kMeans
