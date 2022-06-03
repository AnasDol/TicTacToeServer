import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {

    private Server server;
    private Socket clientSocket = null;

    private static final String HOST = "localhost";
    private static final int PORT = 3443;

    private Scanner inMessage;
    private PrintWriter outMessage;
    private String clientMessage;

    private int TTL = 999999999;

    private String username;
    public String getUsername() {return username;}
    private int sessionId;

    private Session session;

    public ClientHandler(Server server, Socket clientSocket) {
        try {
            this.server = server;
            this.clientSocket = clientSocket;
            this.inMessage = new Scanner(clientSocket.getInputStream());
            this.outMessage = new PrintWriter(clientSocket.getOutputStream());
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {

            sendMessage("Подключение установлено");

            while (true) {

                if (inMessage.hasNext()) {

                    clientMessage = inMessage.nextLine();
                    System.out.println(clientMessage);

                    // обработать имя пользователя
                    if (clientMessage.equalsIgnoreCase("##username##")) {
                        username = waitForMessage();
                        if (session != null) session.addClient(this); // добавляем игрока к сессии только после того, как получили его имя
                        else sendMessage("##error##nosession##");
                    }

                    // передать сообщение игрока
                    else if (clientMessage.equalsIgnoreCase("##message##")) {
                        session.sendMessageToAll(username + ": " + waitForMessage());
                    }

                    // завершить клиентское соединение
                    else if (clientMessage.equalsIgnoreCase("##session##end##")) {
                        session.sendMessageToAll(username + " покинул сеанс");
                        session.removeClient(this);
                        Server.removeClient(this);
                        break;
                    }

                    // найти случайного соперника
                    else if (clientMessage.equalsIgnoreCase("##session##public##")) {
                        session = Server.findSessionById(Server.getFreePublicId());
                        if (session != null) {
                               sessionId = session.getSessionId();
                        }
                        sendMessage("##sessionId##");
                        sendMessage(Integer.toString(sessionId));
                    }

                    // создать новую приватную игру
                    else if (clientMessage.equalsIgnoreCase("##session##private##")) {
                        session = Server.createSession(false);
                        sessionId = session.getSessionId();
                        sendMessage("##sessionId##");
                        sendMessage(Integer.toString(sessionId));
                    }

                    // присоединиться к существующей игре
                    else if (clientMessage.equalsIgnoreCase("##session##join##")) {
                        sessionId = Integer.parseInt(waitForMessage());
                        session = Server.findSessionById(sessionId);
                        sendMessage("##sessionId##");
                        sendMessage(Integer.toString(sessionId));
                    }

                    // обработать ход игрока
                    else if (clientMessage.equalsIgnoreCase("##mark##")) {
                        int cell = Integer.parseInt(waitForMessage()); // две цифры: первая - строка, вторая - столбец
                        session.markCell(this, cell);
                        session.nextTurn();
                    }

                    else if (clientMessage.equalsIgnoreCase("##newgame##")) {
                        session.sendMessageToAll("Игрок "+getUsername()+" начал новую игру");
                        session.clearField();
                        session.startGame();
                    }

                }

                Thread.sleep(100);
            }
        }

        catch (InterruptedException e) {
            e.printStackTrace();
        }

        finally {
            this.close();
        }

    }

    public void sendMessage(String message) {
        try {
            outMessage.println(message);
            outMessage.flush();
            System.out.println("sending: " + message);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public String waitForMessage() {
        String clientMsg = "";
        while(true){
            if (inMessage.hasNext()) {
                clientMsg = inMessage.nextLine();
                break;
            }
        }
        System.out.println("receiving: " + clientMsg);
        return clientMsg;
    }

    public void close() {
        Server.removeClient(this);
        session.removeClient(this);
    }

}
