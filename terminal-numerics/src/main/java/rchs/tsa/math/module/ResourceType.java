package rchs.tsa.math.module;

public enum ResourceType
{
	MODULES,
	APPS;
	
	public String getPath()
	{
		return name().toLowerCase();
	}
}