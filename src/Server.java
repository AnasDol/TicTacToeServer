import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    /*private static volatile Server instance;

    public static Server getInstance() {
        Server result = instance;
        if (result != null) {
            return result;
        }
        synchronized(Server.class) {
            if (instance == null) {
                instance = new Server();
            }
            return instance;
        }
    }*/

    private static final int PORT = 3443;

    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ArrayList<Session> sessions = new ArrayList<>();

    private static ArrayList<Integer> freeIds = new ArrayList<>(); // коллекция освободившихся айдишников
    private static int maxId = 0; // текущий максимальный id
    //private static int freePublicId = -1;
    private static ArrayList<Integer> waitingPublicSessions = new ArrayList<>();


    public Server() {

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {

            serverSocket = new ServerSocket(PORT);
            System.out.println("сервер запущен");

            while (true) {

                clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(this, clientSocket);
                clients.add(client);
                new Thread(client).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("cервер остановлен");
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public static Session createSession(boolean isPublic) {
        int id = getFreeSessionId();
        Session session = new Session(id, isPublic);
        sessions.add(session);
        return session;
    }

    public static Session findSessionById(int id) {
        for (Session session : sessions) {
            if (session.getSessionId()==id) {
                return session;
            }
        }
        return null;
    }

    public static int getFreeSessionId() {
        if (freeIds.isEmpty()) {
            return ++maxId;
        }
        else {
            return freeIds.remove(0);
        }
    }

    public static int getFreePublicId() {
        int id;
        if (waitingPublicSessions.isEmpty()) {
            Session session = createSession(true);
            id = session.getSessionId();
            waitingPublicSessions.add(id);
        }
        else {
            id = waitingPublicSessions.remove(0);
        }
        return id;
    }

    public static void removeSession(Session session) {
        freeIds.add(session.getSessionId());
        sessions.remove(session);
        waitingPublicSessions.remove(session);
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }


    public static void addWaitingPublicSession(int sessionId) {

    }
}
