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

public class LevelSix extends Level 
{
	private static final long serialVersionUID = 8708508151341709791L;
	
	LevelSix(boolean showMenu, int scheme)
	{
		super(showMenu, scheme);
	}
	
	protected void goNextLevel()
	{
		stop();
 		setVisible(false);
 		
 		Level next = new LevelSeven(this.showStartMenu, Level.scheme);
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
		Rectangle floorRect = new Rectangle(5.00, 2.00);
		GameObject goal_floor = new GameObject();
		goal_floor.addFixture(new BodyFixture(floorRect));
		goal_floor.setMass(MassType.INFINITE);
		goal_floor.translate(6.5, 6.5);
		goal_floor.setAsPermanent(this);
		goal_floor.setColor(Color.ORANGE);
		goal = goal_floor;
		world.addBody(goal);
		
		GameObject d1 = new GameObject();
		d1.addFixture(Geometry.createTriangle(new Vector2(-9.00, 6.50), new Vector2(-9.00, 4.00), new Vector2(0.00, 4.00)));
		d1.setMass(MassType.INFINITE);
		d1.setColor(Color.BLACK);
		d1.setAsPermanent(this);
		world.addBody(d1);
		
		Rectangle p1 = new Rectangle(6.00, 1.00);
		GameObject platform1 = new GameObject();
		platform1.addFixture(new BodyFixture(p1));
		platform1.setMass(MassType.INFINITE);
		platform1.translate(-3.00, -0.50);
		platform1.setAsPermanent(this);
		platform1.setColor(Color.BLACK);
		world.addBody(platform1);
		
		Rectangle w1 = new Rectangle(4.00, 1.00);
		GameObject wall1 = new GameObject();
		wall1.addFixture(new BodyFixture(w1));
		wall1.setMass(MassType.INFINITE);
		wall1.translate(1.00, -6.5);
		wall1.setAsPermanent(this);
		wall1.setColor(Color.BLACK);
		world.addBody(wall1);
		
		Rectangle w2 = new Rectangle(1.00, 14.00);
		GameObject wall2 = new GameObject();
		wall2.addFixture(new BodyFixture(w2));
		wall2.setMass(MassType.INFINITE);
		wall2.translate(9.50, 0.0);
		wall2.setAsPermanent(this);
		wall2.setColor(Color.BLACK);
		world.addBody(wall2);
		
		Rectangle b1 = new Rectangle(2.00, 2.00);
		BoosterBlock booster1  = new BoosterBlock('n');
		booster1.addFixture(new BodyFixture(b1));
		booster1.setMass(MassType.INFINITE);
		booster1.translate(4.00, -6.00);
		booster1.setColor(Color.CYAN);
		world.addBody(booster1);
		
		Rectangle b2 = new Rectangle(2.00, 2.00);
		BoosterBlock booster2  = new BoosterBlock('n');
		booster2.addFixture(new BodyFixture(b2));
		booster2.setMass(MassType.INFINITE);
		booster2.translate(-8.00, -5.00);
		booster2.setColor(Color.CYAN);
		world.addBody(booster2);
		
		BoosterBlock booster3  = new BoosterBlock('u');
		booster3.addFixture(new BodyFixture(b2));
		booster3.setMass(MassType.INFINITE);
		booster3.translate(8.00, -6.00);
		booster3.setColor(Color.CYAN);
		world.addBody(booster3);
		
		BoosterBlock booster4  = new BoosterBlock('u');
		booster4.addFixture(new BodyFixture(b2));
		booster4.setMass(MassType.INFINITE);
		booster4.translate(8.00, -1.00);
		booster4.setColor(Color.CYAN);
		world.addBody(booster4);
		
		/*  Create the player, a circle  */
		Circle cirShape = new Circle(0.6);
		GameObject worldplayer = new GameObject();
		worldplayer.addFixture(cirShape);
		worldplayer.setMass(MassType.NORMAL);
		worldplayer.translate(-8.0, 7.0);
		worldplayer.setAsPermanent(this);
		Color playercolor = new Color(244 / 255.0f, 66 / 255.0f, 66 / 255.0f);
		worldplayer.setColor(playercolor);
		player = worldplayer;
		world.addBody(player);
		
		GameObject rightTri2 = new GameObject();
		rightTri2.addFixture(Geometry.createRightTriangle(4.0, 5.0, true));
		rightTri2.setMass(MassType.INFINITE);
		rightTri2.setAsPermanent(this);
		rightTri2.setColor(Color.BLACK);
		rightTri2.translate(2.6, 1.75);
		world.addBody(rightTri2);
		
		GameObject rightTri3 = new GameObject();
		rightTri3.addFixture(Geometry.createRightTriangle(3.0, 2.0));
		rightTri3.setMass(MassType.NORMAL);
		rightTri3.translate(-8.25, -3.5);
		world.addBody(rightTri3);
		
		Rectangle s1 = new Rectangle(2.00, 2.00);
		GameObject standard1 = new GameObject();
		standard1.addFixture(s1);
		standard1.setMass(MassType.NORMAL);
		standard1.translate(5.5, -3.00);
		world.addBody(standard1);
		
		Rectangle platform2 = new Rectangle(1.00, 1.00);
		GameObject p2 = new GameObject();
		p2.addFixture(platform2);
		p2.setMass(MassType.INFINITE);
		p2.setAsPermanent(this);
		p2.setColor(Color.BLACK);
		p2.translate(-9.5, -3.50);
		world.addBody(p2);
		
		Rectangle standard3 = new Rectangle(1.00, 2.00);
		GameObject s3 = new GameObject();
		s3.addFixture(standard3);
		s3.setMass(MassType.NORMAL);
		s3.translate(-5.5, 1.0);
		world.addBody(s3);
		
		GameObject s4 = new GameObject();
		s4.addFixture(standard3);
		s4.setMass(MassType.NORMAL);
		s4.translate(-1.5, 1.0);
		world.addBody(s4);
		
		Rectangle standard5 = new Rectangle(5.00, 1.00);
		GameObject s5 = new GameObject();
		s5.addFixture(standard5);
		s5.setMass(MassType.NORMAL);
		s5.translate(-3.5, 2.50);
		world.addBody(s5);
		
		Circle cirShape2 = new Circle(0.5);
		GameObject ball = new GameObject();
		ball.addFixture(cirShape2);
		ball.setMass(MassType.NORMAL);
		ball.translate(-6.0, 6.0);
		world.addBody(ball);
		
		GameObject ball2 = new GameObject();
		ball2.addFixture(cirShape2);
		ball2.setMass(MassType.NORMAL);
		ball2.translate(-5.0, 5.5);
		world.addBody(ball2);
		
		Rectangle standard6 = new Rectangle(1.00, 4.00);
		GameObject s6 = new GameObject();
		s6.addFixture(standard6);
		s6.setMass(MassType.NORMAL);
		s6.translate(0.5, -4.0);
		world.addBody(s6);
		
		GameObject s7 = new GameObject();
		s7.addFixture(standard6);
		s7.setMass(MassType.NORMAL);
		s7.translate(1.5, -4.0);
		world.addBody(s7);
		
		GameObject standard8 = new GameObject();
		standard8.addFixture(s1);
		standard8.setMass(MassType.NORMAL);
		standard8.translate(1.0, -1.0);
		world.addBody(standard8);
		
		GameObject rightTri4 = new GameObject();
		rightTri4.addFixture(Geometry.createRightTriangle(1.0, 4.0, true));
		rightTri4.setMass(MassType.NORMAL);
		rightTri4.translate(0.5, -4.0);
		world.addBody(rightTri4);
		
		GameObject rightTri5 = new GameObject();
		rightTri5.addFixture(Geometry.createRightTriangle(1.0, 4.0));
		rightTri5.setMass(MassType.NORMAL);
		rightTri5.translate(2.5, -4.0);
		world.addBody(rightTri5);
		
		/*GameObject ball3 = new GameObject();
		ball3.addFixture(cirShape2);
		ball3.setMass(MassType.INFINITE);
		ball3.translate(5.0, 0.0);
		world.addBody(ball3);*/
		
		GameObject ball4 = new GameObject();
		ball4.addFixture(cirShape2);
		ball4.setMass(MassType.INFINITE);
		ball4.translate(8.0, 2.0);
		world.addBody(ball4);
		
		/*GameObject ball5 = new GameObject();
		ball5.addFixture(cirShape2);
		ball5.setMass(MassType.INFINITE);
		ball5.translate(5.0, 3.0);
		world.addBody(ball5);*/
		
		GameObject ball6 = new GameObject();
		ball6.addFixture(cirShape2);
		ball6.setMass(MassType.INFINITE);
		ball6.translate(8.0, 4.0);
		world.addBody(ball6);
		
		/*  attach the contact listener  */
		world.addListener(new WinDetection());
	}
	
	public static void main(String[] args) 
	{
		LevelSix level6 = new LevelSix(Boolean.parseBoolean(args[0]),Integer.parseInt(args[1]));
		//LevelSix level6 = new LevelSix(false, 0);
		level6.setVisible(true);
		level6.start();
	}
}
