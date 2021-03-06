package net.teamfruit.signpic;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.Closeable;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import net.minecraft.block.BlockSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.teamfruit.bnnwidget.WFrame;
import net.teamfruit.signpic.command.CommandImage;
import net.teamfruit.signpic.command.CommandVersion;
import net.teamfruit.signpic.command.RootCommand;
import net.teamfruit.signpic.compat.Compat.CompatBlockPos;
import net.teamfruit.signpic.compat.Compat.CompatMinecraft;
import net.teamfruit.signpic.compat.Compat.CompatMovingObjectPosition;
import net.teamfruit.signpic.compat.Compat.CompatSoundHandler;
import net.teamfruit.signpic.compat.Compat.CompatWorld;
import net.teamfruit.signpic.gui.GuiMain;
import net.teamfruit.signpic.render.CustomItemSignRenderer;
import net.teamfruit.signpic.render.CustomTileEntitySignRenderer;

public class Client {
	public static final @Nonnull Minecraft mc = CompatMinecraft.getMinecraft();

	public static final @Nonnull Gson gson = new Gson();

	public static @Nonnull CustomTileEntitySignRenderer rendererTile = new CustomTileEntitySignRenderer();
	public static @Nonnull CustomItemSignRenderer itemRenderer = new CustomItemSignRenderer();
	public static @Nonnull CoreHandler handler = new CoreHandler();
	private static @Nullable Locations location;

	public static @Nonnull Locations getLocation() {
		if (location!=null)
			return location;
		throw new IllegalStateException("signpic location not initialized");
	}

	public static void initLocation(final @Nonnull Locations locations) {
		location = locations;
	}

	public static @Nonnull String mcversion = MinecraftForge.MC_VERSION;
	public static @Nonnull String forgeversion = ForgeVersion.getVersion();

	public static @Nullable String id;
	public static @Nullable String name;
	public static @Nullable String token;

	public static @Nullable RootCommand rootCommand;

	static {
		final RootCommand cmd = rootCommand = new RootCommand();
		cmd.addChildCommand(new CommandVersion());
		cmd.addChildCommand(new CommandImage());
	}

	public static void openEditor() {
		WFrame.displayFrame(new GuiMain(mc.currentScreen));
	}

	public static void startSection(final @Nonnull String sec) {
		mc.mcProfiler.startSection(sec);
	}

	public static void endSection() {
		mc.mcProfiler.endSection();
	}

	public static @Nullable TileEntitySign getTileSignLooking() {
		final CompatMovingObjectPosition mpos = CompatMovingObjectPosition.getMovingPos();
		final CompatWorld world = CompatMinecraft.getWorld();
		if (mpos!=null&&world!=null) {
			final CompatBlockPos pos = mpos.getMovingBlockPos();
			if (pos!=null&&world.getBlockState(pos).getBlock().getBlockObj() instanceof BlockSign) {
				final TileEntity tile = world.getTileEntity(pos);
				if (tile instanceof TileEntitySign)
					return (TileEntitySign) tile;
			}
		}
		return null;
	}

	public static void playSound(final @Nonnull ResourceLocation location, final float volume) {
		CompatSoundHandler.playSound(location, volume);
	}

	public static final Set<String> schemes = ImmutableSet.of("http", "https");

	public static boolean openURL(final @Nonnull String uri) {
		try {
			return openURL(new URI(uri));
		} catch (final Throwable e) {
			Log.notice(I18n.format("signpic.gui.notice.openurlfailed", e));
			Log.log.warn("Failed to open URL", e);
		}
		return false;
	}

	public static boolean openURL(final @Nonnull URI uri) {
		try {
			final String scheme = StringUtils.lowerCase(uri.getScheme());
			if (!schemes.contains(scheme))
				throw new URISyntaxException(uri.toString(), "Unsupported protocol: "+scheme);
			final Desktop desktop = Desktop.getDesktop();
			desktop.browse(uri);
		} catch (final URISyntaxException e) {
			Log.notice(I18n.format("signpic.gui.notice.openurlfailed.invalid"));
		} catch (final Throwable e) {
			Log.notice(I18n.format("signpic.gui.notice.openurlfailed", e));
			Log.log.warn("Failed to open URL", e);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static void deleteMod(final @Nonnull File mod) {
		if (mod.delete())
			return;

		try {
			final LaunchClassLoader lloader = (LaunchClassLoader) Client.class.getClassLoader();
			final URL url = mod.toURI().toURL();
			final Field f_ucp = URLClassLoader.class.getDeclaredField("ucp");
			final Class<?> sunURLClassPath = Class.forName("sun.misc.URLClassPath");
			final Field f_loaders = sunURLClassPath.getDeclaredField("loaders");
			final Field f_lmap = sunURLClassPath.getDeclaredField("lmap");
			f_ucp.setAccessible(true);
			f_loaders.setAccessible(true);
			f_lmap.setAccessible(true);

			final Object ucp = f_ucp.get(lloader);
			final Closeable loader = ((Map<String, Closeable>) f_lmap.get(ucp)).remove(urlNoFragString(url));
			if (loader!=null) {
				loader.close();
				((List<?>) f_loaders.get(ucp)).remove(loader);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		if (!mod.delete()) {
			mod.deleteOnExit();
			final String msg = Reference.NAME+" was unable to delete file "+mod.getPath()+" the game will now try to delete it on exit. If this dialog appears again, delete it manually.";
			Log.log.error(msg);
			if (!GraphicsEnvironment.isHeadless())
				JOptionPane.showMessageDialog(null, msg, "An update error has occurred", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static @Nonnull String urlNoFragString(final @Nonnull URL url) {
		final StringBuilder strForm = new StringBuilder();

		String protocol = url.getProtocol();
		if (protocol!=null) {
			/* protocol is compared case-insensitive, so convert to lowercase */
			protocol = protocol.toLowerCase();
			strForm.append(protocol);
			strForm.append("://");
		}

		String host = url.getHost();
		if (host!=null) {
			/* host is compared case-insensitive, so convert to lowercase */
			host = host.toLowerCase();
			strForm.append(host);

			int port = url.getPort();
			if (port==-1)
				/* if no port is specificed then use the protocols
				 * default, if there is one */
				port = url.getDefaultPort();
			if (port!=-1)
				strForm.append(":").append(port);
		}

		final String file = url.getFile();
		if (file!=null)
			strForm.append(file);

		return strForm.toString();
	}

	public static void deleteMod() {
		final Locations loc = location;
		if (loc!=null&&loc.modFile.isFile())
			deleteMod(loc.modFile);
	}
}
