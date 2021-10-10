package com.mkm75.mclw.mcdi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mkm75.mclw.betterlogging.LogEvent;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {

	JDA api;
	MessageChannel channel;
	List<Function<LogEvent, Message>> logfunctions;
	List<Predicate<Message>> eventpredicates;

	public Listener() {
		logfunctions = new ArrayList<>();
		eventpredicates = new ArrayList<>();
	}

	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) return;
		Message message = event.getMessage();
		for (int i=0;i<eventpredicates.size();i++) {
			if (eventpredicates.get(i).test(message)) break;
		}
	}

	// ついでに処理
	public void onGotLog(LogEvent event) {
		Message message = null;
		for (int i=0;i<logfunctions.size();i++) {
			message = logfunctions.get(i).apply(event);
			if (message != null) break;
		}
		if (message != null) {
			channel.sendMessage(message).queue();
		}
	}

}
