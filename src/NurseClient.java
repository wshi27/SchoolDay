import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class NurseClient extends Thread{
	String name, incoming, outGoingRequest;
	Socket sSocket;
	PrintWriter out;
	BufferedReader in;

	// constructor
	NurseClient(Socket sSocket, PrintWriter out, BufferedReader in) {
		this.name = "Nurse";
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
		Msg("request to run arrived method");
		outGoingRequest = name + ":a:null";
		out.println(outGoingRequest);
		
		Msg("request to run startWork method");
		outGoingRequest = name + ":s:null";
		out.println(outGoingRequest);
		
		try {
			incoming = in.readLine();
		}catch(IOException e) {
			Msg("read response from numPositive Exception");
			e.printStackTrace();
		}
		if(Integer.parseInt(incoming) >= 3) {
			Msg("number of students tested positive is >= 3, request to run endSchool method");
			outGoingRequest = name + ":e:null";
			out.println(outGoingRequest);
		}
		
		Msg("request to run wakeUpPricipal method");
		outGoingRequest = name + ":p:null";
		out.println(outGoingRequest);
		
		Msg("request to terminate connection");
		out.println("terminate");
		Msg("Thread terminated");
	}
	
	//main method
	public static void main(String[] args) {
		//checks if there are two arguments from command line for hostname and port number
		if (args.length != 2) {
			System.err.println("NurseClient missing <host name> and <port number> from command line");
			System.exit(1);
		}
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);

		//creates a socket connection for the NurseClient and start the NurseClient threads
		try {
			Socket sSocket = new Socket(hostName, portNumber);
			PrintWriter out = new PrintWriter(sSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(sSocket.getInputStream()));
			NurseClient Nurse = new NurseClient(sSocket, out, in);
			Nurse.start();

		} catch (UnknownHostException e) {
			System.out.println("NurseClient sSocket creation Exception: ");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("NurseClient input output creation Exception: ");
			e.printStackTrace();
		}

	}

}
