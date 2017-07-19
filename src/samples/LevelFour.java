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


public class LevelFour extends Level 
{
	private static final long serialVersionUID = 8708508151341709791L;
	
	LevelFour(boolean showMenu, int scheme)
	{
		super(showMenu, scheme);
	}
	
	protected void goNextLevel()
	{
		stop();
 		setVisible(false);
 		
 		Level next = new LevelFive(this.showStartMenu, Level.scheme);
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
		goal_floor.translate(8.5, -6.0);
		goal_floor.setAsPermanent(this);
		goal_floor.setColor(Color.ORANGE);
		goal = goal_floor;
		world.addBody(goal);
		
		Rectangle p1 = new Rectangle(3.00, 1.00);
		GameObject platform1 = new GameObject();
		platform1.addFixture(new BodyFixture(p1));
		platform1.setMass(MassType.INFINITE);
		platform1.translate(2.50, -1.50);
		platform1.setAsPermanent(this);
		platform1.setColor(Color.BLACK);
		world.addBody(platform1);
		
		Rectangle w1 = new Rectangle(2.00, 6.00);
		GameObject wall1 = new GameObject();
		wall1.addFixture(new BodyFixture(w1));
		wall1.setMass(MassType.INFINITE);
		wall1.translate(4.0, -4.0);
		wall1.setAsPermanent(this);
		wall1.setColor(Color.BLACK);
		world.addBody(wall1);
		
		GameObject t1 = new GameObject();
		t1.addFixture(Geometry.createRightTriangle(3.00, 2.00));
		t1.setMass(MassType.NORMAL);
		t1.translate(2.0, -1.0);
		world.addBody(t1);
		
		Rectangle p2 = new Rectangle(3.00, 1.00);
		GameObject platform2 = new GameObject();
		platform2.addFixture(new BodyFixture(p2));
		platform2.setMass(MassType.INFINITE);
		platform2.translate(2.50, -1.50);
		platform2.setAsPermanent(this);
		platform2.setColor(Color.BLACK);
		world.addBody(platform2);
		
		Rectangle standard1 = new Rectangle(1.00, 1.00);
		GameObject s1 = new GameObject();
		s1.addFixture(new BodyFixture(standard1));
		s1.setMass(MassType.NORMAL);
		s1.translate(4.50, 0.0);
		world.addBody(s1);
		
		Rectangle b1 = new Rectangle(3.00, 3.00);
		BoosterBlock booster1  = new BoosterBlock('n');
		booster1.addFixture(new BodyFixture(b1));
		booster1.setMass(MassType.INFINITE);
		booster1.translate(-2.50, 0.50);
		booster1.setColor(Color.CYAN);
		world.addBody(booster1);
		
		Rectangle b2 = new Rectangle(3.00, 3.00);
		BoosterBlock booster2  = new BoosterBlock('u');
		booster2.addFixture(new BodyFixture(b2));
		booster2.setMass(MassType.INFINITE);
		booster2.translate(-3.50, -6.50);
		booster2.setColor(Color.CYAN);
		world.addBody(booster2);
		
		/*  Create the player, a circle  */
		Circle cirShape = new Circle(0.6);
		GameObject worldplayer = new GameObject();
		worldplayer.addFixture(cirShape);
		worldplayer.setMass(MassType.NORMAL);
		worldplayer.translate(-8.5, 13.0);
		worldplayer.setAsPermanent(this);
		Color playercolor = new Color(244 / 255.0f, 66 / 255.0f, 66 / 255.0f);
		worldplayer.setColor(playercolor);
		player = worldplayer;
		world.addBody(player);
		
		GameObject rightTri = new GameObject();
		rightTri.addFixture(Geometry.createRightTriangle(3.0, 5.0));
		rightTri.setMass(MassType.INFINITE);
		rightTri.setColor(Color.BLACK);
		rightTri.setAsPermanent(this);
		rightTri.translate(-8.0, -2.375);
		world.addBody(rightTri);
		
		GameObject d1 = new GameObject();
		d1.addFixture(Geometry.createTriangle(new Vector2(9.00, 7.00), new Vector2(4.00, 7.00), new Vector2(9.00, 2.00)));
		d1.setMass(MassType.INFINITE);
		d1.setColor(Color.BLACK);
		d1.setAsPermanent(this);
		world.addBody(d1);
		
		GameObject d2 = new GameObject();
		d2.addFixture(Geometry.createTriangle(new Vector2(4.0, 2.0), new Vector2(1.0, 2.0), new Vector2(4.0, 0.0)));
		d2.setMass(MassType.NORMAL);
		world.addBody(d2);
		
		Rectangle s2 = new Rectangle(3.00, 1.00);
		GameObject standard2 = new GameObject();
		standard2.addFixture(s2);
		standard2.setMass(MassType.NORMAL);
		standard2.translate(-4.50, -5.50);
		world.addBody(standard2);
		
		Rectangle standard3 = new Rectangle(3.0, 1.0);
		GameObject s3 = new GameObject();
		s3.addFixture(new BodyFixture(standard3));
		s3.setMass(MassType.NORMAL);
		s3.translate(2.50, 2.50);
		world.addBody(s3);
		
		GameObject s4 = new GameObject();
		s4.addFixture(new BodyFixture(standard3));
		s4.setMass(MassType.NORMAL);
		s4.translate(2.50, 3.50);
		world.addBody(s4);
		
		GameObject s5 = new GameObject();
		s5.addFixture(new BodyFixture(standard1));
		s5.setMass(MassType.INFINITE);
		s5.translate(0.50, -1.0);
		s5.setColor(Color.BLACK);
		s5.setAsPermanent(this);
		world.addBody(s5);
		
		/*  attach the contact listener  */
		world.addListener(new WinDetection());
	}
	
	public static void main(String[] args) 
	{
		LevelFour level4 = new LevelFour(Boolean.parseBoolean(args[0]),Integer.parseInt(args[1]));
		//LevelFour level4 = new LevelFour(false, 0);
		level4.setVisible(true);
		level4.start();
	}
}
