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
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.JCheckBox;


public class ImageDisplay extends JPanel {
	JLabel lblImageLabel;
	ImagePlus currImagePlus;
	BufferedImage impb;
	JLabel lblMaxVal;
	JLabel lblMeanVal;
	JLabel lblMinVal;
	RescaleOp rescale;
	private JSlider slrMinRange;
	private JSlider slrMaxRange;
	JCheckBox chkboxLockValues;
	JLabel lblScale;
	double scale = 1;
	/**
	 * Create the panel.
	 */
	ImageIcon currImage;
	private JTextField txtMinRange;
	private JTextField txtMaxRange;
	public ImageDisplay() {
		setMaximumSize(new Dimension(550, 700));
		setMinimumSize(new Dimension(530, 580));
		setPreferredSize(new Dimension(588, 638));
		setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		Box horizontalBox = Box.createHorizontalBox();
		add(horizontalBox);
		
		Box verticalBox = Box.createVerticalBox();
		horizontalBox.add(verticalBox);
		
		
		
		lblImageLabel = new JLabel();
		lblImageLabel.setMaximumSize(new Dimension(2000000, 2000000));
		lblImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblImageLabel.setMinimumSize(new Dimension(512, 512));
		//verticalBox.add(lblImageLabel);
		
		JScrollPane scrollPane = new JScrollPane(lblImageLabel);
		scrollPane.setPreferredSize(new Dimension(590, 590));
		verticalBox.add(scrollPane);
		
		
		Component verticalGlue = Box.createVerticalGlue();
		verticalBox.add(verticalGlue);
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_3);
		
		Box verticalBox_1 = Box.createVerticalBox();
		horizontalBox_3.add(verticalBox_1);
		
		Box horizontalBox_7 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_7);
		
		JButton btnZoomIn = new JButton("Zoom In");
		btnZoomIn.addActionListener(btnZoomInActionListener);
		btnZoomIn.setAlignmentX(Component.CENTER_ALIGNMENT);
		horizontalBox_7.add(btnZoomIn);
		
		Component horizontalGlue_7 = Box.createHorizontalGlue();
		horizontalBox_7.add(horizontalGlue_7);
		
		JButton btnZoomOut = new JButton("Zoom Out");
		btnZoomOut.addActionListener(btnZoomOutActionListener);
		
		Box horizontalBox_8 = Box.createHorizontalBox();
		horizontalBox_7.add(horizontalBox_8);
		
		JLabel lbllbl = new JLabel("Scale:     ");
		lbllbl.setHorizontalAlignment(SwingConstants.LEFT);
		horizontalBox_8.add(lbllbl);
		
		lblScale = new JLabel("1");
		lblScale.setMaximumSize(new Dimension(50, 16));
		lblScale.setHorizontalAlignment(SwingConstants.RIGHT);
		lblScale.setPreferredSize(new Dimension(50, 20));
		lblScale.setName("lblScale");
		horizontalBox_8.add(lblScale);
		
		Component horizontalGlue_9 = Box.createHorizontalGlue();
		horizontalBox_7.add(horizontalGlue_9);
		btnZoomOut.setAlignmentX(Component.CENTER_ALIGNMENT);
		horizontalBox_7.add(btnZoomOut);
		
		Component verticalGlue_2 = Box.createVerticalGlue();
		verticalBox_1.add(verticalGlue_2);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_1);
		
		JLabel lblNewLabel_1 = new JLabel("min Range");
		horizontalBox_1.add(lblNewLabel_1);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue);
		
		slrMinRange = new JSlider();
		slrMinRange.addChangeListener(slrMinRange_changeListener);
		horizontalBox_1.add(slrMinRange);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue_2);
		
		txtMinRange = new JTextField();
		txtMinRange.setHorizontalAlignment(SwingConstants.RIGHT);
		txtMinRange.setText("-1");
		txtMinRange.setMaximumSize(new Dimension(50, 50));
		txtMinRange.setMinimumSize(new Dimension(22, 25));
		txtMinRange.setPreferredSize(new Dimension(50, 25));
		horizontalBox_1.add(txtMinRange);
		txtMinRange.setColumns(6);
		
		Component verticalGlue_1 = Box.createVerticalGlue();
		verticalBox_1.add(verticalGlue_1);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_2);
		
		JLabel lblNewLabel_2 = new JLabel("max Range");
		horizontalBox_2.add(lblNewLabel_2);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalBox_2.add(horizontalGlue_1);
		
		slrMaxRange = new JSlider();
		slrMaxRange.addChangeListener(slrMaxRange_changeListener);
		horizontalBox_2.add(slrMaxRange);
		
		Component horizontalGlue_3 = Box.createHorizontalGlue();
		horizontalBox_2.add(horizontalGlue_3);
		
		txtMaxRange = new JTextField();
		txtMaxRange.setHorizontalAlignment(SwingConstants.RIGHT);
		txtMaxRange.setText("-1");
		txtMaxRange.setMaximumSize(new Dimension(50, 50));
		txtMaxRange.setAlignmentX(Component.RIGHT_ALIGNMENT);
		txtMaxRange.setPreferredSize(new Dimension(22, 25));
		horizontalBox_2.add(txtMaxRange);
		txtMaxRange.setColumns(6);
		
		chkboxLockValues = new JCheckBox("lock values");
		verticalBox_1.add(chkboxLockValues);
		
		Box verticalBox_2 = Box.createVerticalBox();
		horizontalBox_3.add(verticalBox_2);
		
		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_4);
		
		JLabel lblNewLabel = new JLabel("min val:");
		lblNewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		horizontalBox_4.add(lblNewLabel);
		
		Component horizontalGlue_4 = Box.createHorizontalGlue();
		horizontalBox_4.add(horizontalGlue_4);
		
		lblMinVal = new JLabel("0");
		lblMinVal.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblMinVal.setPreferredSize(new Dimension(80, 16));
		lblMinVal.setHorizontalAlignment(SwingConstants.RIGHT);
		horizontalBox_4.add(lblMinVal);
		
		Component verticalGlue_3 = Box.createVerticalGlue();
		verticalBox_2.add(verticalGlue_3);
		
		Box horizontalBox_6 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_6);
		
		JLabel lblNewLabel_4 = new JLabel("mean val:");
		horizontalBox_6.add(lblNewLabel_4);
		
		Component horizontalGlue_6 = Box.createHorizontalGlue();
		horizontalBox_6.add(horizontalGlue_6);
		
		lblMeanVal = new JLabel("0");
		lblMeanVal.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMeanVal.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblMeanVal.setPreferredSize(new Dimension(80, 16));
		horizontalBox_6.add(lblMeanVal);
		
		Component verticalGlue_4 = Box.createVerticalGlue();
		verticalBox_2.add(verticalGlue_4);
		
		Box horizontalBox_5 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_5);
		
		JLabel lblNewLabel_5 = new JLabel("max val:");
		horizontalBox_5.add(lblNewLabel_5);
		
		Component horizontalGlue_5 = Box.createHorizontalGlue();
		horizontalBox_5.add(horizontalGlue_5);
		
		lblMaxVal = new JLabel("0");
		lblMaxVal.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMaxVal.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lblMaxVal.setPreferredSize(new Dimension(80, 16));
		horizontalBox_5.add(lblMaxVal);

	}
	
	void updateImage(String path){
		//System.out.println(path);
		ImagePlus imp = IJ.openImage(path); 
		currImagePlus = imp;
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
		imp = setRange(imp);
		setSliders(imp);
		impb = imp.getBufferedImage();
		impb = scaleImage(impb,scale);
		drawImage(impb);
        
	}
	void drawImage(BufferedImage impb) {
		currImage = new ImageIcon(impb);
        
		if (currImage != null && lblImageLabel != null) {
			lblImageLabel.setIcon(currImage);
			setValues();
		}
		else {
			System.out.println("nullpointer");
		}
	}
	
	BufferedImage scaleImage(BufferedImage impb, double scale){
		BufferedImage impbScaled = new BufferedImage((int) (impb.getWidth()*scale), (int) (impb.getHeight()*scale), impb.getType());
		Graphics2D g = impbScaled.createGraphics();
		g.drawImage(impb, 0, 0, (int) (impb.getWidth()*scale), (int) (impb.getHeight()*scale), null);
		g.dispose();
		return impbScaled;
	}
	
	void setSliders(ImagePlus imp) {
		int currentSlrMinRangeValue = slrMinRange.getValue();
		int currentSlrMaxRangeValue = slrMaxRange.getValue();
		int currentSlrMinRangeMinimum = slrMinRange.getMinimum();
		int currentSlrMinRangeMaximum = slrMinRange.getMaximum();
		int currentImageMinimum = (int) imp.getStatistics().min;
		int currentImageMaximum = (int) imp.getStatistics().max;
		int currentImageRange = currentImageMaximum-currentImageMinimum;
		
		if (Math.abs(currentImageMinimum - currentSlrMinRangeMinimum) / (1.0*currentImageRange) > 0.1 && !chkboxLockValues.isSelected()) {
			slrMinRange.setMinimum((int) (currentImageMinimum - 0.1*currentImageRange));
			slrMaxRange.setMinimum((int) (currentImageMinimum - 0.1*currentImageRange));
			slrMinRange.setValue((int) currentImageMinimum);
		}
		System.out.println("Math.abs(currentImageMaximum - currentSlrMinRangeMaximum): "+Math.abs(currentImageMaximum - currentSlrMinRangeMaximum));
		System.out.println("currentImageRange: "+currentImageRange);
		System.out.println("slrMinRange.getMinimum(): "+slrMinRange.getMinimum());
		System.out.println("slrMinRange.getMaxmum(): "+slrMinRange.getMaximum());
		if (Math.abs(currentImageMaximum - currentSlrMinRangeMaximum) / (1.0*currentImageRange) > 0.1 && !chkboxLockValues.isSelected()) {
			slrMinRange.setMaximum((int) (currentImageMaximum + 0.1*currentImageRange));
			slrMaxRange.setMaximum((int) (currentImageMaximum + 0.1*currentImageRange));
			slrMaxRange.setValue((int) currentImageMaximum);
		}
		/*int oldslrMinValue = slrMinRange.getValue();
		int oldslrMaxValue = slrMaxRange.getValue();
		int currentMinValue = (int) imp.getStatistics().min;
		int currentMaxValue = (int) imp.getStatistics().max;
		//increase the maximal slider value by 10 % to avoid rapid changes of the slider range
		int newMinRange = (int) slrMinRange.getMinimum();
		System.out.println("Slider value minslr: "+slrMinRange.getValue());
		System.out.println("Slider value maxslr: "+slrMaxRange.getValue());
		System.out.println("MinSlider minimum: "+slrMinRange.getMinimum());
		System.out.println("MinSlider maximum: "+slrMinRange.getMaximum());
		System.out.println("MaxSlider minimum: "+slrMaxRange.getMinimum());
		System.out.println("MaxSlider maximum: "+slrMaxRange.getMaximum());
		System.out.println("currentMinValue: "+currentMinValue);
		System.out.println("currentMaxValue: "+currentMaxValue);
		if (slrMinRange.getMinimum()<currentMinValue) {
			newMinRange = (int) 0.9 * currentMinValue; //if the new minimum is too low a new lower bound is set
		}
		int newMaxRange = (int) slrMinRange.getMaximum(); //limits can be calculated for only one slider but will be applied to both

		if (slrMinRange.getMaximum() <currentMaxValue || slrMinRange.getMaximum() > 1.1 * currentMaxValue) {
			newMaxRange = (int) 1.1 * currentMaxValue; //if the new maximum is too hig
		}
		slrMinRange.setMinimum((int) newMinRange);
		slrMinRange.setMaximum((int) newMaxRange);
		slrMinRange.setValue(Math.min(oldslrMinValue, currentMinValue));
		
		slrMaxRange.setMinimum((int) newMinRange);
		slrMaxRange.setMaximum((int) newMaxRange);
		slrMaxRange.setValue((int) Math.max(oldslrMaxValue,  currentMaxValue));*/
	}
	
	ImagePlus setRange(ImagePlus imp) {
		if (Integer.parseInt(txtMinRange.getText()) == -1) {
			txtMinRange.setText("" + (int) imp.getStatistics().min);
		}
		if (Integer.parseInt(txtMaxRange.getText()) == -1) {
			txtMaxRange.setText("" + (int) imp.getStatistics().max);
		}
		if (slrMaxRange.getValue() == 50){ //happens for first frame
			slrMaxRange.setMaximum((int) (1.1*imp.getStatistics().max));
			slrMinRange.setMaximum((int) (1.1*imp.getStatistics().max));
			slrMaxRange.setValue((int) imp.getStatistics().max);
			slrMinRange.setMinimum((int) (0.9*imp.getStatistics().min));
			slrMaxRange.setMinimum((int) (0.9*imp.getStatistics().min));
			slrMinRange.setValue((int) imp.getStatistics().min);
		}
		
		int minVal = Integer.parseInt(txtMinRange.getText());
		int maxVal = Integer.parseInt(txtMaxRange.getText());
		imp.setDisplayRange(minVal,maxVal);
		return imp;
	}
	void setValues(){
		//currImagePlus.getStatistics();
		//System.out.println(currImagePlus.getStatistics().mean);
		lblMinVal.setText(String.valueOf(currImagePlus.getStatistics().min));
		lblMeanVal.setText(String.valueOf((int) currImagePlus.getStatistics().mean));
		lblMaxVal.setText(String.valueOf(currImagePlus.getStatistics().max));
		
	};
	
	ChangeListener slrMinRange_changeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent arg0) {
			txtMinRange.setText(String.valueOf(slrMinRange.getValue()));
			updateImage(currImagePlus);
		}
	};
	ChangeListener slrMaxRange_changeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent arg0) {
			txtMaxRange.setText(String.valueOf(slrMaxRange.getValue()));
			updateImage(currImagePlus);
		}
	};
	ActionListener btnZoomInActionListener =new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(scale);
			scale = scale * 2;
			lblScale.setText(String.valueOf(scale));
			drawImage(impb);
		}
	};
	ActionListener btnZoomOutActionListener =new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(scale);
			scale = scale / 2;			
			lblScale.setText(String.valueOf(scale));
			drawImage(impb);
		}
	};
}
