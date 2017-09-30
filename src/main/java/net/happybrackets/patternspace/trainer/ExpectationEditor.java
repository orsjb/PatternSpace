package net.happybrackets.patternspace.trainer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.happybrackets.patternspace.ctrnn.JCtrnn;

public class ExpectationEditor {

	/*
	 * Creates an editor for controlling a bunch of expectations.
	 */
	
	DataList inputNames;
	DataList outputNames;
	List<OutputExpectation> expectations;
	List<List<BufferedImage>> expectationImages;
	int boxWidth, boxHeight;
	JComponent component;
	JDialog drawWindow;
	int textWidth;
	int textHeight;
	int previousX;
	int previousY;
	JCtrnn.Params params;
	
	public ExpectationEditor(DataList inputNames, DataList outputNames, JCtrnn.Params params) {
		this.inputNames = inputNames;
		this.outputNames = outputNames;
		this.params = params;
		boxWidth = 40;
		boxHeight = 40;
		textWidth = 100;
		textHeight = 100;
	}
	
	public void createExpectationSet() {
		expectations = new ArrayList<OutputExpectation>();
		for(int i = 0; i < outputNames.size(); i++) {
			OutputExpectation exp = new OutputExpectation(inputNames.size());	
			expectations.add(exp);		
			for(int j = 0; j < exp.getNumInputs(); j++) {
				exp.getIOExpectation(j).createMap();
			}
		}
		setupImages();
	}
	
	void setupImages() {
		expectationImages = new ArrayList<List<BufferedImage>>();
		for(int i = 0; i < outputNames.size(); i++) {
			ArrayList<BufferedImage> theseImages = new ArrayList<BufferedImage>();
			expectationImages.add(theseImages);
			for(int j = 0; j < inputNames.size(); j++) {
				BufferedImage bi = new BufferedImage(boxWidth, boxHeight, BufferedImage.TYPE_INT_RGB);
				drawImage(expectations.get(i).getIOExpectation(j), bi);
				theseImages.add(bi);
			}	
		}
	}
	
	void drawImage(IOExpectation ioExp, BufferedImage bi) {
		Graphics g = bi.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		g.setColor(Color.black);
		int width = bi.getWidth() - 1;
		int height = bi.getHeight() - 1;
		g.drawLine(width, 0, width, height);
		g.drawLine(0, height, width, height);
		for(int i = 0; i < width; i++) {
			float val = ioExp.mapValue((float) i / width);
			g.fillRect(i, (int)(val * height), 1, 1);
		}
	}
	
	public JComponent getComponent() {
		if(component != null) return component;
		component = new JPanel();
		component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
		final JComponent editPanel = new JComponent() {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g) {
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.black);
				int i = 0;
				for(String inputName : inputNames.names()) {
					g.drawString(inputName, 5, textHeight + (i++ + 1) * boxHeight - 10);
				}
				i = 0;
				Graphics2D g2d = (Graphics2D)g;
				for(String outputName : outputNames.names()) {
					g2d.rotate(-Math.PI / 2f);
					g2d.drawString(outputName, -textHeight + 5, textWidth + (i++ + 1) * boxWidth - 10);
					g2d.rotate(Math.PI / 2f);
				}
				for(i = 0; i < expectationImages.size(); i++) {
					List<BufferedImage> theseImages = expectationImages.get(i);
					for(int j = 0; j < theseImages.size(); j++) {
						BufferedImage bi = theseImages.get(j);
						g.drawImage(bi, textWidth + i * boxWidth, textHeight + j * boxHeight, null);
					}
				}
				g.drawLine(0, textHeight, getWidth(), textHeight);
				g.drawLine(textWidth, 0, textWidth, getHeight());
			}
		};
		editPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if((e.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
					//work out which IOExpectation we're drawing to
					int windowWidth = 400;
					int windowHeight = 400;
					int x = e.getX() / boxWidth;
					int y = e.getY() / boxHeight;
					final IOExpectation ioExp = expectations.get(x).getIOExpectation(y);
					final BufferedImage bigIOExpImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
					drawImage(ioExp, bigIOExpImage);
					final BufferedImage ioExpImage = expectationImages.get(x).get(y);
					final JPanel drawPanel = new JPanel() {
						private static final long serialVersionUID = 1L;
						public void paintComponent(Graphics g) {
							Graphics2D g2d = (Graphics2D)g;
							g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							g.drawImage(bigIOExpImage, 0, 0, null);
						}
					};
					drawWindow = new JDialog((Frame)editPanel.getTopLevelAncestor());
					drawWindow.addMouseMotionListener(new MouseMotionListener() {
						public void mouseDragged(MouseEvent e) {
							ioExp.markMap((float)e.getX() / drawPanel.getWidth(), (float)e.getY() / drawPanel.getHeight());
							drawImage(ioExp, ioExpImage);
							drawImage(ioExp, bigIOExpImage);
							drawWindow.repaint();
						}
						public void mouseMoved(MouseEvent e) {
						}
					});
					drawWindow.addMouseListener(new MouseAdapter() {
						public void mousePressed(MouseEvent e) {
							ioExp.markMap((float)e.getX() / drawPanel.getWidth(), (float)e.getY() / drawPanel.getHeight());
							drawImage(ioExp, ioExpImage);
							drawImage(ioExp, bigIOExpImage);
							drawWindow.repaint();
						}
					});
					drawWindow.addWindowFocusListener(new WindowFocusListener() {
						public void windowGainedFocus(WindowEvent e) {}
						public void windowLostFocus(WindowEvent e) {
							drawWindow.dispose();
							editPanel.repaint();
						}
					});
					drawWindow.setContentPane(drawPanel);
					drawWindow.setUndecorated(true);
					drawWindow.setSize(new Dimension(windowWidth, windowHeight));
					drawWindow.setLocation(new Point(e.getX(), e.getY()));
					drawWindow.setVisible(true);
				}
			}
		});
		Dimension d = new Dimension(outputNames.size() * boxWidth + textWidth, inputNames.size() * boxHeight + textHeight);
		editPanel.setMinimumSize(d);
		editPanel.setMaximumSize(d);
		editPanel.setPreferredSize(d);
		component.add(editPanel);
		JPanel buttonPanel = new JPanel();
		JButton button = new JButton("Evolve");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ExpectationFitnessFunction eff = new ExpectationFitnessFunction(expectations, params);
				final CTRNNTrainer trainer = new CTRNNTrainer(params, eff, "/Users/ollie/Desktop/temp");
				Thread t = new Thread() {
					public void run() {
						try {
							trainer.evolve();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				t.start();
			}
		});
		buttonPanel.add(button);
		component.add(buttonPanel);
		return component;
	}

	
}
