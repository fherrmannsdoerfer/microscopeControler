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
import javax.swing.SwingConstants;

public class ImageDisplay extends JPanel {
	JLabel lblImageLabel;
	ImagePlus currImagePlus;
	JLabel lblMaxVal;
	JLabel lblMinVal;
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
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_3);
		
		Box verticalBox_1 = Box.createVerticalBox();
		horizontalBox_3.add(verticalBox_1);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_1);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue);
		
		JLabel lblNewLabel_1 = new JLabel("New label");
		horizontalBox_1.add(lblNewLabel_1);
		
		JSlider slider = new JSlider();
		horizontalBox_1.add(slider);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue_2);
		
		Component verticalGlue_1 = Box.createVerticalGlue();
		verticalBox_1.add(verticalGlue_1);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_2);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalBox_2.add(horizontalGlue_1);
		
		JLabel lblNewLabel_2 = new JLabel("New label");
		horizontalBox_2.add(lblNewLabel_2);
		
		JSlider slider_1 = new JSlider();
		horizontalBox_2.add(slider_1);
		
		Component horizontalGlue_3 = Box.createHorizontalGlue();
		horizontalBox_2.add(horizontalGlue_3);
		
		Box verticalBox_2 = Box.createVerticalBox();
		horizontalBox_3.add(verticalBox_2);
		
		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_4);
		
		JLabel lblNewLabel = new JLabel("min val:");
		horizontalBox_4.add(lblNewLabel);
		
		Component horizontalGlue_4 = Box.createHorizontalGlue();
		horizontalBox_4.add(horizontalGlue_4);
		
		lblMinVal = new JLabel("0");
		lblMinVal.setPreferredSize(new Dimension(40, 16));
		lblMinVal.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMinVal.setAlignmentY(Component.TOP_ALIGNMENT);
		horizontalBox_4.add(lblMinVal);
		
		Box horizontalBox_5 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_5);
		
		JLabel lblNewLabel_5 = new JLabel("max val:");
		horizontalBox_5.add(lblNewLabel_5);
		
		Component horizontalGlue_5 = Box.createHorizontalGlue();
		horizontalBox_5.add(horizontalGlue_5);
		
		lblMaxVal = new JLabel("0");
		lblMaxVal.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMaxVal.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lblMaxVal.setPreferredSize(new Dimension(40, 16));
		horizontalBox_5.add(lblMaxVal);

	}
	
	void updateImage(String path){
		//System.out.println(path);
		ImagePlus imp = IJ.openImage(path); 
		currImagePlus = imp;
        //ImageCanvas ic = new ImageCanvas(imp); 
        //imp.getBufferedImage();
        currImage = new ImageIcon(imp.getBufferedImage());
		//System.out.println(currImage.getIconHeight()+" "+ currImage.getIconWidth());
		if (currImage != null && lblImageLabel != null) {
			//System.out.println("kein nullpointer");
			lblImageLabel.setIcon(currImage);
			//this.repaint();
		}
		else {
			System.out.println("nullpointer");
		}
		setValues();
	}
	
	void updateImage(ImagePlus imp){
		currImagePlus = imp;
        currImage = new ImageIcon(imp.getBufferedImage());
		if (currImage != null && lblImageLabel != null) {
			lblImageLabel.setIcon(currImage);
		}
		else {
			System.out.println("nullpointer");
		}
		setValues();
		
	}
	
	void setValues(){
		lblMinVal.setText(String.valueOf(currImagePlus.getDisplayRangeMin()));
		lblMaxVal.setText(String.valueOf(currImagePlus.getDisplayRangeMax()));
	};

}
