package microscopeControl;

import java.util.EventListener;

public interface PSFRateCountRequiredListener extends EventListener{
	public void PSFRateCountRequiredEventOccured(PSFRateCountRequiredEvent event);
}
