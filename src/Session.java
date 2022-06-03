import java.util.ArrayList;
import java.util.Random;

public class Session {

    private final int sessionId;
    public int getSessionId() {return sessionId;}

    private int dimension = 3;

    // список существующих соединений
    private ArrayList<ClientHandler> clients = new ArrayList<>();
    // действующие игроки
    private ArrayList<ClientHandler> players = new ArrayList<>();

    private boolean isPublic; // сессия публичная или приватная

    private int rand;
    private ClientHandler player;
    private String[][] field;

    public Session(int id, boolean isPublic) {
        this.sessionId = id;
        this.isPublic = isPublic;
        field = new String[dimension][dimension];
    }

    public void sendMessageToAll(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void addClient(ClientHandler client) {
        clients.add(client);
        sendMessageToAll(client.getUsername() + " подключился к сеансу");
        if (players.size()<2) {
            players.add(client);
            if (players.size() == 2) {
                sendMessageToAll("Все в сборе! Можно начинать");
                startGame();
            }
        }
        else client.sendMessage("##observer##");

    }

    public void removeClient(ClientHandler client) {

        clients.remove(client);
        players.remove(client);

        if (clients.isEmpty()) {
            Server.removeSession(this);
        }
        else if (isPublic) Server.addWaitingPublicSession(sessionId);

    }

    public void startGame() {

        rand = new Random().nextInt(players.size());
        player = players.get(rand);

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                field[i][j] = null;
            }
        }
        nextTurn();

    }

    public void nextTurn() {
        if (rand == players.size()) rand = 0;
        player = players.get(rand++);
        //sendMessageToBoth("Ход игрока: " + player.getUsername());
        sendMessageToAll("##turn##");
        sendMessageToAll(player.getUsername());
    }

    public void markCell(ClientHandler player, int cell) {

        field[cell/10][cell%10] = player.getUsername();
        sendMessageToAll("##cell##");
        sendMessageToAll(Integer.toString(cell));
        if (player.equals(players.get(0))) {
            sendMessageToAll("X");
        }
        else {
            sendMessageToAll("O");
        }


        /*for (int i=0;i<3;i++) {
            System.out.println(Arrays.toString(field[i]));
        }*/
    }

    public void clearField() {
        sendMessageToAll("##clear##");
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                field[i][j] = null;
            }
        }
    }



}
