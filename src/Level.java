package samples;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy; 
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.dynamics.DetectResult;
import org.dyn4j.dynamics.contact.ContactAdapter;
import org.dyn4j.dynamics.contact.ContactConstraintId;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.ContactPointId;
import org.dyn4j.dynamics.contact.PersistedContactPoint;

public class Level extends JFrame 
{
	/*  Generated serialVersionUID  */
	private static final long serialVersionUID = 7489010610517678448L;
	public static final double SCALE = 45.0;
	public static final double NANO_TO_BASE = 1.0e9;
	private Point point;
	
	/*  The picking results  */
	private List<DetectResult> results = new ArrayList<DetectResult>();
	
	protected Canvas canvas;
	protected World world;
	protected boolean stopped;
	protected boolean reset = false;
	protected long last;
	
	/*  A mapping of contact id to UUID  */
	private Map<ContactPointId, UUID> contact_ids = new HashMap<ContactPointId, UUID>();
	
	protected static List<Body> game_bodies = new ArrayList<Body>();
	protected UUID playerID;
	protected UUID goalID;
	
	public static class GameObject extends Body 
	{
		protected Color color;
		
		public GameObject() 
		{
			this.color = new Color(
					(float)Math.random() * 0.5f + 0.5f,
					(float)Math.random() * 0.5f + 0.5f,
					(float)Math.random() * 0.5f + 0.5f);
		}
		
		public void render(Graphics2D fixtureGraphics) 
		{
			AffineTransform original_transformation = fixtureGraphics.getTransform();
			
			AffineTransform new_transformation = new AffineTransform();
			new_transformation.translate(this.transform.getTranslationX() * SCALE, this.transform.getTranslationY() * SCALE);
			new_transformation.rotate(this.transform.getRotation());
			
			fixtureGraphics.transform(new_transformation);

			for (BodyFixture fixture : this.fixtures) 
			{

				Convex convex = fixture.getShape();
				/*  Uses Graphics2DRenderer file from dyn4j example source  */
				Graphics2DRenderer.render(fixtureGraphics, convex, SCALE, color);
			}
			fixtureGraphics.setTransform(original_transformation);
		}
	}
	
	private class WinDetection extends ContactAdapter 
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
			if((thegoodstuff.getBody1Id() == goalID && thegoodstuff.getBody2Id() == playerID) || 
					(thegoodstuff.getBody1Id() == playerID && thegoodstuff.getBody2Id() == goalID))
			{
				System.out.println("The ball touched the thing!");
				
				/*  Player just lost the game, reset world  */
				if(game_bodies.size() > 2)
				{
					System.out.println("Bruh you made contact before all the shit was gone!");
					reset = true;
				}
				/*  Player wins the game  */
				else if(game_bodies.size() == 2)
				{
					System.out.println("You fuckin' Won!!!!");
				}
			}
			
			return true;
		}
		
		@Override
		public void end(ContactPoint point) 
		{
			ContactPointId id = point.getId();
			contact_ids.remove(id);
		}
		
		@Override
		public boolean persist(PersistedContactPoint point) 
		{
			ContactPointId id = point.getId();
			contact_ids.get(id);
			return true;
		}
	}

	
	private final class GameObjectDeleter extends MouseAdapter 
	{
		@Override
		public void mousePressed(MouseEvent e) 
		{
			/*  Store the mouse click position  */
			point = new Point(e.getX(), e.getY());
		}
		
		@Override
		public void mouseReleased(MouseEvent e) 
		{
			point = null;
		}
	}
 
	public Level() 
	{
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		/*  Window Listener for funzies  */
		this.addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				stop();
				super.windowClosing(e);
			}
		});
		
		Dimension size = new Dimension(800, 600);
		
		/* Create a canvas to paint to  */
		this.canvas = new Canvas();
		this.canvas.setPreferredSize(size);
		this.canvas.setMinimumSize(size);
		this.canvas.setMaximumSize(size);
		
		/*  Mouse listener for the best of times  */
		MouseAdapter ml = new GameObjectDeleter();
		this.canvas.addMouseMotionListener(ml);
		this.canvas.addMouseWheelListener(ml);
		this.canvas.addMouseListener(ml);
		
		/*  Add the canvas to the JFrame  */
		this.add(this.canvas);
		
		/*  Make it unresizable and pack it  */
		this.setResizable(false);
		this.pack();
		this.stopped = false;
		
		/*  Create World  */
		this.initializeWorld();
	}

	protected void initializeWorld() 
	{
		this.world = new World();
		
		/*  Create the goal, a rectangle  */
		Rectangle floorRect = new Rectangle(15.0, 1.0);
		GameObject goal_floor = new GameObject();
		goal_floor.addFixture(new BodyFixture(floorRect));
		goal_floor.setMass(MassType.INFINITE);
		goal_floor.translate(0.0, -4.0);
		this.goalID = goal_floor.getId();
		this.world.addBody(goal_floor);
		
		/*  Create the player, a circle  */
		Circle cirShape = new Circle(0.6);
		GameObject player = new GameObject();
		player.addFixture(cirShape);
		player.setMass(MassType.NORMAL);
		player.translate(2.0, 20.0);
		this.playerID = player.getId();
		this.world.addBody(player);
		
		/*  Try some squares dude  */
		for(int i = 0; i < 4; i++)
		{
			Rectangle rectShape = new Rectangle(2.5, 2.5);
			GameObject rectangle = new GameObject();
			rectangle.addFixture(rectShape);
			rectangle.setMass(MassType.NORMAL);
			rectangle.translate(2.0, i*2.0);
			this.world.addBody(rectangle);	
		}
		
		Rectangle rectShape = new Rectangle(2.5, 2.5);
		GameObject rectangle = new GameObject();
		rectangle.addFixture(rectShape);
		rectangle.setMass(MassType.NORMAL);
		rectangle.translate(0.0, 0.0);
		this.world.addBody(rectangle);	
		
		/*  attach the contact listener  */
		this.world.addListener(new WinDetection());
	}
	
	public void start() 
	{
		this.last = System.nanoTime();
		this.canvas.setIgnoreRepaint(true);
		this.canvas.createBufferStrategy(2);
		
		/* run a separate thread to do active rendering
		 * because we don't want to do it on the EDT  */
		Thread thread = new Thread() 
		{
			public void run() 
			{
				/* perform an infinite loop stopped
				 * render as fast as possible  */
				while (!isStopped()) 
				{
					gameLoop();
	
				}
			}
		};
		
		thread.setDaemon(true);
		thread.start();
	}
	
	protected void gameLoop() 
	{	
		game_bodies = this.world.getBodies();
		Graphics2D graphics = (Graphics2D)this.canvas.getBufferStrategy().getDrawGraphics();
		
		AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
		AffineTransform move = AffineTransform.getTranslateInstance(400, -300);
		graphics.transform(yFlip);
		graphics.transform(move);
		
		this.render(graphics);
		
		graphics.dispose();
		
		BufferStrategy strategy = this.canvas.getBufferStrategy();
		
		if (!strategy.contentsLost()) 
		{
			strategy.show();
		}
		
        Toolkit.getDefaultToolkit().sync();
        
		this.results.clear();
        
     		if (this.point != null) 
     		{
     			/*  convert from screen space to world space coordinates  */
     			double x =  (this.point.getX() - this.canvas.getWidth() / 2.0) / SCALE;
     			double y = -(this.point.getY() - this.canvas.getHeight() / 2.0) / SCALE;
     			
     			/*  iterates through all bodies in the world and checks
     			 *  if they are within the mouse click  */
     			for (int i = 0; i < this.world.getBodyCount(); i++) 
     			{
    				Body b = this.world.getBody(i);
    				if (b.contains(new Vector2(x, y)))
    				{
    					this.world.removeBody(b);
						/*	World.setUpdateRequired(true) basically has to get called any time
						 *	you change the physics or world space, such as removing Bodies  */
    					this.world.setUpdateRequired(true);    				
    				}
    			}
     			/*	Reset point so we don't repeatedly call the same mouse point  */
     			this.point = null;
     		}
     		
     	if(this.reset == true)
     	{
     		this.initializeWorld();
     		this.reset = false;
     	}
       
        long time = System.nanoTime();
        long diff = time - this.last;
        this.last = time;
    	double elapsedTime = diff / NANO_TO_BASE;
        this.world.update(elapsedTime);
	}

	protected void render(Graphics2D level_graphics) 
	{
		/*  Fill world with white color to draw objects over  */
		level_graphics.setColor(Color.WHITE);
		level_graphics.fillRect(-400, -300, 800, 600);
		
		/*  Draw all the objects in the world  */
		for (int i = 0; i < this.world.getBodyCount(); i++) 
		{
			GameObject go = (GameObject) this.world.getBody(i);
			go.render(level_graphics);
		}
	}

	public synchronized void stop() 
	{
		this.stopped = true;
	}
	
	public synchronized boolean isStopped() 
	{
		return this.stopped;
	}
	
	public static void main(String[] args) 
	{
		
		Level level0 = new Level();
		level0.setVisible(true);
		level0.start();
	}
}
