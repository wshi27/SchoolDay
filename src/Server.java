import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread{
	ServerSocket serverSocket;
	private ArrayList<Thread> subThreads = new ArrayList<>();
	
	//constructor establishes the server
	Server(int portNumber){
		try {
			serverSocket = new ServerSocket(portNumber);
			
		} catch (IOException e) {
			System.out.println("Exception caught trying to create new serverSocket");
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		System.out.println("Server established.");
		//for each request, create a new SubServerThread to handle the requests
		for (int i = 0; i < 24; i++) {
			try { 
				Socket clientSocket = serverSocket.accept();
				SubServerThread sub = new SubServerThread(clientSocket);
				subThreads.add(sub);
				sub.start();
				
			}catch(IOException e) {
				System.out.println("accepting client request exception.");
			}

		}	
		
		//join all sub threads so main server thread will only terminate after all
		//sub threads terminated
		for(Thread thread: subThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//close server socket
		try {
			serverSocket.close();
			System.out.println("Closed server socket.");
		} catch (IOException e) {
			System.out.println("Closing server socket exception");
			e.printStackTrace();
		}
	}
	
	//main method to create and start the server thread
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Cannot find port number from command line");
			System.exit(1);
		}

		int portNumber = Integer.parseInt(args[0]);
		Server s = new Server(portNumber);
		s.start();
	}
}
