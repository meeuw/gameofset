package set;
import java.awt.Container;
import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.RGBImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ColorModel;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.Color;

public class CardComponent extends Container {
	private int shape;
	final static int WAVE = 1;
	final static int RECT = 2;
	final static int DIAMOND = 3;
	private int count;
	private int fill;
	final static int NONE = 1;
	final static int LINES = 2;
	final static int OPAQUE = 3;
	private int color;
	final static int PURPLE = 1;
	final static int RED = 2;
	final static int GREEN = 3;
	private boolean selected = false;
	private class Symbol extends JComponent {
		Image image;
		int rgbcolor = 0xFF000000;
		public Symbol(Image image) {
			switch (color) {
				case PURPLE:
					rgbcolor = 0xFFFF00FF;
					break;
				case RED:
					rgbcolor = 0xFFFF0000;
					break;
				case GREEN:
					rgbcolor = 0xFF00FF00;
					break;
			}
			this.image = createImage(new FilteredImageSource(image.getSource(),new RGBImageFilter() {
				public int filterRGB(int x, int y, int rgb) {
					if ((rgb & 0x00ffffff) == 0x00000000) {
						return rgbcolor;
					} else {
						return rgb;
					}
				}
			}));
		}
		public void paint(Graphics g) {
			if (selected) {
				g.setColor(Color.BLACK);
				g.fillRect(0,0,getWidth(),getHeight());
			}
			g.drawImage(image,0,0,getWidth(),getHeight(),0,0,128,256,this);
		}
	}
	public CardComponent(int shape, int count, int fill, int color, final CardComponentListener listener) {
		if (!((shape == 0) && (count == 0) && (fill == 0) && (color == 0))) {
			this.shape = shape;
			this.count = count;
			this.fill = fill;
			this.color = color;
			this.setLayout(new GridLayout(1,count));
			for (int i = 0; i < count ; i++) {
				this.add(new Symbol(getToolkit().getImage("set/media/"+shape+fill+".gif")));
			}
			this.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {
					selected = listener.setSelected(selected);
					repaint();
				}
				public void mouseReleased(MouseEvent e) {}
			});
		}
	}
}
