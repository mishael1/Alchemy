package physics;

import java.util.ArrayList;

public class Chunk implements ChunkInterface
{	//a chunk of volume
	long[] location;				//the location of the --- corner of the chunk
	int depth;						//how many layers does it contain
	long diameter[];				//the size of each level, progressed 2 at a time
	Chunk[][][] neighbors;			//the neighbors of the chunk
	ArrayList<Ball>[][][][] layeredList;		//[depth][x][y][z][ballNum], in depth=0 there is only one cell
	boolean[][][][] isAwaken;			//[depth][x][y][z], if a cell is awoken all its ancestors are too
	boolean[][][][] neighborsAwaken;	//determines if all the neighbors are awoken.
	public Chunk(int d, long minSize, long x, long y, long z)
	{
		this.location=new long[]{x,y,z};
		depth=d;
		diameter=new long[depth];
		this.layeredList=new ArrayList[depth][][][];
		this.isAwaken=new boolean[depth][][][];
		this.neighborsAwaken=new boolean[depth][][][];
		int length=1;
		for(int i=0;i<d;i++)
		{
			this.layeredList[i]=new ArrayList[length][length][length];
			this.isAwaken[i]=new boolean[length][length][length];
			this.neighborsAwaken[i]=new boolean[length][length][length];
			length*=2;
		}
		for(int i=d-1;i>=0;i++)
		{
			this.diameter[i]=minSize;
			minSize*=2;
		}
		for(int i=0;i<d;i++)
			for(int j=0;j<this.diameter[i];j++)
				for(int k=0;k<this.diameter[i];k++)
					for(int l=0;l<this.diameter[i];l++)
					{
						this.layeredList[i][j][k][l]=new ArrayList<Ball>();
						this.isAwaken[i][j][k][l]=false;
						this.neighborsAwaken[i][j][k][l]=false;
					}
	}
	
	public void insertBall(Ball A)//Inserts a NEW ball to a chunk.
	{
		if(A!=null){
			int d=0;
			for(int i=this.depth-1;i>=0;i--)
				if((Ball.rad[A.getType()]<<1)<diameter[i])
					{
						d=i;	i=-1;
					}
			int a=(int)((A.x[0]-this.location[0])/diameter[d]);
			int b=(int)((A.x[1]-this.location[1])/diameter[d]);
			int c=(int)((A.x[2]-this.location[2])/diameter[d]);
			this.layeredList[d][a][b][c].add(A);
			A.Awaken();
			awakenAll(d,a,b,c);
		}
	}
	
	public void awakenCell(int d,int xp,int yp, int zp)
	{
		if(!isAwaken[d][xp][yp][zp])
		{
			isAwaken[d][xp][yp][zp]=true;
			for(int c=0;c<layeredList[d][xp][yp][zp].size();c++)
				layeredList[d][xp][yp][zp].get(c).Awaken();
		}
	}
	public void awakenAbove(int d,int xp,int yp, int zp)
	{
		if(!isAwaken[d][xp][yp][zp])
		{
			isAwaken[d][xp][yp][zp]=true;
			for(int c=0;c<layeredList[d][xp][yp][zp].size();c++)
				layeredList[d][xp][yp][zp].get(c).Awaken();
			
		}
	}
	public void awakenAll(int d,int xp,int yp,int zp)//Awakens all cells in respect to this cell
	{
		if(!neighborsAwaken[d][xp][yp][zp])
		{
			for(int i=xp-1;i<=xp+1;i++)
				for(int j=yp-1;j<=yp+1;j++)
					for(int k=zp-1;k<=zp+1;k++)
						awakenCell(d,i,j,k);
			int x=2*xp,y=2*yp,z=2*zp,l=1;
			for(int W=d+1;W<depth;W++)
				{
				for(int i=x-l-1;i<x+3l+1;i++)
					for(int j=y-l-1;j<y+3l+1;j++)
						for(int k=z-l-1;k<z+3l+1;k++)
							awakenCell(W,i,j,k);
				x*=2;	y*=2;	z*=2;	l*=2;
				}
			
		}
			
	}

	public void cellCellCollide(int d1,int x1, int y1,int z1,int d2,int x2, int y2,int z2)
	{
		for(int c=0;c<layeredList[d1][x1][y1][z1].size();c++)
			for(int e=0;e<layeredList[d2][x2][y2][z2].size();e++)
					insertBall(Ball.collide(layeredList[d1][x1][y1][z1].get(c),
												layeredList[d2][x2][y2][z2].get(e)));
	}
	public void cellCellCollideKS(int d1,int x1, int y1,int z1,int d2,int x2, int y2,int z2)
	{
		for(int c=0;c<layeredList[d1][x1][y1][z1].size();c++)
			for(int e=0;e<layeredList[d2][x2][y2][z2].size();e++)
					insertBall(Ball.collideKS(layeredList[d1][x1][y1][z1].get(c),
												layeredList[d2][x2][y2][z2].get(e)));
	}
	public void cellCollideAllI(int d,int xp,int yp, int zp)//internal cell:0<xp<max
	{
	if(!layeredList[d][xp][yp][zp].isEmpty()){
		
		for(int c=0;c<layeredList[d][xp][yp][zp].size();c++)//collides in the cell
				for(int e=0;e<c;e++)
						insertBall(Ball.collide(layeredList[d][xp][yp][zp].get(c),
												layeredList[d][xp][yp][zp].get(e)));
		
		//Neighbor cells,dominated
		for(int j=yp-1;j<=yp+1;j++)//x=xp+1
			for(int k=zp-1;k<=zp+1;k++)
				if(!layeredList[d][xp+1][j][k].isEmpty())
				{
					if(isAwaken[d][xp+1][j][k])
						cellCellCollide(d,xp,yp,zp,	d,xp+1,j,k);
					else
						cellCellCollideKS(d,xp,yp,zp,	d,xp+1,j,k);
				}
		for(int k=zp-1;k<=zp+1;k++)//x=xp y=yp+1
			if(!layeredList[d][xp][yp+1][k].isEmpty())
			{
				if(isAwaken[d][xp][yp+1][k])
					cellCellCollide(d,xp,yp,zp,	d,xp,yp+1,k);
				else
					cellCellCollideKS(d,xp,yp,zp,	d,xp,yp+1,k);
			}
		if(!layeredList[d][xp][yp][zp+1].isEmpty())//x=xp,y=yp,z=zp+1
		{
			if(isAwaken[d][xp][yp][zp+1])
				cellCellCollide(d,xp,yp,zp,	d,xp,yp,zp+1);
			else
				cellCellCollideKS(d,xp,yp,zp,	d,xp,yp,zp+1);
		}
		
		//Neighbor cells, dominant. only reacts to the sleeping ones
		for(int j=yp-1;j<=yp+1;j++)//x=xp-1
			for(int k=zp-1;k<=zp+1;k++)
				if(!layeredList[d][xp-1][j][k].isEmpty())
				{
					if(!isAwaken[d][xp-1][j][k])
						cellCellCollideKS(d,xp,yp,zp,	d,xp-1,j,k);
				}
		for(int k=zp-1;k<=zp+1;k++)//x=xp y=yp-1
			if(!layeredList[d][xp][yp-1][k].isEmpty())
			{
				if(!isAwaken[d][xp][yp-1][k])
					cellCellCollideKS(d,xp,yp,zp,	d,xp,yp-1,k);
			}
		if(!layeredList[d][xp][yp][zp-1].isEmpty())//x=xp,y=yp,z=zp-1
		{
			if(!isAwaken[d][xp][yp][zp-1])
				cellCellCollideKS(d,xp,yp,zp,	d,xp,yp,zp-1);
		}
		
		
		if(d!=depth-1)//Cells of lower rank
		{
			int x=2*xp,y=2*yp,z=2*zp,l=1;
			for(int W=d+1;W<depth;W++)
				{
				for(int i=x-l-1;i<x+3l+1;i++)
					for(int j=y-l-1;j<y+3l+1;j++)
						for(int k=z-l-1;k<z+3l+1;k++)
							if(!layeredList[W][i][j][k].isEmpty())
								{	
									if(isAwaken[W][i][j][k])
										cellCellCollide(d,xp,yp,zp,	 W,i,j,k);
									else
										cellCellCollideKS(d,xp,yp,zp,	W,i,j,k);
								}
					x*=2;	y*=2;	z*=2;	l*=2;
				}
			}
		
		}
	}
	public void collideInternal(int d,int x,int y, int z)
	{
			if(this.isAwaken[d][x][y][z])
			{
				cellCollideAllI(d, x, y, z);
				if(d<depth-1)
				{
					d++;	x*=2;	y*=2;	z*=2;
					collideInternal(d,x,y,z);	collideInternal(d,x+1,y,z);
					collideInternal(d,x,y+1,z);	collideInternal(d,x+1,y+1,z);
					collideInternal(d,x,y,z+1);	collideInternal(d,x+1,y,z+1);
					collideInternal(d,x,y+1,z+1);collideInternal(d,x+1,y+1,z+1);
				}
			}
	}
	public void cellStepAllI(int d,int x,int y, int z)
	{
		
	}//time steps all balls in the chunk, inside cell version
	
}
