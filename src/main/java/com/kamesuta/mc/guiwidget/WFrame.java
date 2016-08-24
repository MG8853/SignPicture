package com.kamesuta.mc.guiwidget;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import com.kamesuta.mc.guiwidget.position.Point;
import com.kamesuta.mc.guiwidget.position.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class WFrame extends GuiScreen implements WContainer {
	protected final ArrayList<WCommon> widgets = new ArrayList<WCommon>();
	protected final WEvent event = new WEvent();

	public WFrame() {
	}

	public Area getAbsolute() {
		return new Area(0, 0, 0, 0, this.width, this.height);
	}

	public Point getMouseAbsolute() {
		return new Point(Mouse.getX() * this.width / this.mc.displayWidth,
				this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1);
	}

	@Override
	public List<WCommon> getContainer() {
		return this.widgets;
	}

	@Override
	public boolean add(final WCommon widget) {
		return this.widgets.add(widget);
	}

	@Override
	public boolean remove(final WCommon widget) {
		return this.widgets.remove(widget);
	}

	@Override
	public void initGui() {
		final Area gp = getAbsolute();
		for (final WCommon widget : this.widgets)
			widget.init(this.event, gp);
		sInitGui();
	}

	protected void sInitGui() {
		super.initGui();
	}

	protected void initWidgets() {
	}

	public void reset() {
		this.widgets.clear();
		initGui();
		initWidgets();
	}

	@Override
	public void setWorldAndResolution(final Minecraft mc, final int i, final int j) {
		final boolean init = this.mc == null;
		sSetWorldAndResolution(mc, i, j);
		if (init) {
			initWidgets();
		}
	}

	protected void sSetWorldAndResolution(final Minecraft mc, final int i, final int j) {
		super.setWorldAndResolution(mc, i, j);
	}

	@Override
	public void drawScreen(final int mousex, final int mousey, final float f) {
		final Area gp = getAbsolute();
		final Point p = getMouseAbsolute();
		for (final WCommon widget : this.widgets)
			widget.draw(this.event, gp, p, f);
		sDrawScreen(mousex, mousey, f);
	}

	protected void sDrawScreen(final int mousex, final int mousey, final float f) {
		super.drawScreen(mousex, mousey, f);
	}

	protected int mousebutton;

	@Override
	protected void mouseClicked(final int x, final int y, final int button) {
		this.mousebutton = button;
		final Area gp = getAbsolute();
		final Point p = getMouseAbsolute();
		for (final WCommon widget : this.widgets)
			widget.mouseClicked(this.event, gp, p, button);
		sMouseClicked(x, y, button);
	}

	protected void sMouseClicked(final int x, final int y, final int button) {
		super.mouseClicked(x, y, button);
	}

	@Override
	protected void mouseMovedOrUp(final int x, final int y, final int button) {
		if (this.mousebutton!=0 && button==0) {
			mouseReleased(x, y, this.mousebutton);
		} else {
			final Area gp = getAbsolute();
			final Point p = getMouseAbsolute();
			for (final WCommon widget : this.widgets)
				widget.mouseMoved(this.event, gp, p, button);
		}
		this.mousebutton = button;
		sMouseMovedOrUp(x, y, button);
	}

	protected void sMouseMovedOrUp(final int x, final int y, final int button) {
		super.mouseMovedOrUp(x, y, button);
	}

	protected void mouseReleased(final int x, final int y, final int button) {
		final Area gp = getAbsolute();
		final Point p = getMouseAbsolute();
		for (final WCommon widget : this.widgets)
			widget.mouseReleased(this.event, gp, p, button);
		sMouseReleased(x, y, button);
	}

	protected void sMouseReleased(final int x, final int y, final int button) {}

	@Override
	protected void mouseClickMove(final int x, final int y, final int button, final long time) {
		this.mousebutton = button;
		final Area gp = getAbsolute();
		final Point p = getMouseAbsolute();
		for (final WCommon widget : this.widgets)
			widget.mouseDragged(this.event, gp, p, button, time);
		sMouseClickMove(x, y, button, time);
	}

	protected void sMouseClickMove(final int x, final int y, final int button, final long time) {
		super.mouseClickMove(x, y, button, time);
	}

	@Override
	public void updateScreen() {
		if (this.mc.currentScreen == this) {
			final Area gp = getAbsolute();
			final Point p = getMouseAbsolute();
			for (final WCommon widget : this.widgets) {
				widget.update(this.event, gp, p);
			}
		}
		sUpdateScreen();
	}

	protected void sUpdateScreen() {
		super.updateScreen();
	}

	@Override
	protected void keyTyped(final char c, final int keycode) {
		final Area gp = getAbsolute();
		final Point p = getMouseAbsolute();
		for (final WCommon widget : this.widgets)
			widget.keyTyped(this.event, gp, p, c, keycode);
		sKeyTyped(c, keycode);
	}

	protected void sKeyTyped(final char c, final int keycode) {
		super.keyTyped(c, keycode);
	}

	@Override
	public void handleMouseInput() {
		final int i = Mouse.getEventDWheel();
		if (i != 0) {
			final Area gp = getAbsolute();
			final Point p = getMouseAbsolute();
			for (final WCommon widget : this.widgets)
				widget.mouseScrolled(this.event, gp, p, i);
		}
		sHandleMouseInput();
	}

	protected void sHandleMouseInput() {
		super.handleMouseInput();
	}
}
