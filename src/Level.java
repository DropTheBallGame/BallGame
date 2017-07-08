/** The following was Written by Jordan Harlow with assistance from Richard Hamm
 *  While the code is inspired from several examples from dyn4j.org's examples it is
 *  entirely original code
 *  
 *  This class will serve as the basic template for our levels
 *  By extending Level, we will have access to all the tools to create levels quickly
 *  simply by overloading the initializeWorld() function
 *  
 *       __                      	
 *     _/  |______    ____  ____  
 *     \   __\__  \ _/ ___\/  _ \ 
 *	    |  |  / __ \\  \__(  <_> )
 *	    |__| (____  /\___  >____/  (:
 *                \/     \/   
 **/

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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.*;

import org.dyn4j.collision.AxisAlignedBounds;
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
	protected Point point;
	
	/*  The picking results  */
	protected List<DetectResult> results = new ArrayList<DetectResult>();
	
	class GameCanvas extends Canvas
	{
		private static final long serialVersionUID = -2056634882817701012L;
		
	}
	
	protected GameCanvas canvas;
	protected World world;
	protected AxisAlignedBounds bounds;
	protected boolean stopped;
	protected boolean reset, game_over = false;
	protected long last;
	/*  Only changed by the setAsPermanent Function */
	protected int permanent_count;
	
	/*  A mapping of contact id to UUID  */
	protected Map<ContactPointId, UUID> contact_ids = new HashMap<ContactPointId, UUID>();
	
	protected static List<Body> game_bodies = new ArrayList<Body>();
	protected UUID playerID;
	protected UUID goalID;
	
	protected static int scheme;
	
	public static int randoNum(int s, int l)
	{
		Random randgen = new Random();
		return randgen.nextInt(l - s + 1) + s;
	}
	
	public static class GameObject extends Body 
	{
		protected Color color;
		/*  Permanents are not deletable  */
		protected boolean permanent;
		
		public GameObject() 
		{
			permanent = false;
			
			/*  Set this bitch up to generate random shades of brown 
			 *  I picked absolutely random numbers but it worked out  */
			color = SchemeColor();
		}
		
		public void setColor(Color c)
		{
			color = c;
		}
		
		public void setAsPermanent(Level lev)
		{
			permanent = true;
			lev.permanent_count++;
		}
		
		public boolean isPermanent()
		{
			return permanent;
		}
		
		public void render(Graphics2D fixtureGraphics) 
		{
			AffineTransform original_transformation = fixtureGraphics.getTransform();
			
			AffineTransform new_transformation = new AffineTransform();
			new_transformation.translate(transform.getTranslationX() * SCALE, transform.getTranslationY() * SCALE);
			new_transformation.rotate(transform.getRotation());
			
			fixtureGraphics.transform(new_transformation);

			for(BodyFixture fixture : fixtures) 
			{
				Convex convex = fixture.getShape();
				/*  Uses Graphics2DRenderer file from dyn4j example source  */
				Graphics2DRenderer.render(fixtureGraphics, convex, SCALE, color);
			}
			fixtureGraphics.setTransform(original_transformation);
		}
	}
	
	public static Color SchemeColor()
	{
		Color schemecolor = new Color(
				(float)randoNum(70, 108) / 255.0f,
				(float)randoNum(50, 67) / 255.0f,
				(float)randoNum(15, 35) / 255.0f);
		int seasoncolor = 1;
		
		switch(scheme)
		{
			case 0:  /*  Standard  */
				schemecolor = new Color(
						(float)randoNum(70, 108) / 255.0f,
						(float)randoNum(50, 67) / 255.0f,
						(float)randoNum(15, 35) / 255.0f);
				
				break;
			case 1:  /* Spring  */
				seasoncolor = randoNum(1, 4);
				switch(seasoncolor)
				{
					case 1:
						schemecolor = new Color(
								(float)randoNum(0, 130) / 255.0f,
								(float)randoNum(200, 255) / 255.0f,
								(float)randoNum(0, 130) / 255.0f);
						break;
					case 2:
						int yellowscheme = randoNum(200, 255);
						schemecolor = new Color(
								(float)yellowscheme / 255.0f,
								(float)yellowscheme / 255.0f,
								(float)randoNum(0, 150) / 255.0f);
						break;
					case 3:
						int pinkscheme = randoNum(200, 255);
						schemecolor = new Color(
								(float)pinkscheme / 255.0f,
								(float)randoNum(50, 130) / 255.0f,
								(float)pinkscheme / 255.0f);
						break;
					case 4:
						schemecolor = new Color(
								(float)255 / 255.0f,
								(float)randoNum(150, 225) / 255.0f,
								(float)randoNum(150, 225) / 255.0f);
				}
				break;
			case 2:  /*  Summer  */
				seasoncolor = randoNum(1, 4);
				switch(seasoncolor)
				{
					case 1:
						int goldscheme = randoNum(100, 150);
						schemecolor = new Color(
								(float)goldscheme / 255.0f,
								(float)goldscheme / 255.0f,
								0);
						break;
					case 2:
						schemecolor = new Color(
								(float)randoNum(0, 50) / 255.0f,
								(float)randoNum(50, 100) / 255.0f,
								(float)randoNum(0, 50) / 255.0f);
						break;
					case 3:
						int bluescheme = randoNum(200, 255);
						schemecolor = new Color(
								(float)randoNum(0, 150) / 255.0f,
								(float)bluescheme / 255.0f,
								(float)bluescheme / 255.0f);
						break;
					case 4:
						schemecolor = new Color(
								(float)randoNum(150, 255) / 255.0f,
								0,
								0);
				}
				break;
			case 3:  /*  Autumn  */
				seasoncolor = randoNum(1, 2);
				switch(seasoncolor)
				{
					case 1:
						schemecolor = new Color(
								(float)randoNum(100, 255) / 255.0f,
								(float)randoNum(0, 100) / 255.0f,
								(float)randoNum(0, 60) / 255.0f);		
						break;
					case 2:
						int yellowscheme = randoNum(200, 255);
						schemecolor = new Color(
								(float)yellowscheme / 255.0f,
								(float)yellowscheme / 255.0f,
								(float)randoNum(0, 150) / 255.0f);		
				}
				break;
			case 4:  /*  Winter  */
				seasoncolor = randoNum(1, 4);
				switch(seasoncolor)
				{
					case 1:
						int graycolor = randoNum(0, 255);
						schemecolor = new Color(
								(float)graycolor / 255.0f,
								(float)graycolor / 255.0f,
								(float)graycolor / 255.0f);
						break;
					case 2:
						schemecolor = new Color(
								(float)randoNum(0, 204) / 255.0f,
								(float)randoNum(255, 255) / 255.0f,
								(float)randoNum(204, 255) / 255.0f);
						break;
					case 3:
						schemecolor = new Color(
								0,
								0,
								(float)randoNum(100, 160) / 255.0f);
						break;
					case 4:
						int purplescheme = randoNum(80, 130);
						schemecolor = new Color(
								(float)purplescheme / 255.0f,
								0,
								(float)purplescheme / 255.0f);
				}
		}
		
		return schemecolor;
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
			if((thegoodstuff.getBody1Id() == goalID && thegoodstuff.getBody2Id() == playerID) || 
					(thegoodstuff.getBody1Id() == playerID && thegoodstuff.getBody2Id() == goalID))
			{
				//System.out.println("The ball touched the thing!" + permanent_count + " -> " + game_bodies.size());
				
				/*  Player just lost the game, reset world  */
				if(game_bodies.size() > permanent_count)
				{
					JOptionPane.showMessageDialog(null, "Bruh you made contact before all the shit was gone!", "You Lose!", JOptionPane.INFORMATION_MESSAGE);
					//System.out.println("Bruh you made contact before all the shit was gone!");
					reset = true;
				}
				/*  Player wins the game  */
				else
				{	
					JOptionPane.showMessageDialog(null, "You fuckin' Won!!!!", "You Win!", JOptionPane.INFORMATION_MESSAGE);
					// System.out.println("You fuckin' Won!!!!");
				
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
		/*  Set color scheme to Standard by default  */
		scheme = 0;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		/*  Window Listener for funzies  */
		addWindowListener(new WindowAdapter() 
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
		canvas = new GameCanvas();
		canvas.setPreferredSize(size);
		canvas.setMinimumSize(size);
		canvas.setMaximumSize(size);
		
		/*  Mouse listener for the best of times  */
		MouseAdapter ml = new GameObjectDeleter();
		canvas.addMouseMotionListener(ml);
		canvas.addMouseWheelListener(ml);
		canvas.addMouseListener(ml);

		/*  Add the canvas to the JFrame  */
		this.setJMenuBar(this.buildMenu());
		add(canvas);
		
		/*  Make it unresizable and pack it  */
		setResizable(false);
		pack();
		stopped = false;
		
		/*  Create World  */
		initializeWorld();
	}

	protected JMenuBar buildMenu()
	{
		JMenuBar menu = new JMenuBar();
		menu.setOpaque(true); //makes it visible
        menu.setPreferredSize(new Dimension(300, 30)); //size of the menu bar
        
        JMenu gameMenu = new JMenu("Game");
        JMenuItem restartItem = new JMenuItem("Restart Game");
        JMenuItem restartLevel = new JMenuItem("Restart Level");
        
        JMenuItem exitItem = new JMenuItem("Exit");
        
        JMenu colorMenu = new JMenu("Schemes");
        ButtonGroup colorGroup = new ButtonGroup();
        
        JRadioButtonMenuItem standard = new JRadioButtonMenuItem ("Standard");
        standard.setSelected(true);
        
        standard.addActionListener((ActionEvent event) -> { 
        	this.setScheme(0);
        	});        
        
        JRadioButtonMenuItem spring = new JRadioButtonMenuItem ("Spring");
        
        spring.addActionListener((ActionEvent event) -> { 
        	this.setScheme(1);
        	});
        
        JRadioButtonMenuItem summer = new JRadioButtonMenuItem ("Summer");
        summer.addActionListener((ActionEvent event) -> { 
        	this.setScheme(2);
        	});
        
        JRadioButtonMenuItem autumn = new JRadioButtonMenuItem ("Autumn");
        autumn.addActionListener((ActionEvent event) -> { 
        	this.setScheme(3);
        	});
        
        JRadioButtonMenuItem winter = new JRadioButtonMenuItem ("Winter");
        winter.addActionListener((ActionEvent event) -> { 
        	this.setScheme(4);
        	});
 
        colorMenu.add(standard);
        colorGroup.add(standard);
        
        colorMenu.add(spring);
        colorGroup.add(spring);
        
        colorMenu.add(summer);
        colorGroup.add(summer);
        
        colorMenu.add(autumn);
        colorGroup.add(autumn);
        
        colorMenu.add(winter);
        colorGroup.add(winter);
        
        // Events
        restartItem.addActionListener((ActionEvent event)-> {
        	this.restartGame(event);
        });
        
        restartLevel.addActionListener((ActionEvent event) -> {
        	this.restartLevel(event);
        });
        
        exitItem.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });

        gameMenu.add(restartItem);
        gameMenu.add(restartLevel);
        gameMenu.add(exitItem);

        menu.add(gameMenu);
        menu.add(colorMenu);
        
        return menu;
	}
	
	protected void restartGame(ActionEvent event)
	{
		//TODO: add reset code
	}
	
	protected void restartLevel(ActionEvent event)
	{
		//TODO: add restartLevel code
		reset = true;
	}
	
	public void setScheme(int schemeNumber)
	{
		scheme = schemeNumber;
		
		//TODO: Set up color scheme change here.
	}
		
	/*  Pretty Much the only function you need to overload  */
	protected void initializeWorld() 
	{
		bounds = new AxisAlignedBounds(25, 30);
		bounds.translate(0, 0);
		world = new World(bounds);
		//System.out.print("World bounds -> " + world.getBounds());
		permanent_count = 0;
		
		/*  Create the goal, a rectangle  */
		Rectangle floorRect = new Rectangle(15.0, 1.0);
		GameObject goal_floor = new GameObject();
		goal_floor.addFixture(new BodyFixture(floorRect));
		goal_floor.setMass(MassType.INFINITE);
		goal_floor.translate(0.0, -6.0);
		goal_floor.setAsPermanent(this);
		goal_floor.setColor(Color.BLACK);
		goalID = goal_floor.getId();
		world.addBody(goal_floor);
		
		/*  Create the player, a circle  */
		Circle cirShape = new Circle(0.5);
		GameObject player = new GameObject();
		player.addFixture(cirShape);
		player.setMass(MassType.NORMAL);
		player.translate(2.0, 13.0);
		player.setAsPermanent(this);
		Color playercolor = new Color(244 / 255.0f, 66 / 255.0f, 66 / 255.0f);
		player.setColor(playercolor);
		playerID = player.getId();
		world.addBody(player);
		
		/*  Try some squares dude  */
		for(int i = 0; i < 4; i++)
		{
			Rectangle rectShape = new Rectangle(2.5, 2.5);
			GameObject rectangle = new GameObject();
			rectangle.addFixture(rectShape);
			rectangle.setMass(MassType.NORMAL);
			rectangle.translate(2.0, i*2.0);
			world.addBody(rectangle);	
		}
		
		Rectangle rectShape = new Rectangle(2.5, 2.5);
		GameObject rectangle = new GameObject();
		rectangle.addFixture(rectShape);
		rectangle.setMass(MassType.NORMAL);
		rectangle.translate(-10.0, 0.0);
		world.addBody(rectangle);
		
		/*  attach the contact listener  */
		world.addListener(new WinDetection());
	}
	
	public void start() 
	{
		last = System.nanoTime();
		canvas.setIgnoreRepaint(true);
		canvas.createBufferStrategy(2);
		
		/*  run a separate thread to do active rendering
		 *  because we don't want to do it on the EDT  */
		Thread thread = new Thread() 
		{
			public void run() 
			{
				/*  perform an infinite loop stopped
				 *  render as fast as possible  */
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
 				if(((GameObject) b).getId() == playerID)
 				{
 					reset = true;
 				}
 				System.out.print("Shit's gone!");
 				world.removeBody(b);
 				world.setUpdateRequired(true);
 			}
 		}
     		
     	if(reset == true)
     	{
     		initializeWorld();
     		reset = false;
     	}
       
        long time = System.nanoTime();
        long diff = time - last;
        last = time;
    	double elapsedTime = diff / NANO_TO_BASE;
        world.update(elapsedTime);
	}

	protected void render(Graphics2D level_graphics) 
	{
		/*  Fill world with white color to draw objects over  */
		level_graphics.setColor(Color.WHITE);
		level_graphics.fillRect(-400, -300, 800, 600);
		

		
		/*  Draw all the objects in the world  */
		for(int i = 0; i < world.getBodyCount(); i++) 
		{
			GameObject go = (GameObject)world.getBody(i);
			
			//Attempted way to have screen centered on player, didn't work QQ
			if(go.getId() == playerID)
			{
				//level_graphics.translate(go.getWorldCenter().x, go.getWorldCenter().y);
			}
			go.render(level_graphics);
		}
	}

	public synchronized void stop() 
	{
		stopped = true;
	}
	
	public synchronized boolean isStopped() 
	{
		return stopped;
	}
	
	public static void main(String[] args) 
	{
		Level level0 = new Level();
		level0.setVisible(true);
		level0.start();
	}
}
