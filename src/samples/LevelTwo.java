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
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;


public class LevelTwo extends Level 
{
	private static final long serialVersionUID = 8708508151341709791L;
	
	LevelTwo(boolean showMenu, int scheme)
	{
		super(showMenu, scheme);
	}
	
	protected void goNextLevel()
	{
		stop();
 		setVisible(false);
 		
 		Level next = new LevelThree(this.showStartMenu, Level.scheme);
 		next.start();
		// LevelOne.main(new String[] {((Boolean)this.showStartMenu).toString(), ((Integer)Level.scheme).toString()});
	}
	
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
     		goNextLevel();
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
		Rectangle floorRect = new Rectangle(7.00, 2.00);
		GameObject goal_floor = new GameObject();
		goal_floor.addFixture(new BodyFixture(floorRect));
		goal_floor.setMass(MassType.INFINITE);
		goal_floor.translate(5.5, -6.0);
		goal_floor.setAsPermanent(this);
		goal_floor.setColor(Color.ORANGE);
		goal = goal_floor;
		world.addBody(goal);
		
		Rectangle p1 = new Rectangle(4.00, 2.00);
		GameObject platform1 = new GameObject();
		platform1.addFixture(new BodyFixture(p1));
		platform1.setMass(MassType.INFINITE);
		platform1.translate(7.00, 3.00);
		platform1.setAsPermanent(this);
		platform1.setColor(Color.BLACK);
		world.addBody(platform1);
		
		Rectangle w1 = new Rectangle(2.00, 5.00);
		GameObject wall1 = new GameObject();
		wall1.addFixture(new BodyFixture(w1));
		wall1.setMass(MassType.INFINITE);
		wall1.translate(8.00, -2.5);
		wall1.setAsPermanent(this);
		wall1.setColor(Color.BLACK);
		world.addBody(wall1);
		
		Rectangle b1 = new Rectangle(2.00, 2.00);
		BoosterBlock booster1  = new BoosterBlock('l');
		booster1.addFixture(new BodyFixture(b1));
		booster1.setMass(MassType.INFINITE);
		booster1.translate(4.00, 3.00);
		booster1.setColor(Color.CYAN);
		world.addBody(booster1);
		
		Rectangle b2 = new Rectangle(2.00, 2.00);
		BoosterBlock booster2  = new BoosterBlock('r');
		booster2.addFixture(new BodyFixture(b2));
		booster2.setMass(MassType.INFINITE);
		booster2.translate(-8.00, 4.00);
		booster2.setColor(Color.CYAN);
		world.addBody(booster2);
		
		/*  Create the player, a circle  */
		Circle cirShape = new Circle(0.6);
		GameObject worldplayer = new GameObject();
		worldplayer.addFixture(cirShape);
		worldplayer.setMass(MassType.NORMAL);
		worldplayer.translate(7.0, 13.0);
		worldplayer.setAsPermanent(this);
		worldplayer.setLinearDamping(0.05);
		Color playercolor = new Color(244 / 255.0f, 66 / 255.0f, 66 / 255.0f);
		worldplayer.setColor(playercolor);
		player = worldplayer;
		world.addBody(player);
		
		GameObject rightTri = new GameObject();
		rightTri.addFixture(Geometry.createRightTriangle(4.0, 1.0, true));
		rightTri.setMass(MassType.NORMAL);
		rightTri.translate(7.5, 5.0);
		world.addBody(rightTri);
		
		Rectangle s1 = new Rectangle(2.00, 2.00);
		GameObject standard1 = new GameObject();
		standard1.addFixture(s1);
		standard1.setMass(MassType.NORMAL);
		standard1.translate(4.00, 5.00);
		world.addBody(standard1);
		
		/*  attach the contact listener  */
		world.addListener(new WinDetection());
	}
	
	public static void main(String[] args) 
	{
		LevelTwo level2 = new LevelTwo(Boolean.parseBoolean(args[0]),Integer.parseInt(args[1]));
		level2.setVisible(true);
		level2.start();
	}
}
