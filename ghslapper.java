import lejos.nxt.*;
import lejos.util.Delay;

public class GH_Slapper {
	int retracted, extended;
	NXTRegulatedMotor arm;
	public GH_Slapper(MotorPort mp) {
		arm = new NXTRegulatedMotor(mp);
		arm.setSpeed(250);
		arm.backward();
		Delay.msDelay(300);
		arm.stop();
		retracted = arm.getTachoCount();
		arm.forward();
		Delay.msDelay(1050);
		arm.stop();
		extended = arm.getTachoCount();
		arm.setSpeed(900);
		arm.rotateTo(retracted);
		arm.flt();
	} 

	public void slap(int N) {
		for (int i = 0; i < N; ++i) {
			slap();
		}
	}
	public void slap() {
		arm.rotateTo(retracted);
		arm.rotateTo(extended);
		arm.rotateTo(retracted);
		arm.flt();
	}

	public static void main(String[] args) {
		GH_Slapper slapper = new GH_Slapper(MotorPort.A);
		while (true) {
			int button = Button.waitForAnyPress();
			if (button == Button.ID_ESCAPE)
				return;
			slapper.slap();
		}
	}
}
