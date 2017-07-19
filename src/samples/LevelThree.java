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


public class LevelThree extends Level 
{
	private static final long serialVersionUID = 8708508151341709791L;
	
	LevelThree(boolean showMenu, int scheme)
	{
		super(showMenu, scheme);
	}
	
	protected void goNextLevel()
	{
		stop();
 		setVisible(false);
 		
 		Level next = new LevelFour(this.showStartMenu, Level.scheme);
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
		Rectangle floorRect = new Rectangle(6.00, 2.00);
		GameObject goal_floor = new GameObject();
		goal_floor.addFixture(new BodyFixture(floorRect));
		goal_floor.setMass(MassType.INFINITE);
		goal_floor.translate(-6.0, -6.0);
		goal_floor.setAsPermanent(this);
		goal_floor.setColor(Color.ORANGE);
		goal = goal_floor;
		world.addBody(goal);
		
		Rectangle floor2 = new Rectangle(6.00, 2.00);
		GameObject f2 = new GameObject();
		f2.addFixture(new BodyFixture(floor2));
		f2.setMass(MassType.INFINITE);
		f2.translate(6.0, -6.0);
		f2.setAsPermanent(this);
		f2.setColor(Color.BLACK);
		world.addBody(f2);
		
		/*  Create the player, a circle  */
		Circle cirShape = new Circle(0.6);
		GameObject worldplayer = new GameObject();
		worldplayer.addFixture(cirShape);
		worldplayer.setMass(MassType.NORMAL);
		worldplayer.translate(0.0, 13.0);
		worldplayer.setAsPermanent(this);
		worldplayer.setLinearDamping(0.05);
		Color playercolor = new Color(244 / 255.0f, 66 / 255.0f, 66 / 255.0f);
		worldplayer.setColor(playercolor);
		player = worldplayer;
		world.addBody(player);
		
		Rectangle s1 = new Rectangle(2.00, 2.00);
		GameObject standard1 = new GameObject();
		standard1.addFixture(s1);
		standard1.setMass(MassType.NORMAL);
		standard1.translate(-4.00, -4.00);
		world.addBody(standard1);
		
		Rectangle s2 = new Rectangle(2.00, 2.00);
		GameObject standard2 = new GameObject();
		standard2.addFixture(s2);
		standard2.setMass(MassType.NORMAL);
		standard2.translate(-4.00, -2.00);
		world.addBody(standard2);
		
		Rectangle s3 = new Rectangle(2.00, 2.00);
		GameObject standard3 = new GameObject();
		standard3.addFixture(s3);
		standard3.setMass(MassType.NORMAL);
		standard3.translate(4.00, -4.00);
		world.addBody(standard3);
		
		Rectangle s4 = new Rectangle(2.00, 2.00);
		GameObject standard4 = new GameObject();
		standard4.addFixture(s4);
		standard4.setMass(MassType.NORMAL);
		standard4.translate(4.00, -2.00);
		world.addBody(standard4);
		
		Rectangle s5 = new Rectangle(10.00, 2.00);
		GameObject standard5 = new GameObject();
		standard5.addFixture(s5);
		standard5.setMass(MassType.NORMAL);
		world.addBody(standard5);
		
		Rectangle s6 = new Rectangle(10.00, 1.00);
		GameObject standard6 = new GameObject();
		standard6.addFixture(s6);
		standard6.translate(0.0, 3.5);
		standard6.setMass(MassType.NORMAL);
		world.addBody(standard6);
		
		Rectangle s7 = new Rectangle(10.00, 1.00);
		GameObject standard7 = new GameObject();
		standard7.addFixture(s7);
		standard7.translate(0.0, 4.5);
		standard7.setMass(MassType.NORMAL);
		world.addBody(standard7);
		
		GameObject t1 = new GameObject();
		t1.addFixture(Geometry.createIsoscelesTriangle(4.0, 2.0));
		t1.setMass(MassType.NORMAL);
		t1.translate(-3.0, 2.0);
		world.addBody(t1);
		
		GameObject t2 = new GameObject();
		t2.addFixture(Geometry.createIsoscelesTriangle(4.0, 2.0));
		t2.setMass(MassType.NORMAL);
		t2.translate(3.0, 2.0);
		world.addBody(t2);
		
		GameObject t3 = new GameObject();
		t3.addFixture(Geometry.createIsoscelesTriangle(2.0, 2.0));
		t3.setMass(MassType.NORMAL);
		t3.translate(0.0, 2.0);
		world.addBody(t3);
		
		/*  attach the contact listener  */
		world.addListener(new WinDetection());
	}
	
	public static void main(String[] args) 
	{
		LevelThree level3 = new LevelThree(Boolean.parseBoolean(args[0]),Integer.parseInt(args[1]));
		level3.setVisible(true);
		level3.start();
	}
}
