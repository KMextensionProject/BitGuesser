package com.mt.notification;

import static java.lang.System.lineSeparator;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Logger.getLogger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;

public class TelegramNotification implements Notification {

	private static final Logger LOG = getLogger(TelegramNotification.class.getName());

	@Override
	public void sendNotification(Message message, Recipient recipient) {
		validateMessage(message);
		validateRecipient(recipient);

		String otherAddress = recipient.getOtherAddress();
		String botId = otherAddress.substring(0, otherAddress.lastIndexOf(":")); 
		String chatId = otherAddress.substring(otherAddress.lastIndexOf(":") + 1);
		sendTelegramMessage(botId, chatId, formatMessage(message));
	}

	private String formatMessage(Message message) {
		String formattedMessage = "";
		String subject = message.getSubject();
		String body = message.getBody();
		if (subject != null) {
			formattedMessage = "<b>" + subject + "</b>";
		}
		if (body != null) {
			if (!body.isEmpty()) {
				formattedMessage += lineSeparator();
			}
			formattedMessage += body;
		}
		return formattedMessage;
	}

	private void sendTelegramMessage(String botId, String chatId, String message) {
		@SuppressWarnings("deprecation")
		String encodedMessage = URLEncoder.encode(message);
		String url = "https://api.telegram.org/bot" + botId + "/sendMessage?chat_id=" + chatId + "&text=" + encodedMessage + "&parse_mode=html";
		try {
			HttpURLConnection httpRequest = (HttpURLConnection) new URL(url).openConnection();
			// for now, we do not need to capture the response
			httpRequest.getContentLength();
			httpRequest.disconnect();
		} catch (IOException ioex) {
			// quietly continue, no need to stop the application
			LOG.warning("Error during sending telegram notification: " + ioex);
		}
	}

	private static void validateMessage(Message message) {
		requireNonNull(message, "message cannot be null");
		if (isNull(message.getSubject()) && isNull(message.getBody())) {
			throw new IllegalArgumentException("message cannot be empty");
		}
	}

	private static void validateRecipient(Recipient recipient) {
		requireNonNull(recipient, "recipient cannot be null");
		if (isNull(recipient.getOtherAddress())) {
			throw new IllegalArgumentException("recipient for telegram notification should be defined by the 'other address'");
		}
	}
}
