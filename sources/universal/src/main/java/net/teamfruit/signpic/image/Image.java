package net.teamfruit.signpic.image;

import static org.lwjgl.opengl.GL11.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.teamfruit.bnnwidget.compat.OpenGL;
import net.teamfruit.bnnwidget.position.Area;
import net.teamfruit.bnnwidget.render.WGui;
import net.teamfruit.bnnwidget.render.WRenderer;
import net.teamfruit.bnnwidget.render.WRenderer.BlendType;
import net.teamfruit.signpic.Config;
import net.teamfruit.signpic.ILoadCancelable;
import net.teamfruit.signpic.attr.AttrReaders;
import net.teamfruit.signpic.attr.prop.SizeData;
import net.teamfruit.signpic.entry.IAsyncProcessable;
import net.teamfruit.signpic.entry.ICollectable;
import net.teamfruit.signpic.entry.IDivisionProcessable;
import net.teamfruit.signpic.entry.IInitable;
import net.teamfruit.signpic.entry.content.Content;
import net.teamfruit.signpic.state.StateType;

public abstract class Image implements IInitable, IAsyncProcessable, IDivisionProcessable, ICollectable, ILoadCancelable {
	protected final @Nonnull Content content;

	public Image(final @Nonnull Content content) {
		this.content = content;
	}

	public abstract @Nonnull ImageTexture getTexture() throws IllegalStateException;

	public abstract @Nonnull String getLocal();

	public @Nonnull SizeData getSize() {
		if (this.content.state.getType()==StateType.AVAILABLE)
			return getTexture().getSize();
		else
			return SizeData.DefaultSize;
	}

	public void draw(@Nullable final Area vertex, @Nullable final Area trim, @Nullable final Area texture, final @Nullable BlendType b, final @Nullable BlendType d, final boolean r, final boolean m, final boolean l) {
		if (this.content.state.getType()==StateType.AVAILABLE) {
			WRenderer.startTexture(b, d);
			if (l)
				OpenGL.glEnable(GL_LIGHTING);
			final ImageTexture image = getTexture();
			image.bind();

			final int wraps = OpenGL.glGetTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S);
			final int wrapt = OpenGL.glGetTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T);
			final int mag = OpenGL.glGetTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER);
			final int min = OpenGL.glGetTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER);
			if (r) {
				OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
				OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
			} else {
				OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
				OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
			}
			if (image.hasMipmap())
				if (m&&OpenGL.openGl30()&&Config.getConfig().renderUseMipmap.get()) {
					OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, Config.getConfig().renderMipmapTypeNearest.get() ? GL_NEAREST : GL_LINEAR);
					OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, Config.getConfig().renderMipmapTypeNearest.get() ? GL_NEAREST_MIPMAP_LINEAR : GL_LINEAR_MIPMAP_LINEAR);
				} else {
					OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
					OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				}
			WGui.drawTexture(vertex, trim, texture);

			OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wraps);
			OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapt);
			if (image.hasMipmap()) {
				OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, mag);
				OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, min);
			}
			WRenderer.startTexture();
		}
	}

	public void draw(@Nonnull final AttrReaders meta, @Nullable final Area vertex, @Nullable final Area trim) {
		draw(
				vertex, trim,
				Area.size(
						meta.u.getMovie().get().data,
						meta.v.getMovie().get().data,
						meta.w.getMovie().get().data/meta.c.getMovie().get().data,
						meta.h.getMovie().get().data/meta.s.getMovie().get().data),
				meta.b.getMovie().get().data,
				meta.d.getMovie().get().data,
				meta.r.getMovie().get().data,
				meta.m.getMovie().get().data,
				meta.l.getMovie().get().data);
	}

	public void draw(@Nonnull final AttrReaders meta) {
		draw(meta, null, null);
	}
}