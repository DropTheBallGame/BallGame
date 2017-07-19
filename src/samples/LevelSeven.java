package samples;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.dyn4j.collision.AxisAlignedBounds;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.contact.ContactAdapter;
import org.dyn4j.dynamics.contact.ContactConstraintId;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.ContactPointId;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;


public class LevelSeven extends Level 
{
	private static final long serialVersionUID = 8708503331341709791L;
	
	LevelSeven(boolean showMenu, int scheme)
	{
		super(showMenu, scheme);
	}
	
	protected void goNextLevel()
	{
		stop();
 		setVisible(false);
 		//Level next = new Level();
 		//next.start();
 		Level.main(null);
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
	
	protected class WinDetection extends ContactAdapter 
	{ 
		@Override
		public boolean begin(ContactPoint point) 
		{
			/*  ContactPointID contains the ContactConstraintID  */
			ContactPointId id = point.getId();
			/*  ContactConstraintID contains the information we need such as BodyIDs  */
			ContactConstraintId thegoodstuff = id.getContactConstraintId();
			
			UUID uuid = UUID.randomUUID();
			contact_ids.put(id, uuid);
			
			/*  If the two bodies that contact are the player and goal  */
			if(goal != null && player != null && ((thegoodstuff.getBody1Id() == goal.getId() && thegoodstuff.getBody2Id() == player.getId()) || 
					(thegoodstuff.getBody1Id() == player.getId() && thegoodstuff.getBody2Id() == goal.getId())))
			{
				//System.out.println("The ball touched the thing!" + permanent_count + " -> " + game_bodies.size());
				
				/*  Player just lost the game, reset world  */
				if(game_bodies.size() > permanent_count)
				{
					//stop();
					reset = true;
					JOptionPane.showMessageDialog(null, "You failed the level!\nPress 'OK' to try again", "You Lose!", JOptionPane.INFORMATION_MESSAGE);
					//System.out.println("Bruh you made contact before all the shit was gone!");
					//reset = true;
					//start();
				}
				/*  Player wins the game  */
				else
				{	
					JOptionPane.showMessageDialog(null, "You have officially beat our game!\nThanks for playing, now get the fuck out!", "WAY TO GO!", JOptionPane.INFORMATION_MESSAGE);
					game_over = true;
				}
			}
			//If player ball touches a boost block, we're gonna fling them in the blocks direction
			else if(goal != null && player != null && ((thegoodstuff.getBody1Id() == player.getId() && boost_blocks.contains(thegoodstuff.getBody2Id()))
				|| (thegoodstuff.getBody2Id() == player.getId() && boost_blocks.contains(thegoodstuff.getBody1Id()))))
			{
				for(Body b : game_bodies)
				{
					if(((GameObject) b).booster == true && 
						(b.getId() == thegoodstuff.getBody1Id() || b.getId() == thegoodstuff.getBody2Id()))
					{
						//System.out.println("BOOST ME SCOTTY! -> " + player.getForce());
						
						switch(((BoosterBlock) b).getDirection())
						{
							case 'r':	
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(300.0, 0.0));
								break;
							case 'l':
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(-300.0, 0.0));
								break;
							case 'u':
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(0.0, 300.0));
								break;
							case 'd':
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(0.0, -300.0));
								break;
							case 'n':
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(300.0, 300.0));
								break;
							case 's':
								player.clearAccumulatedForce();
								player.applyForce(new Vector2(0.0, 3500.0));
								break;							
						}
						return false;
					}
				
				}
			}
			
			return true;
		}
	}
	
	/*  Pretty Much the only function you need to overload  */
	protected void initializeWorld() 
	{
		//Let's fucking do it already
		bounds = new AxisAlignedBounds(25, 25);
		world = new World(bounds);
		permanent_count = 0;
		
		/*  Create the goal, a rectangle  */
		Rectangle floorRect = new Rectangle(4.00, 2.00);
		GameObject goal_floor = new GameObject();
		goal_floor.addFixture(new BodyFixture(floorRect));
		goal_floor.setMass(MassType.INFINITE);
		goal_floor.translate(7.0, -6.0);
		goal_floor.setAsPermanent(this);
		goal_floor.setColor(Color.ORANGE);
		goal = goal_floor;
		world.addBody(goal);		
		
		/*  Create the player, a circle  */
		Circle cirShape = new Circle(0.6);
		GameObject worldplayer = new GameObject();
		worldplayer.addFixture(cirShape);
		worldplayer.setMass(MassType.NORMAL);
		worldplayer.translate(0.0, 13.0);
		worldplayer.setAsPermanent(this);
		Color playercolor = new Color(244 / 255.0f, 66 / 255.0f, 66 / 255.0f);
		worldplayer.setColor(playercolor);
		player = worldplayer;
		world.addBody(player);
		
		Rectangle platform1 = new Rectangle(5.00, 1.00);
		GameObject p1 = new GameObject();
		p1.addFixture(new BodyFixture(platform1));
		p1.setMass(MassType.INFINITE);
		p1.translate(0.50, 2.5);
		p1.setAsPermanent(this);
		p1.setColor(Color.BLACK);
		world.addBody(p1);
		
		GameObject p2 = new GameObject();
		p2.addFixture(new BodyFixture(platform1));
		p2.setMass(MassType.INFINITE);
		p2.translate(6.50, -1.5);
		p2.setAsPermanent(this);
		p2.setColor(Color.BLACK);
		world.addBody(p2);
		
		Rectangle platform2 = new Rectangle(3.00, 1.00);
		GameObject p3 = new GameObject();
		p3.addFixture(new BodyFixture(platform2));
		p3.setMass(MassType.INFINITE);
		p3.translate(4.50, 3.5);
		p3.setAsPermanent(this);
		p3.setColor(Color.BLACK);
		world.addBody(p3);
		
		GameObject p4 = new GameObject();
		p4.addFixture(new BodyFixture(platform2));
		p4.setMass(MassType.INFINITE);
		p4.translate(3.50, -7.5);
		p4.setAsPermanent(this);
		p4.setColor(Color.BLACK);
		world.addBody(p4);
		
		Rectangle platform3 = new Rectangle(1.00, 3.00);
		GameObject p5 = new GameObject();
		p5.addFixture(new BodyFixture(platform3));
		p5.setMass(MassType.INFINITE);
		p5.translate(1.50, 0.5);
		p5.setAsPermanent(this);
		p5.setColor(Color.BLACK);
		world.addBody(p5);
		
		Rectangle platform4 = new Rectangle(2.00, 6.00);
		GameObject p6 = new GameObject();
		p6.addFixture(new BodyFixture(platform4));
		p6.setMass(MassType.INFINITE);
		p6.translate(-5.0, -1.0);
		p6.setAsPermanent(this);
		p6.setColor(Color.BLACK);
		world.addBody(p6);
		
		Rectangle platform5 = new Rectangle(1.00, 12.00);
		GameObject p7 = new GameObject();
		p7.addFixture(new BodyFixture(platform5));
		p7.setMass(MassType.INFINITE);
		p7.translate(-9.50, -3.0);
		p7.setAsPermanent(this);
		p7.setColor(Color.BLACK);
		world.addBody(p7);
		
		Rectangle platform6 = new Rectangle(8.00, 1.00);
		GameObject p8 = new GameObject();
		p8.addFixture(new BodyFixture(platform6));
		p8.setMass(MassType.INFINITE);
		p8.translate(-5.0, -6.50);
		p8.setAsPermanent(this);
		p8.setColor(Color.BLACK);
		world.addBody(p8);
		
		Rectangle block2 = new Rectangle(2.0, 1.0);
		GameObject p9 = new GameObject();
		p9.addFixture(block2);
		p9.translate(-3.0, 1.5);
		p9.setColor(Color.BLACK);
		p9.setAsPermanent(this);
		world.addBody(p9);
		
		GameObject tri1 = new GameObject();
		tri1.addFixture(new BodyFixture(Geometry.createRightTriangle(1.0, 2.0)));
		tri1.translate(6.30, 2.65);
		tri1.setAsPermanent(this);
		tri1.setColor(Color.BLACK);
		world.addBody(tri1);
		
		GameObject tri2 = new GameObject();
		tri2.addFixture(new BodyFixture(Geometry.createRightTriangle(3.0, 4.0)));
		tri2.translate(0.0, -5.5);
		tri2.setAsPermanent(this);
		tri2.setColor(Color.BLACK);
		world.addBody(tri2);
		
		Rectangle block1 = new Rectangle(2.0, 2.0);
		BoosterBlock b1 = new BoosterBlock('r');
		b1.addFixture(block1);
		b1.translate(8.0, 1.0);
		b1.setColor(Color.CYAN);
		world.addBody(b1);
		
		BoosterBlock b2 = new BoosterBlock('s');
		b2.addFixture(block2);
		b2.translate(-2.0, -5.5);
		b2.setColor(Color.CYAN);
		world.addBody(b2);
		
		for(int i = 0; i < 3; i++)
		{
			Rectangle standard1 = new Rectangle(1.00, 2.00);
			GameObject s1 = new GameObject();
			s1.addFixture(new BodyFixture(standard1));
			s1.setMass(MassType.NORMAL);
			s1.translate(2.50 + i, -6.0);
			world.addBody(s1);
		}
		
		for(int i = 0; i < 2; i++)
		{
			GameObject s2 = new GameObject();
			s2.addFixture(block1);
			s2.setMass(MassType.NORMAL);
			s2.translate(5.0, 0.0 + 2*i);
			world.addBody(s2);
		}
		
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				Rectangle standard2 = new Rectangle(1.0, 1.0);
				GameObject s3 = new GameObject();
				s3.addFixture(standard2);
				s3.setMass(MassType.NORMAL);
				s3.translate(3.50 + j, 5.50 - i);
				world.addBody(s3);
			}
		}
		
		Rectangle standard1 = new Rectangle(1.00, 2.00);
		GameObject s4 = new GameObject();
		s4.addFixture(new BodyFixture(standard1));
		s4.setMass(MassType.NORMAL);
		s4.translate(-1.50, 4.0);
		world.addBody(s4);
		
		GameObject s5 = new GameObject();
		s5.addFixture(new BodyFixture(standard1));
		s5.setMass(MassType.NORMAL);
		s5.translate(1.50, 4.0);
		world.addBody(s5);
		
		Rectangle standard2 = new Rectangle(4.00, 1.00);
		GameObject s6 = new GameObject();
		s6.addFixture(new BodyFixture(standard2));
		s6.setMass(MassType.NORMAL);
		s6.translate(0.0, 5.50);
		world.addBody(s6);
		
		Rectangle standard3 = new Rectangle(1.00, 1.00);
		GameObject s7 = new GameObject();
		s7.addFixture(new BodyFixture(standard3));
		s7.setMass(MassType.NORMAL);
		s7.translate(2.50, 3.0);
		world.addBody(s7);
		
		GameObject tri3 = new GameObject();
		tri3.addFixture(new BodyFixture(Geometry.createRightTriangle(3.0, 3.0)));
		tri3.translate(-8.0, -5.0);
		world.addBody(tri3);
		
		GameObject s8 = new GameObject();
		s8.addFixture(new BodyFixture(block1));
		s8.setMass(MassType.NORMAL);
		s8.translate(-5.0, 3.0);
		world.addBody(s8);
		
		Rectangle standard4 = new Rectangle(2.0, 1.0);
		GameObject s9 = new GameObject();
		s9.addFixture(new BodyFixture(standard4));
		s9.setMass(MassType.NORMAL);
		s9.translate(-5.0, 4.5);
		world.addBody(s9);
		
		for(int i = 0; i < 7; i++)
		{
			Rectangle standard10 = new Rectangle(2.00, 1.00);
			GameObject s1 = new GameObject();
			s1.addFixture(new BodyFixture(standard10));
			s1.setMass(MassType.NORMAL);
			s1.translate(2.50 + i, -3.0);
			world.addBody(s1);
		}
		
		Circle cirShape2 = new Circle(1.5);
		GameObject ball1 = new GameObject();
		ball1.addFixture(cirShape2);
		ball1.setMass(MassType.NORMAL);
		ball1.translate(-7.5, -1.5);
		world.addBody(ball1);
		
		GameObject ball2 = new GameObject();
		ball2.addFixture(cirShape2);
		ball2.setMass(MassType.NORMAL);
		ball2.translate(-7.5, 1.5);
		world.addBody(ball2);
		
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				Circle cirShape3 = new Circle(0.5);
				GameObject ball3 = new GameObject();
				ball3.addFixture(cirShape3);
				ball3.setMass(MassType.NORMAL);
				ball3.translate(-7.5, 5.5);
				world.addBody(ball3);
			}
		}
		
		/*  attach the contact listener  */
		world.addListener(new WinDetection());
	}
	
	public static void main(String[] args) 
	{
		LevelSeven level7 = new LevelSeven(Boolean.parseBoolean(args[0]),Integer.parseInt(args[1]));
		//LevelSeven level7 = new LevelSeven(false, 0);
		level7.setVisible(true);
		level7.start();
	}
}
