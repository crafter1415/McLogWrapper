package com.mkm75.mclw.mcdi;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.security.auth.login.LoginException;

import com.google.gson.JsonObject;
import com.mkm75.mclw.betterlogging.BetterLogging;
import com.mkm75.mclw.betterlogging.LogEvent;
import com.mkm75.mclw.langutil.LangUtil;
import com.mkm75.mclw.mclogwrapper.core.EventHooks;
import com.mkm75.mclw.mclogwrapper.extensions.Config;
import com.mkm75.mclw.mclogwrapper.extensions.Extensions;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.Initializable;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogWrapperExtension;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.ServerStateEvents;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.UseConfig;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

@LogWrapperExtension(major_version = MCDiscordIntegration.MAJOR_VERSION, minor_version = MCDiscordIntegration.MINOR_VERSION,
					name = MCDiscordIntegration.NAME, requirements_name = { BetterLogging.NAME, LangUtil.NAME }, requirements_version = { 0, 0 })
public class MCDiscordIntegration implements UseConfig, Initializable, ServerStateEvents {

	public static final double MAJOR_VERSION = 0;
	public static final double MINOR_VERSION = 0;
	public static final String NAME = "MCDiscordIntegration@mkm75";

	Config config;

	JDA api;
	MessageChannel channel;
	Listener listener;
	BaseFunction function;

	public Config reserveConfigs() {
		config = new Config();
		config.reserve("token", "");
		config.reserve("server_id", "");
		config.reserve("channel_id", "");
		JsonObject jo = new JsonObject();
		jo.addProperty("version", ":information_source: **サーバーのバージョン：`{version}`**");
		jo.addProperty("done", ":arrow_forward: **サーバーが起動しました**");
		jo.addProperty("stop", ":stop_button: **サーバーが停止しました**");
		jo.addProperty("join", ":bell: `{player}` が参加しました");
		jo.addProperty("leave", ":bell: `{player}` が退出しました");
		jo.addProperty("text", "`<{player}>` {message}");
		jo.addProperty("announce", "`[{player}]` {message}");
		jo.addProperty("emote", "＊ `{player}` {message}");
		jo.addProperty("achievement", ":trophy: {info}");
		jo.addProperty("advancement.task", ":trophy: {info}");
		jo.addProperty("advancement.goal", ":trophy: {info}");
		jo.addProperty("advancement.challenge", ":trophy: {info}");
		jo.addProperty("death", ":skull: {info}");
		config.reserve("chat", jo);

		return config;
	}

	public void setInstances() {

	}

	public void override() {

	}

	public void preInitialize() {

	}

	public void postInitialize() {
		listener = new Listener();
		function = new BaseFunction();
		try {
			api = JDABuilder.createDefault(config.get("token", String.class)).build();
			api.awaitReady();
			Guild guild = api.getGuildById(config.get("server_id", String.class));
			channel = guild.getTextChannelById(config.get("channel_id", String.class));
			String ver = ((LangUtil) Extensions.extensions.get(LangUtil.NAME).getInstance()).mc_ver;
			channel.sendMessage(config.get("chat", JsonObject.class).get("version").getAsString().replace("{version}", ver)).queue();
			api.addEventListener(listener);
		} catch (LoginException e) {
			System.out.println("[MCDiscordIntegr] ログインに失敗しました。トークンを確認してください");
			throw new InitializationException(e);
		} catch (InterruptedException e) {
			// んなもん投げんなの精神
			throw new InitializationException(e);
		} catch (NullPointerException e) {
			System.out.println("[MCDiscordIntegr] サーバーIDまたはチャンネルIDが無効です");
			throw new InitializationException(e);
		}
		EventHooks.add(LogEvent.class, listener::onGotLog);
		listener.api=api;
		listener.channel=channel;
		function.chat_format = config.get("chat", JsonObject.class);
		function.channel=channel;
		appendMsgFunction(function::msgpredicate);
		appendLogFunction(function::logfunction);
	}

	public void appendLogFunction(Function<LogEvent, Message> function) {
		if (!listener.logfunctions.contains(function)) listener.logfunctions.add(function);
	}

	public void insertLogFunction(Function<LogEvent, Message> function) {
		if (!listener.logfunctions.contains(function)) listener.logfunctions.add(0, function);
	}

	public void appendMsgFunction(Predicate<Message> predicate) {
		if (!listener.eventpredicates.contains(predicate)) listener.eventpredicates.add(predicate);
	}

	public void insertMsgFunction(Predicate<Message> predicate) {
		if (!listener.eventpredicates.contains(predicate)) listener.eventpredicates.add(0, predicate);
	}

	public void onDone() {
		function.onDone();
	}

	public void onStop() {
		function.onStop();
		api.shutdown();
	}

}
