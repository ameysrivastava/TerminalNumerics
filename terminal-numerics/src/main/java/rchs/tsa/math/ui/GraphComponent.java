package rchs.tsa.math.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.concurrent.CopyOnWriteArrayList;

import net.anasa.util.Bounds;
import net.anasa.util.Checks;
import net.anasa.util.Listing;
import net.anasa.util.Mapping;
import net.anasa.util.Pair;
import net.anasa.util.data.resolver.IToken;
import net.anasa.util.math.MathHelper;
import net.anasa.util.ui.LabelComponent;
import net.anasa.util.ui.PanelComponent;
import net.anasa.util.ui.SliderComponent;
import net.anasa.util.ui.event.IComponentListener;
import net.anasa.util.ui.layout.UIBorderLayout;
import net.anasa.util.ui.layout.UIBorderLayout.BorderPosition;
import rchs.tsa.math.MathException;
import rchs.tsa.math.graph.Graph;
import rchs.tsa.math.graph.GraphView;
import rchs.tsa.math.interpreter.SequenceParser;
import rchs.tsa.math.ui.event.GraphEvent;
import rchs.tsa.math.util.Evaluator;

public class GraphComponent extends PanelComponent
{
	private static final int SIZE = 320;
	
	private static final Font FONT = new Font(Font.SERIF, Font.PLAIN, 10);
	
	private static final Color BG_COLOR = new Color(0xF8F8F8);
	private static final Color AXIS_COLOR = new Color(0x666666);
	private static final Color INTERVAL_COLOR = new Color(0xAAAAAA);
	
	private final Listing<FunctionColor> functionColors = new Listing<>(new FunctionColor[] {
			new FunctionColor("Red", 0xAA0000),
			new FunctionColor("Blue", 0x0000AA),
			new FunctionColor("Green", 0x009922),
			new FunctionColor("Gold", 0x99690F),
			new FunctionColor("Purple", 0x62138A),
			new FunctionColor("Brown", 0x6B5231),
			new FunctionColor("Turquoise", 0x0F9997),
			new FunctionColor("Pink", 0xA6517D),
	});
	
	private final Listing<Graph> graphs = new Listing<Graph>().setHandle(new CopyOnWriteArrayList<>());
	private final GraphView view;
	
	private final PanelComponent graphPanel;
	
	private final SliderComponent scaleSlider;
	
	private SequenceParser parser;
	
	public GraphComponent(String data)
	{
		this();
		
		updateGraphs(data);
	}
	
	public GraphComponent(Graph... graphs)
	{
		this(new GraphView(), graphs);
	}
	
	public GraphComponent(GraphView view, Graph... graphs)
	{
		this.view = view;
		
		setBorder(2, 2);
		
		graphPanel = new PanelComponent();
		graphPanel.addDrawListener((event) -> updateGraphPanel(event.getGraphics()));
		
		graphPanel.setBackground(BG_COLOR);
		graphPanel.setSize(SIZE, SIZE);
		
		setParser(SequenceParser.EXPRESSION);
		
		scaleSlider = new SliderComponent(0, 154, 40);
		scaleSlider.addActionListener((event) -> redraw());
		
		PanelComponent bottom = new PanelComponent();
		bottom.setBorder(2, 2);
		
		UIBorderLayout bottomLayout = new UIBorderLayout();
		bottomLayout.set(BorderPosition.LEFT, new LabelComponent("Scale"));
		bottomLayout.set(BorderPosition.CENTER, scaleSlider);
		bottomLayout.apply(bottom);
		
		UIBorderLayout layout = new UIBorderLayout();
		layout.set(BorderPosition.CENTER, graphPanel);
		layout.set(BorderPosition.BOTTOM, bottom);
		layout.apply(this);
		
		for(Graph graph : graphs)
		{
			addGraph(graph);
		}
	}
	
	public FunctionColor getFunctionColor(int index)
	{
		return functionColors.getOrDefault(index, new FunctionColor("Black", 0x000000));
	}
	
	protected PanelComponent getPanel()
	{
		return graphPanel;
	}
	
	public GraphView getView()
	{
		return view;
	}
	
	public SequenceParser getParser()
	{
		return parser;
	}
	
	public void setParser(SequenceParser parser)
	{
		this.parser = parser;
	}
	
	public Listing<Graph> getGraphs()
	{
		return graphs;
	}
	
	public void addGraph(Graph graph)
	{
		getGraphs().add(graph);
		redraw();
	}
	
	public void clearGraphs()
	{
		getGraphs().clear();
	}
	
	@Override
	public void redraw()
	{
		getPanel().redraw();
	}
	
	public void addGraph(Listing<IToken> data)
	{
		addGraphs(new Listing<Listing<IToken>>(data));
	}
	
	public void addGraphs(Listing<Listing<IToken>> data)
	{
		Checks.checkNotNull(data, "data cannot be null");
		
		new Thread(() -> {
			clearGraphs();
			for(Listing<IToken> sequence : data)
			{
				try
				{
					Graph graph = new Graph(Evaluator.evaluate(sequence));
					addGraph(graph);
					
					getEvents().dispatch(new GraphEvent(this, graph));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			redraw();
		}).start();
	}
	
	public void updateGraphs(String data)
	{
		if(getParser() == null)
		{
			return;
		}
		
		try
		{
			addGraphs(new Listing<>(data.split(";")).filter((item) -> !item.trim().isEmpty()).conform((item) -> getParser().parse(item)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			clearGraphs();
		}
	}
	
	public void setGraph(String data)
	{
		clearGraphs();
		updateGraphs(data);
	}
	
	public void setGraph(Graph graph)
	{
		clearGraphs();
		addGraph(graph);
	}
	
	public void addGraphListener(IComponentListener<GraphEvent> listener)
	{
		getEvents().register(GraphEvent.class, listener);
	}
	
	private void updateGraphPanel(Graphics2D g)
	{
		g.setColor(INTERVAL_COLOR);
		g.setFont(FONT);
		
		double sizeX = 5 + getPanel().getWidth() * Math.pow(scaleSlider.getValue(), 2) / Math.pow(320, 2);
		double sizeY = 5 + getPanel().getHeight() * Math.pow(scaleSlider.getValue(), 2) / Math.pow(320, 2);
		
		getView().setBounds(new Bounds(-sizeX, -sizeY, sizeX, sizeY));
		getView().normalizeInterval();
		
		Bounds bounds = getView().getBounds();
		int intervalX = getView().getIntervalX();
		int intervalY = getView().getIntervalY();
		
		for(int x = (int)(bounds.getMinX() / intervalX) * intervalX; x < bounds.getMaxX(); x += intervalX)
		{
			g.drawLine(getX(x), 0, getX(x), getPanel().getHeight());
			g.drawString(String.valueOf(x), getX(x) + 2, getPanel().getHeight() - 2);
		}
		
		for(int y = (int)(bounds.getMinY() / intervalY) * intervalY; y < bounds.getMaxY(); y += intervalY)
		{
			g.drawLine(0, getY(y), getPanel().getWidth(), getY(y));
			g.drawString(String.valueOf(y), 2, getY(y) - 2);
		}
		
		g.setColor(AXIS_COLOR);
		
		g.drawLine(0, getY(0), getPanel().getWidth(), getY(0));
		g.drawLine(getX(0), 0, getX(0), getPanel().getHeight());
		
		int index = 0;
		for(Graph graph : getGraphs())
		{
			try
			{
				g.setColor(getFunctionColor(index).getColor());
				
				Integer lastX = null;
				Integer lastY = null;
				
				Mapping<Double, Double> map = getView().getValues(graph);
				
				for(Pair<Double, Double> pair : map)
				{
					double x = pair.getKey();
					double y = pair.getValue();
					
					int posX = getX(x);
					int posY = getY(y);
					
					if(lastX != null && MathHelper.isContained(getY(y), 0, getPanel().getHeight()))
					{
						g.drawLine(lastX, lastY, posX, posY);
					}
					
					lastX = posX;
					lastY = posY;
				}
			}
			catch(MathException e)
			{
				e.printStackTrace();
			}
			
			index++;
		}
	}
	
	private int getX(double x)
	{
		return (int)(x * getPanel().getWidth() / getView().getBounds().getWidth()) + getPanel().getWidth() / 2;
	}
	
	private int getY(double y)
	{
		return -(int)(y * getPanel().getHeight() / getView().getBounds().getHeight()) + getPanel().getHeight() / 2;
	}
}
