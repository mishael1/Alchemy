package physics;

public class Ball
{
	/*All the longs in this class are handled as fixed point precision.
	 *The numbers are stored so that the right 16 bits are fraction,
	 *the left 47 bits are integer and the last bit is the sign.
	 *The highest numbers to be multiplied, therefore, should not exceed 2^47/2~=2^23
	 *which is stored by 23+16=39 bits. therefore the size of the largest of balls is expected to be 
	 *of order 2^22, and normal sized balls of order 2^16, which is stored with 2^32 bits out of the 64.
	 *Since the multiplication assumes 8 bits to be irrelevant the answer is set off with the order of 2^24
	 *while the answer is of order 2^48, so the relative offset is of order 2^-24, or ~10^-7
	 *Pretty accurate right?.Additionally, since a ball's radius is of order 2^32 bits, with a ball being 5 mm wide
	 *it will take about 20,000 kilometers side to side to overflow the numbers :D*/	 
	static Reactor[][] 	collideKind;	//what collision will take place for I,J elements
	static Clicker[]	stepKind;		//what special action action is done once clock is 0
	static long[][]		sumRad2;		//The sum (rad[i]+rad[j])^2
	static long[] 		rad;			//the radius of the ball type
	static long 		transferT; //The minimal temperature for heat transfer
	static double[][] 	heatConductivity;	//How good is the heat conductivity between 2 elements
	
	protected short type;					//Element type, for example: Stone, Metal, Oxygen, Azote (non-flableable air), Water, Oil, Wood positive charge  
	protected short mass,power, viscosity,concentration;	/*Power represents how much more force does this ball do. 
	The mass of particle of type i. You can think of it as if the mass is quantisized.
	For a specific element it is expected that mass and power are the same: 
	for granite they will be high but low for calcite*/ 
	protected short[] capture,reflect;		/*For each color, capture is how much goes to heat and reflect how much 
	becomes own brighness. 100%-(capture+reflect) is how much is transmited, and their sum must not be more than 100%*/
	protected int resetTime;				/*Number of ticks until the (next) reaction of particle*/
	//capture and reflect last bits in infra red and ultra violet are used for identifications
	protected long[] x;  					//Position in fixed point precision L	
	protected Movement p;
	protected int dTime;
	protected long 	temp;			 		//Temperature in Kelvin
	protected short[] brightness;			//Brightness represents the total amount of light the ball will emit
	/*Examples: The amount of Smoke(CO2/smoke) in Azote, the amount of water in Azote, the amount of Nickel
	 * in Iron, the amount of Acid in Acid-water, the amount of salt in salt-water, etc. In all cases the concentration
	 * is conserved over defusion so that the mass of the particles and the amount of the other matter is conserved.
	 * For simplicity the amount of the soluvent is of importance only when Reactions occure.
	 * Concentration is always smaller than the mass*/
	
	public static long Mul(long a,long b)//multiplies two fixed point longs, more than 99.99% accurate for numbers bigger than 1
	{
		return ((a>>8)*(b>>8));//the mistake is of order 2^-8!
	}
	public static long Pow(long a)//returns the power 2 of a fixed long a
	{
		return ((a>>8)*(a>>8));
	}
	public static double Form(long a)
	{
		return(a/(0.0+(1<<16)));
	}
	public Ball(short sort,short m,short p,short v,short c,
				short capIR,short capR,short capG,short capB,short capUV,
				short refIR,short refR,short refG,short refB,short refUV,
				short brIR,short brR,short brG,short brB,short brUV,
				int dT, int rT,long px,long py,long pz,long Temprature)
	{
		this.type=sort;
		this.mass=m;	this.power=p;	this.viscosity=v;	this.concentration=c;
		this.capture=new short[]{capIR,capR,capG,capB,capUV};
		this.reflect=new short[]{refIR,refR,refG,refB,refUV};
		this.brightness=new short[]{brIR,brR,brG,brB,brUV};
		this.dTime=dT;	this.resetTime=rT;
		this.x=new long[]{px,py,pz};
		this.temp=Temprature;
		this.p=null;	//represents 0 movement
	}
	public static long disPow2(Ball A,Ball B) //returns the distance in the power of 2
	{
	return (Pow(A.x[0]-B.x[0])+Pow(A.x[1]-B.x[1])+Pow(A.x[2]-B.x[2]));
	}
////////////////////////////////////////////////////////////////////////////////
	public static void diffusion(Ball A, Ball B)
	{
		
	}
	public static void radialForceR(Ball A,Ball B,double F)//same as radial force, F*A=force/R, where A is a constant.
	{ 	 
   	 F*=A.power*B.power;
    	long a=(long)((A.x[0]-B.x[0])*F);
    	long b=(long)((A.x[1]-B.x[1])*F);
    	long c=(long)((A.x[2]-B.x[2])*F);
    	A.p.q[0]+=a*B.mass;	A.p.q[1]+=b*B.mass;	A.p.q[2]+=c*B.mass;
    	B.p.q[0]-=a*A.mass;	B.p.q[1]-=b*A.mass;	B.p.q[2]-=c*A.mass;
	}//~25 computations, together with finding F it is 50.Happens each tick
	
	public static void radialFriction(Ball A,Ball B,double R2)//acts the friction force between 2 Balls
	{//friction force is m1*m2*(v->r)*factor, v is radial
	long RV=Mul(A.p.v[0]-B.p.v[0],A.x[0]-B.x[0])+Mul(A.p.v[1]-B.p.v[1],A.x[1]-B.x[1])+Mul(A.p.v[2]-B.p.v[2],A.x[2]-B.x[2]);
	double k=-(A.viscosity*B.viscosity*RV)/R2;//(v*r*factor)/r^2~=v/r. k should be of order 1 
		long a=(long)((A.x[0]-B.x[0])*k);
		long b=(long)((A.x[1]-B.x[1])*k);
		long c=(long)((A.x[2]-B.x[2])*k);
		A.p.q[0]+=a*B.mass;	A.p.q[1]+=b*B.mass;	A.p.q[2]+=c*B.mass;
		B.p.q[0]-=a*A.mass;	B.p.q[1]-=b*A.mass;	B.p.q[2]-=c*A.mass;
		k*=RV;//RV*k=viscosity*(v->r)*(v->r)*[dt] = 2*heat/m1m2. The heat is divided equally.
		A.temp+=k*B.mass;
		B.temp+=k*A.mass;
	}//~50computations, happens each tick
	
///////////////////////////////////////////////////////////////////////////////////////////
	public long timestep()	//moves the Ball the length it moves this tick, of order 10^-2 L
	{
		p.v[0]+=p.q[0]>>8;	p.v[1]+=p.q[1]>>8;	p.v[2]+=p.q[2]>>8;
		x[0]+=p.v[0]>>8;	x[1]+=p.v[1]>>8;	x[2]+=p.v[2]>>8;
		stepKind[this.type].resetQ(this);
		this.dTime--;
		if(this.dTime==0)
			return(stepKind[this.type].Click(this));
		return 0;
	}//positive or 0 values return light output. negatives: -1 is delete,
	public static Ball collide(Ball A,Ball B)
	{
		long r2=disPow2(A,B);
		if(r2<sumRad2[A.type][B.type])
			return(collideKind[A.type][B.type].react(A,B,r2));
		else
			return null;
	}//collide may change all parameters except x and p.v and add a ball but not destroy.
	public static Ball collideKS(Ball A,Ball B)
	{
		long r2=disPow2(A,B);
		if(r2<sumRad2[A.type][B.type])
			return(collideKind[A.type][B.type].reactKS(A,B,r2));
		else
			return null;
	}//in KingSlave B is not moving

	public void Awaken()
	{
		if(p!=null)
			p=new Movement();
	}
	public void Asleep()
	{
		p=null;
	}
	
	public short getType()
	{
		return this.type;
	}
	public short getPower()
	{
		return this.power;
	}
	public long getPosX()
	{
		return x[0];
	}
	public long getPosY()
{
	return x[1];
	}
	public long getPosZ()
	{
		return x[2];
}
	public static void main(String args[])
	{
		long a=1,b=1;
		a<<=32;	b<<=32;
		System.out.println(Form(a)+"*"+Form(b)+" = "+Form(Mul(a,b)));
	}
}




