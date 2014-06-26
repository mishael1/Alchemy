package physics.Actions;

import physics.Ball;
import physics.Movement;
import physics.Reactor;

public class Ignore implements Reactor 
{
	public Ball react(Ball A, Ball B, long distance) {
		return null;
	}
	public Ball reactKS(Ball A, Ball B, long distance) {
		return null;
	}
}
