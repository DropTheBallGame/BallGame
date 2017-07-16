package samples;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;

import org.dyn4j.collision.AxisAlignedBounds;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;


public class LevelOne extends Level 
{
	private static final long serialVersionUID = 8708508151341709791L;
	
	protected void gameLoop() 
	{	
		game_bodies = world.getBodies();
		Graphics2D graphics = (Graphics2D)canvas.getBufferStrategy().getDrawGraphics();
		
		AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
		AffineTransform move = AffineTransform.getTranslateInstance(400, -300);
		graphics.transform(yFlip);
		graphics.transform(move);
		
		render(graphics);
		
		graphics.dispose();
		
		BufferStrategy strategy = canvas.getBufferStrategy();
		
		if(!strategy.contentsLost()) 
		{
			strategy.show();
		}
		
        Toolkit.getDefaultToolkit().sync();
        
		results.clear();
        
     	if (point != null) 
     	{
     		/*  convert from screen space to world space coordinates  */
     		double x =  (point.getX() - canvas.getWidth() / 2.0) / SCALE;
     		double y = -(point.getY() - canvas.getHeight() / 2.0) / SCALE;
     			
     		/*  iterates through all bodies in the world and checks
     		 *  if they are within the mouse click  */
     		for(int i = 0; i < world.getBodyCount(); i++) 
     		{
    				Body b = world.getBody(i);
    				if(b.contains(new Vector2(x, y)) && !((GameObject) b).isPermanent()) 
    				{
    					world.removeBody(b);
						/*	World.setUpdateRequired(true) basically has to get called any time
						 *	you change the physics or world space, such as removing Bodies  */
    					world.setUpdateRequired(true);    				
    				}
    			}
     			/*	Reset point so we don't repeatedly call the same mouse point  */
     			point = null;
     	}
     		
     	for(int i = 0; i < world.getBodyCount(); i++)
 		{
 			Body b = world.getBody(i);
 			if(bounds.isOutside(b) == true && !((GameObject)b).isPermanent())
 			{	
 				//System.out.print("Shit's gone!");
 				world.removeBody(b);
 				world.setUpdateRequired(true);
 			}
 			else if(bounds.isOutside(b) == true && ((GameObject) b).getId() == player.getId())
			{
					reset = true;
			}
 		}
     		
     	if(reset == true)
     	{
     		initializeWorld();
     		reset = false;
     	}
     	
     	if(game_over == true)
		{
     		stop();
     		setVisible(false);
			LevelTwo.main(null);
		}
     	
     	if(restart == true)
     	{
     		stop();
     		setVisible(false);
			Level.main(null);
     	}
       
        long time = System.nanoTime();
        long diff = time - last;
        last = time;
        double elapsedTime = diff / NANO_TO_BASE;
        world.update(elapsedTime);
	}
	
	/*  Pretty Much the only function you need to overload  */
	protected void initializeWorld() 
	{
		bounds = new AxisAlignedBounds(25, 25);
		world = new World(bounds);
		permanent_count = 0;
		
		/*  Create the goal, a rectangle  */
		Rectangle floorRect = new Rectangle(17.75, 1.00);
		GameObject goal_floor = new GameObject();
		goal_floor.addFixture(new BodyFixture(floorRect));
		goal_floor.setMass(MassType.INFINITE);
		goal_floor.translate(0.0, -6.0);
		goal_floor.setAsPermanent(this);
		goal_floor.setColor(Color.ORANGE);
		goal = goal_floor;
		world.addBody(goal);
		
		/*  Create the player, a circle  */
		Circle cirShape = new Circle(0.6);
		GameObject worldplayer = new GameObject();
		worldplayer.addFixture(cirShape);
		worldplayer.setMass(MassType.NORMAL);
		worldplayer.translate(2.0, 13.0);
		worldplayer.setAsPermanent(this);
		Color playercolor = new Color(244 / 255.0f, 66 / 255.0f, 66 / 255.0f);
		worldplayer.setColor(playercolor);
		player = worldplayer;
		world.addBody(player);
		
		/*  Try some squares dude  */
		
		for(int j = 0; j < 5; j++)
		{
			for(int i = 0; i < 5; i++)
			{
				Rectangle rectShape = new Rectangle(2.0, 2.0);
				GameObject rectangle = new GameObject();
				rectangle.addFixture(rectShape);
				rectangle.setMass(MassType.NORMAL);
				rectangle.translate(-4 + j*2.0, i*2.0);
				world.addBody(rectangle);	
			}
		}
		
		/*  attach the contact listener  */
		world.addListener(new WinDetection());
	}
	
	public static void main(String[] args) 
	{
		LevelOne level1 = new LevelOne();
		level1.setVisible(true);
		level1.start();
	}
}
