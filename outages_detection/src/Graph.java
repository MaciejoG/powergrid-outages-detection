

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Graph extends JPanel{
    private int width = 400;
    private int heigth = 230;
    private int padding = 25;
    private int labelPadding = 25;
    private Color lineColor = new Color(44, 102, 230, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color[] colors;
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int pointWidth = 4;
    private int numberYDivisions = 10;
    private List<Double> scores;
    ArrayList<ClustSample> samplesClustered; 
	double[][] centroids;
	int busNumber;
	JFrame frame;

    public Graph(ArrayList<ClustSample> samplesClustered, double[][] centroids, int busNumber) {
        this.samplesClustered = new ArrayList<ClustSample>(samplesClustered);
        this.centroids=centroids.clone();
        this.colors = new Color[centroids.length];
        this.busNumber=busNumber-1;
//        for (int i = 0; i < centroids.length; i++){
//        	colors[i]= new Color(i*40, (255-i*40), (int)(Math.random()*255), 150);
//        }
        colors[0]=Color.BLUE;
        colors[1]=Color.RED;
        colors[2]=Color.YELLOW;
        colors[3]=Color.GREEN;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double xScale = (double) (getWidth() - 2 * padding - labelPadding) / (getMaxVoltage() - getMinVoltage());
        double yScale = (double) (getHeight() - 2 * padding - labelPadding) / (getMaxAngle() - getMinAngle());

        List<Point> graphPoints = new ArrayList<>();
        int zz = 0;
        for (Sample sample : samplesClustered){
        	zz++;
        	int xpos = (int) ((sample.values[busNumber] - getMinVoltage()) * xScale + padding + labelPadding);
			int ypos = (int) ((getMaxAngle() - sample.values[busNumber+9]) * yScale + padding);
			graphPoints.add(new Point(xpos, ypos));
//			System.out.println("Point " + zz +": (" + xpos + ", " + ypos + ")");
        }
//		System.out.println("==========================================");		        
        List<Point> centroidPoints = new ArrayList<>();
        for (int i = 0; i < centroids.length; i++){
        	int xpos = (int) ((centroids[i][busNumber] - getMinVoltage()) * xScale + padding + labelPadding);
			int ypos = (int) ((getMaxAngle() - centroids[i][busNumber+9]) * yScale + padding);
			centroidPoints.add(new Point(xpos, ypos));
//			System.out.println("Centroid " + zz +": (" + xpos + ", " + ypos + ")");
        }
        
        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        // create hatch marks and grid lines for y axis.
        for (int i = 0; i < numberYDivisions + 1; i++) {
            int x0 = padding + labelPadding;
            int x1 = pointWidth + padding + labelPadding;
            int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            int y1 = y0;
            if (samplesClustered.size() > 0) {
                g2.setColor(gridColor);
                g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
                g2.setColor(Color.BLACK);
                String yLabel = ((int) ((getMinAngle() + (getMaxAngle() - getMinAngle()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(yLabel);
                g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
            }
            g2.drawLine(x0, y0, x1, y1);
        }

        // and for x axis
        for (int i = 0; i < numberYDivisions + 1; i++) {
            int x0 = (i * (getWidth() - padding * 2 - labelPadding) / numberYDivisions + padding + labelPadding);
            int x1 = x0;
            int y0 = getHeight() - padding - labelPadding;
            int y1 = y0;
            if (samplesClustered.size() > 0) {
                g2.setColor(gridColor);
                g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
                g2.setColor(Color.BLACK);
                String yLabel = ((int) ((getMinVoltage() + (getMaxVoltage() - getMinVoltage()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(yLabel);
                g2.drawString(yLabel, x0 - labelWidth/2, y0 + (metrics.getHeight() / 2 + 10));
            }
            g2.drawLine(x0, y0, x1, y1);
        }
					 
        // create x and y axes 
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        Stroke oldStroke = g2.getStroke();

//        	int zz=0;
	        for (int i = 0; i < graphPoints.size(); i++) {
	        	g2.setStroke(oldStroke);
		        g2.setColor(colors[samplesClustered.get(i).clusterNum].brighter());
//	        	zz++;
	            int x = graphPoints.get(i).x - pointWidth / 2;
	            int y = graphPoints.get(i).y - pointWidth / 2;
	            int ovalW = pointWidth;
	            int ovalH = pointWidth;
	            g2.fillOval(x, y, ovalW, ovalH);
//	            System.out.println("Point " + zz + ":( " + x + ", " + y + ")");
	        }
	        
	        for (int k = 0; k < centroidPoints.size(); k++) {
	        	g2.setStroke(GRAPH_STROKE);
		        g2.setColor(colors[k].darker().darker());
//	        	zz++;
	            int x = centroidPoints.get(k).x - pointWidth / 2;
	            int y = centroidPoints.get(k).y - pointWidth / 2;
//	            int ovalW = 2*pointWidth;
//	            int ovalH = 2*pointWidth;
	            g2.drawLine((x-2),(y-2),(x+2),(y+2));
	            g2.drawLine((x-2),(y+2),(x+2),(y-2));
//	            g2.fillRect(x, y, ovalW, ovalH);
//	            System.out.println("Point " + zz + ":( " + x + ", " + y + ")");
	        }
	        
//	        System.out.println("Min x = " + getMinVoltage()*);
//	        System.out.println("Point " + zz + ":( " + x + ", " + y + ")");
//	        System.out.println("Point " + zz + ":( " + x + ", " + y + ")");
//	        System.out.println("Point " + zz + ":( " + x + ", " + y + ")");
    }

//    @Override

    private double getMinVoltage() {
        double minVoltage = Double.MAX_VALUE;
        for (Sample sample : samplesClustered) {
        	minVoltage= Math.min(minVoltage, sample.values[busNumber]);
        }
        return minVoltage;
    }

    private double getMaxVoltage() {
    	double maxVoltage = Double.MIN_VALUE;
        for (Sample sample : samplesClustered) {
        	maxVoltage= Math.max(maxVoltage, sample.values[busNumber]);
        }
        return maxVoltage;
    }
    
    private double getMinAngle() {
    	double minAngle = Double.MAX_VALUE;
        for (Sample sample : samplesClustered) {
        	minAngle = Math.min(minAngle, sample.values[busNumber+9]);
        }
        return minAngle;
    }

    private double getMaxAngle() {
    	double maxAngle = Double.MIN_VALUE;
        for (Sample sample : samplesClustered) {
        	maxAngle = Math.max(maxAngle, sample.values[busNumber+9]);
        }
        return maxAngle;
    }

    public void setScores(List<Double> scores) {
        this.scores = scores;
        invalidate();
        this.repaint();
    }

    public List<Double> getScores() {
        return scores;
    }

	public void createAndShowGui() {
        setPreferredSize(new Dimension(width, heigth));
        if (busNumber == 0 || busNumber == 1 || busNumber == 2) {
        	frame = new JFrame("Clusters GENERATOR Bus " + (busNumber+1));
        }
        else if (busNumber == 4 || busNumber == 6 || busNumber == 8) {
        	frame = new JFrame("Clusters LOAD Bus " + (busNumber+1));
        }
        else { 
        	frame = new JFrame("Clusters Bus " + (busNumber+1));
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this);
        frame.pack();
        if (busNumber < 3){
        	frame.setLocation(width * busNumber, 0);
        }
        else if(busNumber < 6) {
        	frame.setLocation(width * (busNumber-3), heigth+padding);
        }
        else {
        	frame.setLocation(width * (busNumber-6), 2*(heigth+padding));
        }
        
//        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
	
	public void hide(){
		frame.setVisible(false);
	}
	
	public void show(){
		frame.setVisible(true);
	}

	
}
