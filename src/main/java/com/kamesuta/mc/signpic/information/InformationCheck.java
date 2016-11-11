package com.kamesuta.mc.signpic.information;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.kamesuta.mc.signpic.Client;
import com.kamesuta.mc.signpic.Reference;
import com.kamesuta.mc.signpic.http.CommunicateResponse;
import com.kamesuta.mc.signpic.http.ICommunicate;
import com.kamesuta.mc.signpic.http.ICommunicateResponse;
import com.kamesuta.mc.signpic.state.Progressable;
import com.kamesuta.mc.signpic.state.State;
import com.kamesuta.mc.signpic.util.Downloader;

public class InformationCheck implements ICommunicate<InformationCheck.InformationCheckResult>, Progressable {
	private static final Gson gson = new Gson();

	protected State status = new State("§6SignPicture Update Check");

	@Override
	public ICommunicateResponse<InformationCheckResult> communicate() {
		this.status.getProgress().setOverall(4).setDone(0);
		InputStream input = null;
		try {
			this.status.getProgress().setDone(1);
			final HttpUriRequest req = new HttpGet(new URI("https://raw.githubusercontent.com/Team-Fruit/SignPicture/master/info/info.json"));
			final HttpResponse response = Downloader.downloader.client.execute(req);
			final HttpEntity entity = response.getEntity();
			this.status.getProgress().setDone(2);
			final Info info = gson.fromJson(new JsonReader(new InputStreamReader(input = entity.getContent(), CharEncoding.UTF_8)), Info.class);
			if (info!=null) {
				final Informations.InfoSource source = new Informations.InfoSource(info);
				if (!StringUtils.isEmpty(info.private_msg)) {
					InputStream input1 = null;
					try {
						if (!StringUtils.isEmpty(Client.name)&&!StringUtils.isEmpty(Client.id)) {
							final String msgurl = info.private_msg
									.replace("%name%", Client.name)
									.replace("%id%", Client.id)
									.replace("%mcversion%", Client.mcversion)
									.replace("%forgeversion%", Client.forgeversion)
									.replace("%modmcversion%", Reference.MINECRAFT)
									.replace("%modforgeversion%", Reference.FORGE)
									.replace("%modversion%", Reference.VERSION);
							final HttpUriRequest req1 = new HttpGet(new URI(msgurl));
							final HttpResponse response1 = Downloader.downloader.client.execute(req1);
							final HttpEntity entity1 = response1.getEntity();
							input1 = entity1.getContent();
							this.status.getProgress().setDone(3);
							source.privateMsg = gson.fromJson(new JsonReader(new InputStreamReader(input1, Charsets.UTF_8)), Info.PrivateMsg.class);
						}
					} catch (final Exception e1) {
					} finally {
						IOUtils.closeQuietly(input1);
					}
				}
				this.status.getProgress().setDone(4);
				return new CommunicateResponse<InformationCheckResult>(new InformationCheckResult(source));
			}
		} catch (final Exception e) {
			return new CommunicateResponse<InformationCheckResult>(e);
		} finally {
			IOUtils.closeQuietly(input);
		}
		return new CommunicateResponse<InformationCheckResult>();
	}

	@Override
	public State getState() {
		return this.status;
	}

	@Override
	public void cancel() {
	}

	public static class InformationCheckResult {
		public final Informations.InfoSource source;

		public InformationCheckResult(final Informations.InfoSource source) {
			this.source = source;
		}
	}
}