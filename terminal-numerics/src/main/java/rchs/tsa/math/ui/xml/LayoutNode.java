package rchs.tsa.math.ui.xml;

import rchs.tsa.math.resource.module.context.ModuleContext;
import net.anasa.util.Mapping;
import net.anasa.util.data.FormatException;
import net.anasa.util.ui.IComponent;
import net.anasa.util.ui.PanelComponent;
import net.anasa.util.ui.layout.ILayout;

public class LayoutNode implements ILayoutNode
{
	private final Mapping<ILayoutNode, String> positions = new Mapping<>();
	
	private final ILayout layout;
	
	public LayoutNode(ILayout layout)
	{
		this.layout = layout;
	}

	public ILayout getLayout()
	{
		return layout;
	}
	
	public Mapping<ILayoutNode, String> getPositions()
	{
		return positions;
	}

	public void add(ILayoutNode node, String pos)
	{
		getPositions().put(node, pos);
	}

	@Override
	public IComponent compile(ModuleContext context) throws FormatException
	{
		PanelComponent panel = new PanelComponent();
		
		for(ILayoutNode node : getPositions().getKeys())
		{
			getLayout().set(getPositions().get(node), node.compile(context));
		}
		
		getLayout().apply(panel);
		
		return panel;
	}
}
