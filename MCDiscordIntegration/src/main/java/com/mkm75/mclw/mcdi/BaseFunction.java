package com.mkm75.mclw.mcdi;

import com.google.gson.JsonObject;
import com.mkm75.mclw.betterlogging.LogEvent;
import com.mkm75.mclw.mclogwrapper.base.ProcessWriter;
import com.mkm75.mclw.mclogwrapper.core.ClassHolder;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.ServerStateEvents;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class BaseFunction implements ServerStateEvents {

	JsonObject chat_format;
	MessageChannel channel;
	ProcessWriter writer;

	public BaseFunction() {
		writer=ClassHolder.get(ProcessWriter.class);
	}

	public Message logfunction(LogEvent event) {
		if (event.getFormat().startsWith("multiplayer.player.joined")) {
			return new MessageBuilder(chat_format.get("join").getAsString()
					.replace("{player}", event.getArg(0))).build();
		}
		if (event.getFormat().equals("multiplayer.player.left")) {
			return new MessageBuilder(chat_format.get("leave").getAsString()
					.replace("{player}", event.getArg(0))).build();
		}
		if (event.getFormat().equals("chat.type.text")) {
			return new MessageBuilder(chat_format.get("text").getAsString()
					.replace("{player}", event.getArg(0)).replace("{message}", event.getArg(1))).build();
		}
		if (event.getFormat().equals("chat.type.announcement")) {
			return new MessageBuilder(chat_format.get("announce").getAsString()
					.replace("{player}", event.getArg(0)).replace("{message}", event.getArg(1))).build();
		}
		if (event.getFormat().equals("chat.type.emote")) {
			return new MessageBuilder(chat_format.get("emote").getAsString()
					.replace("{player}", event.getArg(0)).replace("{message}", event.getArg(1))).build();
		}
		if (event.getFormat().equals("chat.type.achievement")) {
			return new MessageBuilder(chat_format.get("achevement").getAsString()
					.replace("{info}", event.format())).build();
		}
		if (event.getFormat().equals("chat.type.advancement.task")) {
			return new MessageBuilder(chat_format.get("advancement.task").getAsString()
					.replace("{info}", event.format())).build();
		}
		if (event.getFormat().equals("chat.type.advancement.goal")) {
			return new MessageBuilder(chat_format.get("advancement.goal").getAsString()
					.replace("{info}", event.format())).build();
		}
		if (event.getFormat().equals("chat.type.advancement.challenge")) {
			return new MessageBuilder(chat_format.get("advancement.challenge").getAsString()
					.replace("{info}", event.format())).build();
		}
		if (event.getFormat().startsWith("death")) {
			return new MessageBuilder(chat_format.get("death").getAsString()
					.replace("{info}", event.format())).build();
		}
		return null;
	}

	public boolean msgpredicate(Message message) {
		if (message.getChannel().getIdLong() != channel.getIdLong()) return false;
		String content = message.getContentRaw();
		System.out.println("[Discord] <" +  message.getAuthor().getName() + "> " + content);
		if (content.length() > 200) {
			writer.append("/tellraw @a [\"\",{\"text\":\"[Discord] \",\"color\":\"aqua\"},{\"text\":\"<" + message.getAuthor().getName() + "> \"},{\"text\":\"(200文字を超えたため省略されました)\",\"color\":\"yellow\"}]");
		} else {
			writer.append("/tellraw @a [\"\",{\"text\":\"[Discord] \",\"color\":\"aqua\"},{\"text\":\"<" + message.getAuthor().getName() + "> "
					+ content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r\n", "\\n").replace("\r", "\\n").replace("\n", "\\n") + "\"}]");
		}
		return false;
	}

	public void onDone() {
		channel.sendMessage(chat_format.get("done").getAsString()).complete();
	}

	public void onStop() {
		channel.sendMessage(chat_format.get("stop").getAsString()).complete();
	}

}
