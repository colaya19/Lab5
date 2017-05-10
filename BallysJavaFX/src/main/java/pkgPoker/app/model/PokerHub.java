package pkgPoker.app.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import netgame.common.Hub;
import pkgPoker.app.controller.PokerTableController;
import pkgPokerBLL.Action;
import pkgPokerBLL.Card;
import pkgPokerBLL.CardDraw;
import pkgPokerBLL.Deck;
import pkgPokerBLL.GamePlay;
import pkgPokerBLL.GamePlayPlayerHand;
import pkgPokerBLL.Player;
import pkgPokerBLL.Rule;
import pkgPokerBLL.Table;

import pkgPokerEnum.eAction;
import pkgPokerEnum.eCardDestination;
import pkgPokerEnum.eDrawCount;
import pkgPokerEnum.eGame;
import pkgPokerEnum.eGameState;

public class PokerHub extends Hub {

	private Table HubPokerTable = new Table();
	private GamePlay HubGamePlay;
	private PokerTableController PokerGameState; 
	private int iDealNbr = 0;
	Rule rle; //made this global instead of in switch case

	public PokerHub(int port) throws IOException {
		super(port);
	}

	protected void playerConnected(int playerID) {

		if (playerID == 2) {
			shutdownServerSocket();
		}
	}

	protected void playerDisconnected(int playerID) {
		shutDownHub();
	}

	protected void messageReceived(int ClientID, Object message) {

		if (message instanceof Action) {
			Player actPlayer = (Player) ((Action) message).getPlayer();
			Action act = (Action) message;
			UUID dealer = HubPokerTable.getRandomPlayer().getPlayerID();
			switch (act.getAction()) {
			case Sit:
				HubPokerTable.AddPlayerToTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case Leave:			
				HubPokerTable.RemovePlayerFromTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case TableState:
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case StartGame:
				// Get the rule from the Action object.
				rle = new Rule(act.geteGame());
	

				//Lab #5 - Start the new instance of GamePlay
				HubGamePlay = new GamePlay(rle,dealer);
				// Add Players to Game
				HubGamePlay.setGamePlayers(HubPokerTable.getHmPlayer());
				// Set the order of players


			case Draw:

				int cardNum = rle.GetPlayerNumberOfCards(); //# of cards to be dealt
			
				int[] order = GamePlay.GetOrder(actPlayer.getiPlayerPosition()); //get position of me
				
				CardDraw cDraw = rle.GetDrawCard(HubGamePlay.geteDrawCountLast());
 
				for(int x=0; x < cardNum; x++) {
					for (int currPlayerPosition: order) {
						if (cDraw.getCardDestination() == eCardDestination.Player) {
							if (HubGamePlay.getPlayerByPosition(currPlayerPosition) != null) {
								HubGamePlay.drawCard(HubGamePlay.getPlayerByPosition(currPlayerPosition), cDraw.getCardDestination());
							}
						} else if (cDraw.getCardDestination() == eCardDestination.Community) {
							HubGamePlay.drawCard(HubGamePlay.getPlayerCommon(), cDraw.getCardDestination());
						}
					}
				}

				PokerGameState.Handle_GameState(HubGamePlay);
				HubGamePlay.isGameOver();

				resetOutput();
				//	Send the state of the gameplay back to the clients
				sendToAll(HubGamePlay);
				break;
			case ScoreGame:
				// Am I at the end of the game?

				resetOutput();
				sendToAll(HubGamePlay);
				break;
			}

		}

	}

}