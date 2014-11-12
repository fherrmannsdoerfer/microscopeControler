package microscopeControl;

import java.awt.Polygon;

import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.MaximumFinder;

public class CountBlinkingEvents {
	ImagePlus lastImg = null;
	public int findNumberOfBlinkingEvents(ImagePlus img){
		if (lastImg != null){ 
			ImageCalculator ic = new ImageCalculator();
			//ij.IJ.save(img, "c:\\tmp2\\img.tiff");
			//ij.IJ.save(lastImg, "c:\\tmp2\\lastimg.tiff");
			ic.run("Difference", lastImg, img);
			//ij.IJ.save(lastImg, "c:\\tmp2\\lastimgnacher.tiff");
			GaussianBlur gb = new GaussianBlur();
			gb.blurGaussian(lastImg.getProcessor(), 1,1,0.02);
			//ij.IJ.save(lastImg, "c:\\tmp2\\lastimgnacherAfterBlur.tiff");
			MaximumFinder mf = new MaximumFinder();
			//System.out.println("4*lastImg.getStatistics().stdDev "+4*lastImg.getStatistics().stdDev);
			Polygon maxima = mf.getMaxima(lastImg.getProcessor(), 4*lastImg.getStatistics().stdDev,true);//, img.getStatistics().mean*2, mf.LIST, true, false);
			lastImg = img;
			return maxima.npoints;
		}
		lastImg = img;
		return 0;
	}
}
