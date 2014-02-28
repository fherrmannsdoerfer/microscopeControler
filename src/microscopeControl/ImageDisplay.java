package microscopeControl;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.io.Opener;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.JScrollBar;
import javax.swing.JSlider;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.border.TitledBorder;

public class ImageDisplay extends JPanel {
	JLabel lblImageLabel;
	/**
	 * Create the panel.
	 */
	ImageIcon currImage;
	public ImageDisplay() {
		setMaximumSize(new Dimension(550, 700));
		setMinimumSize(new Dimension(530, 580));
		setPreferredSize(new Dimension(550, 600));
		setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		Box horizontalBox = Box.createHorizontalBox();
		add(horizontalBox);
		
		Box verticalBox = Box.createVerticalBox();
		horizontalBox.add(verticalBox);
		
		lblImageLabel = new JLabel();
		lblImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblImageLabel.setMaximumSize(new Dimension(512, 512));
		lblImageLabel.setPreferredSize(new Dimension(512, 512));
		lblImageLabel.setMinimumSize(new Dimension(512, 512));
		verticalBox.add(lblImageLabel);
		
		Component verticalGlue = Box.createVerticalGlue();
		verticalBox.add(verticalGlue);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_1);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue);
		
		JLabel lblNewLabel_1 = new JLabel("New label");
		horizontalBox_1.add(lblNewLabel_1);
		
		JSlider slider = new JSlider();
		horizontalBox_1.add(slider);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue_2);
		
		Component verticalGlue_1 = Box.createVerticalGlue();
		verticalBox.add(verticalGlue_1);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_2);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalBox_2.add(horizontalGlue_1);
		
		JLabel lblNewLabel_2 = new JLabel("New label");
		horizontalBox_2.add(lblNewLabel_2);
		
		JSlider slider_1 = new JSlider();
		horizontalBox_2.add(slider_1);
		
		Component horizontalGlue_3 = Box.createHorizontalGlue();
		horizontalBox_2.add(horizontalGlue_3);

	}
	
	void updateImage(String path){
		//System.out.println(path);
		ImagePlus imp = IJ.openImage(path); 
        //ImageCanvas ic = new ImageCanvas(imp); 
        //imp.getBufferedImage();
        currImage = new ImageIcon(imp.getBufferedImage());

		
		System.out.println(currImage.getIconHeight()+" "+ currImage.getIconWidth());
		if (currImage != null && lblImageLabel != null) {
			System.out.println("kein nullpointer");
			lblImageLabel.setIcon(currImage);
			//this.repaint();
		}
		else {
			System.out.println("nullpointer");
		}
		
	}
	

}
