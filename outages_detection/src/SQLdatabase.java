

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class SQLdatabase {

	// Function to sort by column 
	public static void sortbyColumn(int arr[][], int col) { 

		// Using built-in sort function Arrays.sort 
		Arrays.sort(arr, new Comparator<int[]>() { 

			@Override              
			// Compare values according to columns 
			public int compare(final int[] entry1,  
					final int[] entry2) { 

				// To sort in descending order revert  
				// the '>' Operator 
				if (entry1[col] > entry2[col]) 
					return 1; 
				else
					return -1; 

			} 

		});  // End of function call sort(). 

	} // end of sortbyColumn

	// Function to create table of samples by importing table from SQL database
	public static ArrayList<Sample> getSamples(String database, String username, String password, String tableName) {

		Connection conn = null;
		Statement stmt = null;
		ArrayList<Sample> samples = new ArrayList<Sample>();


		try{

			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.cj.jdbc.Driver"); // dynamically load the driver's class file into memory, which automatically registers it

			//STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/" + database,username,password);
			System.out.println("Connected database successfully...");

			System.out.println("Creating statement...");
			stmt = conn.createStatement(); // Creates a Statement object
			System.out.println("Statement created...");

			//STEP 4: Execute queries

			// getting number of attributes per sample
			ResultSet NBus = stmt.executeQuery("SELECT count(*) AS NBus FROM substations;");
			NBus.next();
			int buses = Integer.parseInt(NBus.getString("Nbus")); // <- buses in the system
			int attributes = 2*buses; // <- attributes for each sample

			System.out.println("There is " + buses + " buses in the system and every sample has " + attributes + " attributes.");


			ResultSet measurementsTable = stmt.executeQuery("SELECT * FROM " + tableName + " ORDER BY time ASC, name ASC;");
			measurementsTable.last();
			int rows = measurementsTable.getRow(); // <- get the last row number (in SQL the numbering starts from 1!)
			int samplesTotal = rows / attributes; // <- calculate how many samples there is in the database
			//System.out.println("There is " + rows + " rows in the table");
			/*ResultSetMetaData metadata = measurementsTable.getMetaData();
			int columns = metadata.getColumnCount(); // <- columns number
			 */
			//STEP 5: Extract data from result set
			//ArrayList<ArrayList<Double>> values =  new ArrayList<ArrayList<Double>>(); // <- define ArrayList for values
			/*double busNo = 0;
			double volt = 0;
			double ang = 0;*/
			double[] values = new double[18];

			measurementsTable.beforeFirst();
			measurementsTable.next();
			for (int sample=0; sample<samplesTotal; sample++) {

				//System.out.println("While loop, sample " + sampleNo + " out of " + samplesTotal); // <- for DEBUGGING

				for(int j=0; j<buses; j++) {

					for(int i=0; i<2; i++) {

						//String rdfid  = measurementsTable.getString("rdfid");
						String name = measurementsTable.getString("name");
						//int time = measurementsTable.getInt("time");
						double value = measurementsTable.getDouble("value");
						//String sub_rdfid = measurementsTable.getString("sub_rdfid");
						if (i==1) {
							if (name.toLowerCase().indexOf("amhe") != -1) {
								values[1] = value;
							}
							else if (name.toLowerCase().indexOf("bowm") != -1) {
								values[3] = value;
							}
							else if (name.toLowerCase().indexOf("clar") != -1) {
								values[0] = value;
							}
							else if (name.toLowerCase().indexOf("cross") != -1) {
								values[8] = value;
							}
							else if (name.toLowerCase().indexOf("gran") != -1) {
								values[6] = value;
							}
							else if (name.toLowerCase().indexOf("mapl") != -1) {
								values[5] = value;
							}
							else if (name.toLowerCase().indexOf("troy") != -1) {
								values[4] = value;
							}
							else if (name.toLowerCase().indexOf("waut") != -1) {
								values[7] = value;
							}
							else if (name.toLowerCase().indexOf("winl") != -1) {
								values[2] = value;
							}
						}
						else {
							if (name.toLowerCase().indexOf("amhe") != -1) {
								values[10] = value;
							}
							else if (name.toLowerCase().indexOf("bowm") != -1) {
								values[12] = value;
							}
							else if (name.toLowerCase().indexOf("clar") != -1) {
								values[9] = value;
							}
							else if (name.toLowerCase().indexOf("cross") != -1) {
								values[17] = value;
							}
							else if (name.toLowerCase().indexOf("gran") != -1) {
								values[15] = value;
							}
							else if (name.toLowerCase().indexOf("mapl") != -1) {
								values[14] = value;
							}
							else if (name.toLowerCase().indexOf("troy") != -1) {
								values[13] = value;
							}
							else if (name.toLowerCase().indexOf("waut") != -1) {
								values[16] = value;
							}
							else if (name.toLowerCase().indexOf("winl") != -1) {
								values[11] = value;
							}
						}
						//System.out.println(time); // <- FOR DEBUGGING 
						measurementsTable.next();

					} // end for voltage-and-angle pair

				} // end for all buses in semple

				/*Arrays.sort(values, new Comparator<double[]>() {
					@Override
					public int compare(double[] o1, double[] o2) {
						return Double.compare(o1[0], o2[0]);
					}
				});*/
				
				samples.add(new Sample(0,values));
			} // end for there is still more rows

			measurementsTable.close();
			
//			System.out.println("\nSamples to be clustered: ");
			for (int i=0; i<samples.size(); i++) {
//				System.out.println(samples.get(i));
			}

			

		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(stmt!=null)
					conn.close();
			}catch(SQLException se){
			}// do nothing
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}//end try
//		System.out.println("\nSamples created successfully!\n");

		return samples;


	}


} // enf of class









/*package assignment2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class SQLdatabase {

	// Function to sort by column 
	public static void sortbyColumn(int arr[][], int col) { 

		// Using built-in sort function Arrays.sort 
		Arrays.sort(arr, new Comparator<int[]>() { 

			@Override              
			// Compare values according to columns 
			public int compare(final int[] entry1,  
					final int[] entry2) { 

				// To sort in descending order revert  
				// the '>' Operator 
				if (entry1[col] > entry2[col]) 
					return 1; 
				else
					return -1; 

			} 

		});  // End of function call sort(). 

	} // end of sortbyColumn

	// Function to create table of samples by importing table from SQL database
	public static ArrayList<Sample> getSamples(String database, String username, String password) {

		Connection conn = null;
		Statement stmt = null;
		ArrayList<Sample> samples = new ArrayList<Sample>();


		try{

			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.cj.jdbc.Driver"); // dynamically load the driver's class file into memory, which automatically registers it

			//STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/" + database,username,password);
			System.out.println("Connected database successfully...");

			System.out.println("Creating statement...");
			stmt = conn.createStatement(); // Creates a Statement object
			System.out.println("Statement created...");

			//STEP 4: Execute queries

			// getting number of attributes per sample
			ResultSet NBus = stmt.executeQuery("SELECT count(*) AS NBus FROM substations;");
			NBus.next();
			int buses = Integer.parseInt(NBus.getString("Nbus")); // <- buses in the system
			int attributes = 2*buses; // <- attributes for each sample

			System.out.println("There is " + buses + " buses in the system and every sample has " + attributes + " attributes.");


			ResultSet measurementsTable = stmt.executeQuery("SELECT * FROM measurements ORDER BY time ASC, name ASC;");
			measurementsTable.last();
			int rows = measurementsTable.getRow(); // <- get the last row number (in SQL the numbering starts from 1!)
			int samplesTotal = rows / attributes; // <- calculate how many samples there is in the database
			//System.out.println("There is " + rows + " rows in the table");
			ResultSetMetaData metadata = measurementsTable.getMetaData();
			int columns = metadata.getColumnCount(); // <- columns number
			 
			//STEP 5: Extract data from result set
			//ArrayList<ArrayList<Double>> values =  new ArrayList<ArrayList<Double>>(); // <- define ArrayList for values
			double busNo = 0;
			double volt = 0;
			double ang = 0;
			double[] values = new double[18];

			measurementsTable.beforeFirst();
			measurementsTable.next();
			for (int sample=0; sample<samplesTotal; sample++) {

				//System.out.println("While loop, sample " + sampleNo + " out of " + samplesTotal); // <- for DEBUGGING

				for(int j=0; j<buses; j++) {

					for(int i=0; i<2; i++) {

						//String rdfid  = measurementsTable.getString("rdfid");
						String name = measurementsTable.getString("name");
						//int time = measurementsTable.getInt("time");
						double value = measurementsTable.getDouble("value");
						//String sub_rdfid = measurementsTable.getString("sub_rdfid");
						if (i==1) {
							if (name.toLowerCase().indexOf("amhe") != -1) {
								values[1] = value;
							}
							else if (name.toLowerCase().indexOf("bowm") != -1) {
								values[3] = value;
							}
							else if (name.toLowerCase().indexOf("clar") != -1) {
								values[0] = value;
							}
							else if (name.toLowerCase().indexOf("cross") != -1) {
								values[8] = value;
							}
							else if (name.toLowerCase().indexOf("gran") != -1) {
								values[6] = value;
							}
							else if (name.toLowerCase().indexOf("mapl") != -1) {
								values[5] = value;
							}
							else if (name.toLowerCase().indexOf("troy") != -1) {
								values[4] = value;
							}
							else if (name.toLowerCase().indexOf("waut") != -1) {
								values[7] = value;
							}
							else if (name.toLowerCase().indexOf("winl") != -1) {
								values[2] = value;
							}
						}
						else {
							if (name.toLowerCase().indexOf("amhe") != -1) {
								values[10] = value;
							}
							else if (name.toLowerCase().indexOf("bowm") != -1) {
								values[12] = value;
							}
							else if (name.toLowerCase().indexOf("clar") != -1) {
								values[9] = value;
							}
							else if (name.toLowerCase().indexOf("cross") != -1) {
								values[17] = value;
							}
							else if (name.toLowerCase().indexOf("gran") != -1) {
								values[15] = value;
							}
							else if (name.toLowerCase().indexOf("mapl") != -1) {
								values[14] = value;
							}
							else if (name.toLowerCase().indexOf("troy") != -1) {
								values[13] = value;
							}
							else if (name.toLowerCase().indexOf("waut") != -1) {
								values[16] = value;
							}
							else if (name.toLowerCase().indexOf("winl") != -1) {
								values[11] = value;
							}
						}
						//System.out.println(time); // <- FOR DEBUGGING 
						measurementsTable.next();

					} // end for voltage-and-angle pair

				} // end for all buses in semple

				Arrays.sort(values, new Comparator<double[]>() {
					@Override
					public int compare(double[] o1, double[] o2) {
						return Double.compare(o1[0], o2[0]);
					}
				});
				
				samples.add(new Sample(values));

			} // end while there is still more rows

			measurementsTable.close();
			
			System.out.println("\nSamples to be clustered: ");
			for (int i=0; i<samples.size(); i++) {
				System.out.println(samples.get(i));
			}

			

		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(stmt!=null)
					conn.close();
			}catch(SQLException se){
			}// do nothing
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}//end try
		System.out.println("\nSamples created successfully!\n");

		return samples;


	}


} // enf of class
*/

