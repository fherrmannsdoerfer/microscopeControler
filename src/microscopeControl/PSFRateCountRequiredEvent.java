package microscopeControl;

import ij.ImagePlus;

import java.util.EventObject;

public class PSFRateCountRequiredEvent extends EventObject {
	private ImagePlus img;
	public PSFRateCountRequiredEvent(Object source, ImagePlus img){
		super(source);
		this.img = img;
	}
	
	public ImagePlus getImage(){
		return img;
	}
}
