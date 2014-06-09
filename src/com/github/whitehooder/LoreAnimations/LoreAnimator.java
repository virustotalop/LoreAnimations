package com.github.whitehooder.LoreAnimations;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoreAnimator {

	private ArrayList<String> pixels = new ArrayList<String>();

	private HashMap<Integer, Integer> colors = new HashMap<Integer, Integer>();

	private ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();

	public LoreAnimator(File file, LoreAnimations plugin) {
		colors.put(0, new Color(0, 0, 0).getRGB());
		colors.put(1, new Color(0, 0, 170).getRGB());
		colors.put(2, new Color(0, 170, 0).getRGB());
		colors.put(3, new Color(0, 170, 170).getRGB());
		colors.put(4, new Color(170, 0, 0).getRGB());
		colors.put(5, new Color(170, 0, 170).getRGB());
		colors.put(6, new Color(255, 170, 0).getRGB());
		colors.put(7, new Color(170, 170, 170).getRGB());
		colors.put(8, new Color(85, 85, 85).getRGB());
		colors.put(9, new Color(85, 85, 255).getRGB());
		colors.put(10, new Color(85, 255, 85).getRGB());
		colors.put(11, new Color(85, 255, 255).getRGB());
		colors.put(12, new Color(255, 85, 85).getRGB());
		colors.put(13, new Color(255, 85, 255).getRGB());
		colors.put(14, new Color(255, 255, 85).getRGB());
		colors.put(15, new Color(255, 255, 255).getRGB());

		try {
			String[] imageatt = new String[] { "imageLeftPosition",
					"imageTopPosition", "imageWidth", "imageHeight" };

			ImageReader reader = (ImageReader) ImageIO
					.getImageReadersByFormatName("gif").next();
			ImageInputStream ciis = ImageIO.createImageInputStream(file);
			reader.setInput(ciis, false);
			int noi = reader.getNumImages(true);
			BufferedImage master = null;
			for (int i = 0; i < noi; i++) {
				BufferedImage image = reader.read(i);
				IIOMetadata metadata = reader.getImageMetadata(i);
				Node tree = metadata.getAsTree("javax_imageio_gif_image_1.0");
				NodeList children = tree.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node nodeItem = children.item(j);
					if (nodeItem.getNodeName().equals("ImageDescriptor")) {
						Map<String, Integer> imageAttr = new HashMap<String, Integer>();
						for (int k = 0; k < imageatt.length; k++) {
							NamedNodeMap attr = nodeItem.getAttributes();
							Node attnode = attr.getNamedItem(imageatt[k]);
							imageAttr.put(imageatt[k],
									Integer.valueOf(attnode.getNodeValue()));
						}
						if (i == 0) {
							master = new BufferedImage(
									imageAttr.get("imageWidth"),
									imageAttr.get("imageHeight"),
									BufferedImage.TYPE_INT_ARGB);
						}
						master.getGraphics().drawImage(image,
								imageAttr.get("imageLeftPosition"),
								imageAttr.get("imageTopPosition"), null);
					}
				}
				frames.add(deepCopy(master));
			}
			ciis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String tobewritten = "";
		
		for (BufferedImage img : frames) {
			int w = img.getWidth();
			int h = img.getHeight();
			BufferedImage after;
			if (w > 32 || h > 32) {
				if (w > h) {
					after = new BufferedImage(32, h*32/w, img.getType());
				    Graphics2D g = after.createGraphics();
				    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				    g.drawImage(img, 0, 0, 32, h*32/w, 0, 0, w, h, null);
				    g.dispose();
				} else {
					after = new BufferedImage(w*32/h, 32, img.getType());
				    Graphics2D g = after.createGraphics();
				    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				    g.drawImage(img, 0, 0, 32, h*32/w, 0, 0, w, h, null);
				    g.dispose();
				}
			} else {
				after = deepCopy(img);
			}
			for (int y = 0; y < after.getHeight(); y++) {
				for (int x = 0; x < after.getWidth(); x++) {
					pixels.add(toMinecraftColor(new Color(after.getRGB(x, y)))
							+ "█");
					if (x == after.getWidth() - 1) {
						pixels.add("\n");
					}
				}
			}
			String ascii = "";
			for (String s : pixels) {
				ascii += s;
			}
			String speed = "";
			int pauses = (int) Math.round(1D/plugin.gifSpeed);
			for (int i = 0; i < pauses-1; i++) {
				speed += "==COPY==\n";
			}
			tobewritten += ascii + "==FRAME==\n" + speed;
			pixels.clear();
		}
		
		File output = new File(plugin.getDataFolder().getPath()+File.separator+"animations"+File.separator+file.getName().replaceAll("\\.[Gg][Ii][Ff]$", ".txt"));
		try {
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
			writer.write(tobewritten);
			writer.close();
			plugin.getLogger().info("Converted " + file.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	private String toMinecraftColor(Color color) {

		Color closest = null;
		double distance = Double.MAX_VALUE;
		for (int rgb : colors.values()) {
//			Color c = new Color(rgb);
//			float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
//			float brightness = hsb[2];
//			if (brightness > 0.1) {
//				if (colors.get(8) == rgb) {
//					continue;
//				}
//			}
			
			if (closest == null) {
				closest = new Color(rgb);
				distance = Math
						.sqrt(Math.pow(
								color.getRed() - new Color(rgb).getRed(), 2)
								+ Math.pow(
										color.getGreen()
												- new Color(rgb).getGreen(), 2)
								+ Math.pow(
										color.getBlue()
												- new Color(rgb).getBlue(), 2));
			} else {
				double dist = Math
						.sqrt(Math.pow(
								color.getRed() - new Color(rgb).getRed(), 2)
								+ Math.pow(
										color.getGreen()
												- new Color(rgb).getGreen(), 2)
								+ Math.pow(
										color.getBlue()
												- new Color(rgb).getBlue(), 2));
				if (dist < distance) {
					distance = dist;
					closest = new Color(rgb);
				}
			}
		}

		for (int colorcode : colors.keySet()) {
			if (closest.getRGB() == colors.get(colorcode)) {
				if (colorcode == 10)
					return "§a"+"§l";
				if (colorcode == 11)
					return "§b"+"§l";
				if (colorcode == 12)
					return "§c"+"§l";
				if (colorcode == 13)
					return "§d"+"§l";
				if (colorcode == 14)
					return "§e"+"§l";
				if (colorcode == 15)
					return "§f"+"§l";
				return "§" + colorcode+"§l";
			}
		}
		return "ERROR";
	}
}
