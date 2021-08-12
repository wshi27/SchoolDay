import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class StudentClient extends Thread {
	String name, incoming, outGoingRequest;
	Socket sSocket;
	PrintWriter out;
	BufferedReader in;

	// constructor
	StudentClient(int i, Socket sSocket, PrintWriter out, BufferedReader in) {
		this.name = "Student:" + i;
		this.sSocket = sSocket;
		this.out = out;
		this.in = in;
		Msg("client object created");
	}

	//print method
	private void Msg(String message) {
		System.out.println(name + ": " + message);
	}
	
	public void run() {
		Msg("connection established with server, thread running.");
		
		// students rests a random amount of time before going to school
		Random rand = new Random();
		int sleep = rand.nextInt(100);
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			System.out.println("sleep interrupted");
		}

		Msg("request to run filloutCovidForm method");
		outGoingRequest = name + ":f:null";
		out.println(outGoingRequest);

		Msg("request to run gotoSchool method");
		outGoingRequest = name + ":g:null";
		out.println(outGoingRequest);

		// receives instruction on where to go next
		try {
			incoming = in.readLine();
			Msg("instruction received " + incoming);
		} catch (IOException e1) {
			Msg("read response fron goToSchool Exception.");
			e1.printStackTrace();
		}

		// while instruction is not "Home" continue to request decideWheretoGo
		while (!incoming.equals("Home")) {
			Msg("instruction != \"Home\", request to run decideWheretoGo method");
			outGoingRequest = name + ":d:" + incoming;
			out.println(outGoingRequest);
			try {
				incoming = in.readLine();
				Msg("instruction received " + incoming);
			} catch (IOException e) {
				Msg("read response from decideWheretoGo Exception.");
				e.printStackTrace();
			}
		}

		Msg("instruction is to go home, requesting to print attendance");
		outGoingRequest = name + ":a:null";
		out.println(outGoingRequest);

		Msg("request to terminate connection");
		out.println("terminate");
		Msg("Thread terminated.");
	}

	//main method
	public static void main(String[] args) {
		//checks if there are two arguments from command line for hostname and port number
		if (args.length != 2) {
			System.err.println("StudentClietn missing <host name> and <port number> from command line");
			System.exit(1);
		}
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);

		//creates 20 socket connections for each StudentClient and starts all the StudentClient threads
		try {
			for (int i = 1; i <= 20; i++) {
				Socket sSocket = new Socket(hostName, portNumber);
				PrintWriter out = new PrintWriter(sSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(sSocket.getInputStream()));
				StudentClient s = new StudentClient(i, sSocket, out, in);
				s.start();
			}

		} catch (UnknownHostException e) {
			System.out.println("StudentClient sSocket creation Exception: ");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("StudentClient input output creation Exception: ");
			e.printStackTrace();
		}

	}
}
