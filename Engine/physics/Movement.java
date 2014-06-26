package physics;
public class Movement
{
	protected long[] v,	q;	//v is the velocity of the respective ball, q is the acceleration
	public Movement()
	{
		v=new long[]{0,0,0};
		q=new long[]{0,0,0};
	}
	//Responsible for the velocity and acceleration if a ball. When both are close to zero
	//For all the balls in the cell the cell goes to sleep until a nearby cell wakes up.
}
