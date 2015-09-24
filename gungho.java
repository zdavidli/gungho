import lejos.nxt.*; 
import lejos.util.Delay;
import lejos.robotics.RangeReading; 
import lejos.robotics.RangeReadings; 
import lejos.robotics.navigation.DifferentialPilot; 
import lejos.robotics.objectdetection.Feature; 
import lejos.robotics.objectdetection.FeatureDetector; 
import lejos.robotics.objectdetection.FeatureDetectorAdapter; 
import lejos.robotics.objectdetection.FeatureListener; 


public class GungHo implements FeatureListener { 
	public static final float WALL_DISTANCE = 20.0f; // tries to stay this far from the side wall (cm) 
	public static final float WALL_TOLERANCE = 10.0f;
	public static final float FRONT_DISTANCE = 17.5f; //this far from a front wall
	public static final float MOVE_DISTANCE = 7.0f; //moves this far before turning left
	public static final float MOVE_SPEED = 0.0f; //Change this sometime 
	public static final int LINE_COLOR = 600; 
	public static final int CANDLE_COLOR = 90;
	public static final int TURN_ANGLE = 90;

	GH_Slapper smacker = 
			new GH_Slapper(MotorPort.A);

	DifferentialPilot pilot = 
			new DifferentialPilot(3.1, 3.1, 17.4, Motor.C, Motor.B, false); 

	UltrasonicSensor  
	front = new UltrasonicSensor(SensorPort.S1), 
	left = new UltrasonicSensor(SensorPort.S4); 

	FeatureDetector lightDetector; 
	ColorSensor candleSensor = new ColorSensor(SensorPort.S2); 
	LineDetector lines;

	public GungHo(){
		lines = new LineDetector(SensorPort.S3);
		lines.addListener(this);
		lines.enableDetection(true);
		front.capture();
		left.capture();
	}

	public void roomAlg(){
		System.out.println("Room alg");
		//go 30 cm into room
		pilot.travel(30);
		//spin 10 degrees 36 times
		/*    	while(rotateCount < 36){
    		if(candleSensor.getNormalizedLightValue() > CANDLE_COLOR){
    			//stop the loop
    			rotateCount = 36;
    			//move toward the candle *SLOWLY*
    			pilot.setTravelSpeed(0.5);
    			pilot.forward();
    			while(true){
    				//slap
    			}
    		} else {
    			pilot.rotate(10);
    			rotateCount++;
    		}
    	}*/
		for (int rotateCount = 0; rotateCount < 36; rotateCount++){
			if(candleSensor.getNormalizedLightValue() > CANDLE_COLOR){
				pilot.setTravelSpeed(0.5);
				pilot.forward();
				System.out.println("Found a candle!");
				Sound.beepSequence();
				Delay.msDelay(5000);
				smacker.slap(5);
				break;
			} else {
				pilot.rotate(10);
				rotateCount++;
			}
		}

	} 

	public void adjust(){
		System.out.println("adjust");
		front.ping();
		Delay.msDelay(50);
		left.ping();
		Delay.msDelay(50);
		if(front.getDistance() > 10){
			int capture1 = left.getDistance();
			pilot.travel(10);
			left.ping();
			Delay.msDelay(50);
			int capture2 = left.getDistance();
			pilot.travel(-10);
			if(capture1 > WALL_DISTANCE + WALL_TOLERANCE || capture2 > WALL_DISTANCE + WALL_TOLERANCE){
				return;
			}
			double theta = Math.toDegrees((Math.atan(Math.abs(capture1 - capture2) / 10.0)));
			if(capture1 > capture2){
				pilot.rotate(-theta);
				System.out.println(-theta);
			} else if(capture1 < capture2){
				pilot.rotate(theta);
				System.out.println(theta);
			}

		}

	}

	/*public void movement(){ 

        while(lineSensor.readNormalizedValue() < LINE_COLOR){ 
            front.ping(); 

            left.ping(); 
            double distanceIn = front.getDistance() - WALL_DISTANCE; 
            if(front.getDistance() > WALL_DISTANCE){ 

                pilot.travel(distanceIn, true); 
            } else { 
                if(left.getDistance() > WALL_DISTANCE){ 
                    pilot.rotate(90); 

                    if(lineSensor.readNormalizedValue() > LINE_COLOR){ 
                        roomAlg(); 
                    } 
                } 
                else if (left.getDistance() <= WALL_DISTANCE){ 
                    pilot.rotate(90); 
                    if(front.getDistance() < WALL_DISTANCE){ 
                        pilot.rotate(90); 
                        pilot.travel(distanceIn); 
                    } else { 
                        if(lineSensor.readNormalizedValue() > LINE_COLOR){ 
                            roomAlg(); 
                        }    
                    } 
                }  
            } 
        } 
    } */
	public void forwardWithAdjustment(){
		pilot.forward();
		// if distance is > wall_distance, turn a tiny bit left
		if(left.getDistance() > WALL_DISTANCE){
			pilot.travel(5);
			left.ping();
			Delay.msDelay(50);
			if(left.getDistance() > WALL_DISTANCE + WALL_TOLERANCE){
				return;
			}
			pilot.stop();
			pilot.rotate(TURN_ANGLE);
			pilot.travel(4);
			pilot.rotate(-TURN_ANGLE);
		}
		else if(left.getDistance() < WALL_DISTANCE - WALL_TOLERANCE){
			pilot.stop();
			pilot.rotate(-TURN_ANGLE);
			pilot.travel(4);
			pilot.rotate(TURN_ANGLE);
		}
	}
	@Override
	public void featureDetected(Feature feature, FeatureDetector detector) { 
		int count = 1;

		System.out.print("Feature Detected!");
		System.out.println("#" + count++);
	} 

	static enum State { 
		NORMAL, LEFT_EMPTY, FRONT_OBSTRUCTED, PANIC 
	} 
	public void movement2(){ 
		State current; 
		front.ping();
		Delay.msDelay(50);
		left.ping(); 
		Delay.msDelay(50);
		if(front.getDistance() > FRONT_DISTANCE){ 
			if(left.getDistance() <= WALL_DISTANCE + WALL_TOLERANCE) 
				current = State.NORMAL; //normal if there is stuff on left and nothing in front
			else
				current = State.LEFT_EMPTY; //left empty if nothing on left (this is nothing in front)
		} else { 
			if(left.getDistance() > WALL_DISTANCE + WALL_TOLERANCE) 
				current = State.LEFT_EMPTY; //left empty if nothing on left (this is something in front)
			else
				current = State.FRONT_OBSTRUCTED; //front obstructed, stuff on left
		} 
		
		current = State.PANIC; //test lol

		switch(current){ 
		case NORMAL: 
			System.out.println("NORMAL");
			//pilot.travel(front.getDistance() - WALL_DISTANCE);
			//move forward 
			forwardWithAdjustment();

			if(left.getDistance() > WALL_DISTANCE || left.getDistance() < WALL_DISTANCE - WALL_TOLERANCE){
				adjust();
			}
			pilot.forward();
			// elif distance is < wall_dist - tolerance, turn a tiny bit right
			break; 
		case LEFT_EMPTY: 
			System.out.println("LEFT_EMPTY");
			pilot.travel(MOVE_DISTANCE);//clear the corner
			pilot.rotate(TURN_ANGLE); //turn
			pilot.travel(MOVE_DISTANCE*3); //move into the corner
			pilot.forward();
			//forward a bit, rotate left 90, go forward 
			break; 
		case FRONT_OBSTRUCTED: 
			System.out.println("FRONT_OBSTRUCTED");
			pilot.rotate(-TURN_ANGLE);
			pilot.forward();
			//rotate right 90, go forward 
			break; 

		case PANIC: 
			//PANICCCCCCCCC
			//potoooooooo
			pilot.rotateLeft(); 
			System.out.println("PANIC");
			while (true) { 
				Sound.setVolume(75); 
				Sound.beepSequence(); 
				smacker.slap();
				Sound.buzz(); 
				Sound.buzz(); 
				for (int i = 0; i < 15; ++i) 
					Sound.playTone((int)(Math.random()*5000)+1000, 75); 
				Sound.buzz(); 
			} 
		} 
	} 
	public void rotateTest(){
		pilot.rotate(TURN_ANGLE);
	}

	/** 
	 * @param args the command line arguments 
	 */
	public static void main(String[] args) { 

		// TODO code application logic here 
		GungHo gh = new GungHo();
		/*while(true){
		 * 	gh.rotate();
		 * }
		 */
		while(true){
			gh.movement2();
		}
	} 

	public static void testLightSensor() {
		LightSensor s = new LightSensor(SensorPort.S3);
		while (true) {
			Delay.msDelay(250);
			LCD.drawString("Light: ", 0,0);
			LCD.drawInt(s.getNormalizedLightValue(),7, 0);
		}
	}

	class LineDetector extends FeatureDetectorAdapter { 
		public static final int DELAY = 50; 
		public LightSensor sensor; 

		public class LineFeature implements Feature { 

			@Override
			public RangeReading getRangeReading() { 
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. 
			} 

			@Override
			public RangeReadings getRangeReadings() { 
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. 
			} 

			@Override
			public long getTimeStamp() { 
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. 
			} 

		} 

		public LineDetector(SensorPort s) { 
			super(DELAY); 
			sensor = new LightSensor(s); 
		} 

		@Override
		public Feature scan() { 

			if (sensor.getNormalizedLightValue() > LINE_COLOR) 
				return new LineFeature(); 
			else
				return null; 
		} 

	} 

} 

