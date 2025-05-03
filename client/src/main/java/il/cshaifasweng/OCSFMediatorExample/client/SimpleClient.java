package il.cshaifasweng.OCSFMediatorExample.client;

import org.greenrobot.eventbus.EventBus;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import javafx.application.Platform;

public class SimpleClient extends AbstractClient {

	private static SimpleClient client = null;
	public static PrimaryController controller;  // UI controller reference

	private static String customHost = "localhost";
	private static int customPort = 3000;

	public static void overrideConfig(String host, int port) {
		customHost = host;
		customPort = port;
	}

	private SimpleClient(String host, int port) {
		super(host, port);
	}

	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient(customHost, customPort);
		}
		return client;
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg.getClass().equals(Warning.class)) {
			EventBus.getDefault().post(new WarningEvent((Warning) msg));
		} else {
			String message = msg.toString();
			System.out.println("Received from server: " + message);

			if (message.startsWith("start:")) {
				String[] parts = message.split(":");
				String symbol = parts[1];
				boolean myTurn = Boolean.parseBoolean(parts[2]);

				Platform.runLater(() -> {
					if (controller != null) {
						controller.setSymbol(symbol);        // sets this client's identity
						controller.setYourTurn(myTurn);      // sets whether this player goes first
					}
				});
			}
 else if (message.startsWith("move:")) {
				String[] parts = message.split(":");
				int row = Integer.parseInt(parts[1]);
				int col = Integer.parseInt(parts[2]);
				String symbol = parts[3];

				Platform.runLater(() -> {
					if (controller != null) {
						controller.updateBoard(row, col, symbol);
					}
				});

			} else if (message.startsWith("win:")) {
				String symbol = message.split(":")[1];
				Platform.runLater(() -> {
					if (controller != null) controller.showWin(symbol);
				});

			} else if (message.startsWith("draw")) {
				Platform.runLater(() -> {
					if (controller != null) controller.showDraw();
				});
			}
		}
	}
}
