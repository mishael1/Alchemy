package physics;
import physics.Movement;
public interface Reactor {
	//String getName();
	//String printFunction();
	public Ball react(Ball A,Ball B,long distance);
	public Ball reactKS(Ball A,Ball B,long distance);//in KS B is not moving
}
