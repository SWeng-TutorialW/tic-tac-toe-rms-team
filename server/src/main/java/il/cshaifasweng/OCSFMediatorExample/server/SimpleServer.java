package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;

import java.io.IOException;
import java.util.ArrayList;

public class SimpleServer extends AbstractServer {

	private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();
	private ConnectionToClient playerX = null;
	private ConnectionToClient playerO = null;
	private ConnectionToClient currentPlayer = null;

	private String[][] board = new String[3][3]; // new: track game state
	private int moveCount = 0;

	public SimpleServer(int port) {
		super(port);
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		String msgString = msg.toString();

		if (msgString.startsWith("#warning")) {
			Warning warning = new Warning("Warning from server!");
			try {
				client.sendToClient(warning);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		else if (msgString.startsWith("add client")) {
			SubscribedClient connection = new SubscribedClient(client);
			SubscribersList.add(connection);

			try {
				if (playerX == null) {
					playerX = client;
				} else if (playerO == null) {
					playerO = client;
				}

				if (playerX != null && playerO != null) {
					// Randomize who is X and who starts
					if (playerX != null && playerO != null) {

						boolean playerXGetsX = Math.random() < 0.5;
						String symbolX = playerXGetsX ? "X" : "O";
						String symbolO = playerXGetsX ? "O" : "X";

						playerX.setInfo("symbol", symbolX);
						playerO.setInfo("symbol", symbolO);

						boolean playerXStarts = Math.random() < 0.5;
						currentPlayer = playerXStarts ? playerX : playerO;

						playerX.sendToClient("start:" + symbolX + ":" + playerXStarts);
						playerO.sendToClient("start:" + symbolO + ":" + !playerXStarts);
					}

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		else if (msgString.startsWith("remove client")) {
			SubscribersList.removeIf(sub -> sub.getClient().equals(client));
		}

		else if (msgString.startsWith("move")) {
			// move:row:col:symbol
			String[] parts = msgString.split(":");
			int row = Integer.parseInt(parts[1]);
			int col = Integer.parseInt(parts[2]);
			String symbol = parts[3];

			if (board[row][col] == null) {
				board[row][col] = symbol;
				moveCount++;

				// Send the move to both players
				try {
					if (playerX != null) playerX.sendToClient(msgString);
					if (playerO != null) playerO.sendToClient(msgString);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (checkWin(symbol)) {
					sendToAllClients("win:" + symbol);

					new Thread(() -> {
						try {
							Thread.sleep(3000);  // Wait 2 seconds before resetting
							resetGame();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}).start();

				} else if (moveCount == 9) {
					sendToAllClients("draw");

					new Thread(() -> {
						try {
							Thread.sleep(3000);  // Wait 2 seconds before resetting
							resetGame();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}).start();
				}

				// No winner → switch turn
				else {
					currentPlayer = (client == playerX) ? playerO : playerX;
				}
			}
		}
	}

	private boolean checkWin(String symbol) {
		// Rows, columns, diagonals
		for (int i = 0; i < 3; i++) {
			if (symbol.equals(board[i][0]) && symbol.equals(board[i][1]) && symbol.equals(board[i][2])) return true;
			if (symbol.equals(board[0][i]) && symbol.equals(board[1][i]) && symbol.equals(board[2][i])) return true;
		}
		if (symbol.equals(board[0][0]) && symbol.equals(board[1][1]) && symbol.equals(board[2][2])) return true;
		if (symbol.equals(board[0][2]) && symbol.equals(board[1][1]) && symbol.equals(board[2][0])) return true;
		return false;
	}

	private void resetGame() {
		board = new String[3][3];
		moveCount = 0;

		if (playerX != null && playerO != null) {
			// Randomly assign symbols
			boolean playerXGetsX = Math.random() < 0.5;
			String symbolX = playerXGetsX ? "X" : "O";
			String symbolO = playerXGetsX ? "O" : "X";

			playerX.setInfo("symbol", symbolX);
			playerO.setInfo("symbol", symbolO);

			// Randomly choose who starts
			boolean playerXStarts = Math.random() < 0.5;
			currentPlayer = playerXStarts ? playerX : playerO;

			try {
				playerX.sendToClient("start:" + symbolX + ":" + playerXStarts);  // true if starts
				playerO.sendToClient("start:" + symbolO + ":" + !playerXStarts); // false if not
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	public void sendToAllClients(String message) {
		try {
			for (SubscribedClient sub : SubscribersList) {
				sub.getClient().sendToClient(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
