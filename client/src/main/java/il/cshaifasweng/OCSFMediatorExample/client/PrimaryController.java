package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.animation.PauseTransition;
import javafx.util.Duration;


import java.io.IOException;

public class PrimaryController {
	public static boolean isMyTurn = false;
	public static String mySymbol = "";

	@FXML private Button button00, button01, button02;
	@FXML private Button button10, button11, button12;
	@FXML private Button button20, button21, button22;

	private final Button[][] board = new Button[3][3];

	@FXML
	void initialize() {
		board[0][0] = button00; board[0][1] = button01; board[0][2] = button02;
		board[1][0] = button10; board[1][1] = button11; board[1][2] = button12;
		board[2][0] = button20; board[2][1] = button21; board[2][2] = button22;

		labelStatus.setText("Waiting for opponent...");

		try {
			SimpleClient.getClient().openConnection();
			SimpleClient.controller = this;
			SimpleClient.getClient().sendToServer("add client");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private Label labelStatus;

	public void setSymbol(String symbol) {
		mySymbol = symbol;
		labelStatus.setText("You are " + symbol);
	}

	public void setYourTurn(boolean turn) {
		isMyTurn = turn;
		labelStatus.setText(isMyTurn ? "Your turn!" : "Opponent's turn.");
	}

	private void handleMove(Button button, int row, int col) {
		if (!isMyTurn || !button.getText().isEmpty()) return;

		button.setText(mySymbol);
		button.setDisable(true);
		isMyTurn = false;

		try {
			SimpleClient.getClient().sendToServer("move:" + row + ":" + col + ":" + mySymbol);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Button actions
	public void topLeft(ActionEvent e) { handleMove(button00, 0, 0); }
	public void topMiddle(ActionEvent e) { handleMove(button01, 0, 1); }
	public void topRight(ActionEvent e) { handleMove(button02, 0, 2); }
	public void centerLeft(ActionEvent e) { handleMove(button10, 1, 0); }
	public void centerMiddle(ActionEvent e) { handleMove(button11, 1, 1); }
	public void centerRight(ActionEvent e) { handleMove(button12, 1, 2); }
	public void downLeft(ActionEvent e) { handleMove(button20, 2, 0); }
	public void downMiddle(ActionEvent e) { handleMove(button21, 2, 1); }
	public void downRight(ActionEvent e) { handleMove(button22, 2, 2); }

	public void updateBoard(int row, int col, String symbol) {
		Button b = board[row][col];
		b.setText(symbol);
		b.setDisable(true);
		setYourTurn(!symbol.equals(mySymbol));
	}

	public void showWin(String symbol) {
		labelStatus.setText(symbol + " wins!");
		disableBoard();

		PauseTransition pause = new PauseTransition(Duration.seconds(3));
		pause.setOnFinished(e -> resetBoard());
		pause.play();
	}


	public void showDraw() {
		labelStatus.setText("It's a draw!");
		disableBoard();

		PauseTransition pause = new PauseTransition(Duration.seconds(2));
		pause.setOnFinished(e -> resetBoard());
		pause.play();
	}

	private void resetBoard() {
		for (Button[] row : board) {
			for (Button b : row) {
				b.setText("");
				b.setDisable(false);
			}
		}

		isMyTurn = mySymbol.equals("X"); // X starts by default again
		labelStatus.setText(isMyTurn ? "Your turn!" : "Opponent's turn.");
	}

	private void disableBoard() {
		for (Button[] row : board) {
			for (Button b : row) {
				b.setDisable(true);
			}
		}
	}


	private void endGame() {
		for (Button[] row : board)
			for (Button b : row)
				b.setDisable(true);

		// Reset the board after 3 seconds
		new Thread(() -> {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Platform.runLater(() -> {
				for (Button[] row : board) {
					for (Button b : row) {
						b.setText("");
						b.setDisable(false);
					}
				}
				isMyTurn = mySymbol.equals("X"); // X always starts again
			});
		}).start();
	}
}
