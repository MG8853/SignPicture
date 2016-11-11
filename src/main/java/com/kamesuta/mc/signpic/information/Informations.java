package com.kamesuta.mc.signpic.information;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.kamesuta.mc.signpic.Client;
import com.kamesuta.mc.signpic.Config;
import com.kamesuta.mc.signpic.Reference;
import com.kamesuta.mc.signpic.handler.CoreEvent;
import com.kamesuta.mc.signpic.http.Communicator;
import com.kamesuta.mc.signpic.http.ICommunicateCallback;
import com.kamesuta.mc.signpic.http.ICommunicateResponse;
import com.kamesuta.mc.signpic.information.InformationCheck.InformationCheckResult;
import com.kamesuta.mc.signpic.util.ChatBuilder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public final class Informations {
	public static final Informations instance = new Informations();

	private Informations() {
	}

	public static class InfoSource {
		public Info info;
		public Info.PrivateMsg privateMsg;

		public InfoSource(final Info info) {
			this.info = info;
		}

		public InfoVersion stableVersion() {
			if (this.info.versions!=null)
				return new InfoVersion(this.info.versions.get(Client.mcversion));
			return new InfoVersion();
		}

		public InfoVersion unstableVersion() {
			if (this.info.versions!=null)
				return new InfoVersion(this.info.versions.get(Client.mcversion+"-beta"));
			return new InfoVersion();
		}

		public InfoVersion onlineVersion() {
			final InfoVersion stable = stableVersion();
			final InfoVersion unstable = unstableVersion();
			return Config.instance.informationJoinBeta&&unstable.compare(stable) ? unstable : stable;
		}

		public static boolean equalsVersion(final InfoSource a, final InfoSource b) {
			if (a==null||b==null)
				return false;
			final InfoVersion stable = a.stableVersion();
			final InfoVersion unstable = b.stableVersion();
			if (stable==null||unstable==null)
				return false;
			return stable.equals(b.stableVersion())&&unstable.equals(b.unstableVersion());
		}
	}

	public static class InfoState {
		public boolean triedToWarnPlayer = false;
		public boolean downloading = false;
		public File downloadedFile;

		public boolean isDownloaded() {
			if (this.downloadedFile!=null)
				return this.downloadedFile.exists();
			return false;
		}
	}

	private InfoSource source;
	private InfoState state = new InfoState();

	public void setSource(final InfoSource source) {
		if (!InfoSource.equalsVersion(this.source, source))
			this.state = new InfoState();
		this.source = source;
	}

	public InfoSource getSource() {
		return this.source;
	}

	public InfoState getState() {
		return this.state;
	}

	public void init() {
		Communicator.instance.communicate(new InformationCheck(), new ICommunicateCallback<InformationCheck.InformationCheckResult>() {
			@Override
			public void onDone(final ICommunicateResponse<InformationCheckResult> res) {
				if (res.getResult()!=null)
					setSource(res.getResult().source);
				if (res.getError()!=null)
					Reference.logger.warn("Could not check version information", res.getError());
			}
		});
	}

	@CoreEvent
	public void onTick(final ClientTickEvent event) {
		if (event.phase==Phase.END)
			onTick(getSource(), getState());
	}

	public void onTick(final InfoSource source, final InfoState state) {
		final EntityPlayer player = Client.mc.thePlayer;
		if (player!=null&&!state.triedToWarnPlayer&&source!=null) {
			final String lang = Client.mc.gameSettings.language;
			if (source.info!=null&&
					source.info.versions!=null&&
					Config.instance.informationNotice&&
					!StringUtils.equals(Reference.VERSION, "${version}")) {
				final Version client = new Version(Reference.VERSION);
				final InfoVersion online = source.onlineVersion();

				if (online.compare(client))
					if (online.version!=null) {
						final Info.Version version = online.version;
						if (version.message_local!=null&&version.message_local.containsKey(lang))
							ChatBuilder.create(version.message_local.get(lang)).chatClient();
						else if (!StringUtils.isEmpty(version.message))
							ChatBuilder.create(version.message).chatClient();

						final String website;
						if (version.website!=null)
							website = version.website;
						else if (source.info.website!=null)
							website = source.info.website;
						else
							website = "https://github.com/Team-Fruit/SignPicture/";

						final String changelog;
						if (version.changelog!=null)
							changelog = version.changelog;
						else if (source.info.changelog!=null)
							changelog = source.info.changelog;
						else
							changelog = "https://github.com/Team-Fruit/SignPicture/releases";

						ChatBuilder.create("signpic.versioning.updateMessage").useTranslation().useJson()
								.replace("$old$", Reference.VERSION)
								.replace("$new$", version.version)
								.replace("$download$", "{\"action\":\"run_command\",\"value\":\"/signpic-download-latest\"}")
								.replace("$website$", "{\"action\":\"open_url\",\"value\":\""+website+"\"}")
								.replace("$changelog$", "{\"action\":\"open_url\",\"value\":\""+changelog+"\"}")
								.chatClient();
					}
			}
			if (source.privateMsg!=null) {
				final ChatBuilder ctb = new ChatBuilder();
				if (source.privateMsg.message_local!=null&&source.privateMsg.message_local.containsKey(lang))
					ctb.setText(source.privateMsg.message_local.get(lang));
				else if (!StringUtils.isEmpty(source.privateMsg.message))
					ctb.setText(source.privateMsg.message);
				if (source.privateMsg.json)
					ctb.useJson();
				ctb.chatClient();
			}
			getState().triedToWarnPlayer = true;
		}
	}

	public static class Version {
		public final int major;
		public final int minor;
		public final int micro;
		public final boolean beta;

		public Version(final int major, final int minor, final int micro, final boolean beta) {
			this.major = major;
			this.minor = minor;
			this.micro = micro;
			this.beta = beta;
		}

		public Version(final String string) {
			final String[] v = StringUtils.split(string, "\\.");
			this.major = v!=null&&v.length>=1 ? NumberUtils.toInt(v[0], 0) : 0;
			this.minor = v!=null&&v.length>=2 ? NumberUtils.toInt(v[1], 0) : 0;
			this.micro = v!=null&&v.length>=3 ? NumberUtils.toInt(v[2], 0) : 0;
			this.beta = v!=null&&v.length>=4&&StringUtils.equals(v[3], "beta");
		}

		public Version() {
			this(0, 0, 0, false);
		}

		public boolean compare(final Version version) {
			return this.major>version.major||
					this.major==version.major&&this.minor>version.minor||
					this.major==version.major&&this.minor==version.minor&&this.micro>version.micro||
					this.major==version.major&&this.minor==version.minor&&this.micro==version.micro&&this.beta&&!version.beta;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime*result+(this.beta ? 1231 : 1237);
			result = prime*result+this.major;
			result = prime*result+this.micro;
			result = prime*result+this.minor;
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this==obj)
				return true;
			if (obj==null)
				return false;
			if (getClass()!=obj.getClass())
				return false;
			final Version other = (Version) obj;
			if (this.beta!=other.beta)
				return false;
			if (this.major!=other.major)
				return false;
			if (this.micro!=other.micro)
				return false;
			if (this.minor!=other.minor)
				return false;
			return true;
		}
	}

	public static class InfoVersion extends Version {
		public final Info.Version version;

		public InfoVersion(final int major, final int minor, final int micro, final boolean beta, final Info.Version version) {
			super(major, minor, micro, beta);
			this.version = version;
		}

		public InfoVersion(final Info.Version version) {
			super(version!=null ? version.version : "");
			this.version = version;
		}

		public InfoVersion() {
			this(0, 0, 0, false, null);
		}
	}
}