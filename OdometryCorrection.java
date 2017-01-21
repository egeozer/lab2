/* 
 * OdometryCorrection.java
 */
package ev3Odometer;

import java.util.Arrays;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private EV3ColorSensor sensor = new EV3ColorSensor(LocalEV3.get().getPort("S1"));
	private SampleProvider sampleProvider;
	private Odometer odometer;
	
	private int[] distances = { 14, 44, 74 }; // { 12, 42, 72 }
	private int squaresLeft = 0;
	private int squaresTop = 0;
	private int squaresRight = 2;
	private int squaresBot = 2;
	private double theta = 0.0;

	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
		sensor.setFloodlight(true);
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		
		//define int for size of data array based on sensor sample size
		int dataSize = sensor.sampleSize();
		Sound.setVolume(50);
				
		while (true) {
			correctionStart = System.currentTimeMillis();

			// TODO: put your correction code here
			
			sampleProvider=sensor.getRedMode();
			//create array to store sensor data then retrieve data
			float sensorData[] = new float[dataSize];
			
			sampleProvider.fetchSample(sensorData, 0);
			
			LCD.drawString("Color: " + Arrays.toString(sensorData), 0, 3);
			
			if(sensorData[0] < .3 ) {
				
				Sound.beep();
				theta = odometer.getTheta();
				
				//left part of square
				if(theta < 1.5) {
					LCD.drawString("1", 0, 5);
					odometer.setY(distances[squaresLeft]);
					squaresLeft++;	
				}	
				//top part of square
				else if ((theta >= 1.5 && theta < 3)) {
					LCD.drawString("2", 0, 5);
					odometer.setX(distances[squaresTop]);
					squaresTop++;
				}
				//right part of square
				else if ((theta >= 3 && theta < 4.5)) {
					LCD.drawString("3", 0, 5);
					odometer.setY(distances[squaresRight]);
					squaresRight--;
				}	
				//bottom part of square
				else if ((theta >= 4.5 && theta < 6.2)) {
					LCD.drawString("4" , 0, 5);
					odometer.setX(distances[squaresBot]);
					squaresBot--;
				}
			}

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
}