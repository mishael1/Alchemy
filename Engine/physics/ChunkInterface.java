package physics;

public interface ChunkInterface {
	void insertBall(Ball A);	//inserts a new ball to chunk
	void moveStep();			//Moves all the balls
	void collideStep();			//Collides all the balls;
	void putToSleep();			//Checks which cells needs to go to sleep and puts them to sleep
	void lighting();			//Handles the lighting
	
	

}
