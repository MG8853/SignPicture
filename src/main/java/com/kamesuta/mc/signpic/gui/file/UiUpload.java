package com.kamesuta.mc.signpic.gui.file;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.WindowConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.kamesuta.mc.signpic.lib.ComponentMover;
import com.kamesuta.mc.signpic.lib.ComponentResizer;

public abstract class UiUpload {

	protected JDialog frame;

	/**
	 * Create the application.
	 * @wbp.parser.entryPoint
	 */
	public UiUpload() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	protected void initialize() {
		this.frame = new JDialog();
		this.frame.setTitle(getString("signpic.ui.title"));
		this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.frame.setUndecorated(true);

		final JPanel base = new JPanel();
		this.frame.getContentPane().add(base, BorderLayout.CENTER);
		base.setLayout(new BorderLayout(0, 0));

		final JPanel title = new JPanel();
		title.setBackground(new Color(45, 45, 45));
		base.add(title, BorderLayout.NORTH);

		// final UiImage settings = new UiImage();
		// settings.setImage(getImage("textures/ui/setting.png"));

		final UiImage close = new UiImage();
		close.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				requestClose();
			}
		});
		close.setImage(getImage("textures/ui/close.png"));

		final UiImage icon = new UiImage();
		icon.setImage(getImage("/textures/logo.png"));

		final JLabel lbltitle = new JLabel(getString("signpic.ui.title"));
		lbltitle.setForeground(new Color(154, 202, 71));
		lbltitle.setFont(new Font(Font.DIALOG, Font.BOLD, 30));

		final JLabel lbldescription = new JLabel(getString("signpic.ui.description"));
		lbldescription.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		lbldescription.setForeground(new Color(0, 255, 255));
		final GroupLayout gl_title = new GroupLayout(title);
		gl_title.setHorizontalGroup(
				gl_title.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_title.createSequentialGroup()
								.addGap(12)
								.addComponent(icon, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_title.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_title.createParallelGroup(Alignment.LEADING)
												.addGroup(gl_title.createSequentialGroup()
														.addPreferredGap(ComponentPlacement.RELATED, 206, Short.MAX_VALUE)
														// .addComponent(settings, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
														.addGap(4)
														.addComponent(close, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
														.addGap(4))
												.addGroup(gl_title.createSequentialGroup()
														.addComponent(lbltitle)
														.addGap(16)))
										.addGroup(gl_title.createSequentialGroup()
												.addGap(16)
												.addComponent(lbldescription)))));
		gl_title.setVerticalGroup(
				gl_title.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_title.createSequentialGroup()
								.addGap(4)
								.addGroup(gl_title.createParallelGroup(Alignment.LEADING)
										// .addComponent(settings, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
										.addComponent(close, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_title.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_title.createSequentialGroup()
												.addComponent(lbltitle)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(lbldescription))
										.addComponent(icon, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE))
								.addContainerGap(20, Short.MAX_VALUE)));
		title.setLayout(gl_title);

		final JPanel drop = new JPanel();
		drop.setBorder(new LineBorder(new Color(45, 45, 45)));
		drop.setBackground(new Color(255, 255, 255));
		drop.setDropTarget(new DropTarget() {
			@Override
			public synchronized void drop(final DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					final List<?> droppedFiles = (List<?>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					for (final Object obj : droppedFiles)
						if (obj instanceof File) {
							final File file = (File) obj;
							apply(file);
						}
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		base.add(drop, BorderLayout.CENTER);

		final JPanel droparea = new JPanel();
		droparea.setBackground(new Color(255, 255, 255));
		droparea.setBorder(new DashedBorder());
		final GroupLayout gl_drop = new GroupLayout(drop);
		gl_drop.setHorizontalGroup(
				gl_drop.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_drop.createSequentialGroup()
								.addContainerGap()
								.addComponent(droparea, GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
								.addContainerGap()));
		gl_drop.setVerticalGroup(
				gl_drop.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_drop.createSequentialGroup()
								.addContainerGap()
								.addComponent(droparea, GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
								.addContainerGap()));
		droparea.setLayout(new BoxLayout(droparea, BoxLayout.Y_AXIS));

		final Component glue1 = Box.createVerticalGlue();
		droparea.add(glue1);

		final Component verticalStrut_2 = Box.createVerticalStrut(15);
		droparea.add(verticalStrut_2);

		final JLabel lblimagehere = new JLabel(getString("signpic.ui.drop"));
		droparea.add(lblimagehere);
		lblimagehere.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblimagehere.setFont(new Font(Font.DIALOG, Font.PLAIN, 25));

		final JButton btnselect = new JButton(getString("signpic.ui.select"));
		btnselect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ev) {
				final FileDialog fileDialog = new FileDialog(UiUpload.this.frame, getString("signpic.ui.title.file"), FileDialog.LOAD);
				fileDialog.setVisible(true);
				final String dir = fileDialog.getDirectory();
				final String name = fileDialog.getFile();
				if (dir!=null&&name!=null)
					apply(new File(dir, name));
			}
		});
		btnselect.setForeground(Color.BLACK);
		btnselect.setBackground(Color.WHITE);
		final Border line = new LineBorder(Color.BLACK);
		final Border margin = new EmptyBorder(5, 15, 5, 15);
		final Border compound = new CompoundBorder(line, margin);
		btnselect.setBorder(compound);
		btnselect.setFocusPainted(false);
		btnselect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent evt) {
				btnselect.setBackground(Color.CYAN);
			}

			@Override
			public void mouseExited(final MouseEvent evt) {
				btnselect.setBackground(Color.WHITE);
			}
		});
		btnselect.setAlignmentX(Component.CENTER_ALIGNMENT);
		droparea.add(btnselect);

		final Component verticalStrut_1 = Box.createVerticalStrut(20);
		droparea.add(verticalStrut_1);

		final Component glue2 = Box.createVerticalGlue();
		droparea.add(glue2);
		drop.setLayout(gl_drop);

		this.frame.pack();
		final Dimension minsize = new Dimension(this.frame.getSize());
		this.frame.setMinimumSize(minsize);

		final ComponentResizer cr = new ComponentResizer();
		cr.registerComponent(this.frame);
		cr.setMinimumSize(minsize);
		cr.setEdgeInsets(null);

		final ComponentMover cm = new ComponentMover(this.frame);
		cm.registerComponent(title);
		cm.setChangeCursor(false);
		cm.setEdgeInsets(null);

		this.frame.setSize(400, 400);
		this.frame.setLocationRelativeTo(null);
	}

	protected abstract BufferedImage getImage(final String path);

	protected abstract String getString(final String id);

	protected void requestOpen() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				open();
			}
		});
	}

	protected void open() {
		this.frame.setVisible(true);
	}

	protected void requestClose() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				close();
			}
		});
	}

	protected void close() {
		this.frame.setVisible(false);
	}

	public boolean isVisible() {
		return this.frame.isVisible();
	}

	protected abstract void apply(final File f);

	static class DashedBorder extends AbstractBorder {
		@Override
		public void paintBorder(final Component comp, final Graphics g, final int x, final int y, final int w, final int h) {
			final Graphics2D gg = (Graphics2D) g.create();
			gg.setColor(Color.GRAY);
			gg.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 6 }, 0));
			gg.drawRect(x, y, w-1, h-1);
			gg.dispose();
		}
	}
}