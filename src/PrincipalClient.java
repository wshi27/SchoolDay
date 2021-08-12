import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class PrincipalClient extends Thread {
	String name, incoming, outGoingRequest;
	Socket sSocket;
	PrintWriter out;
	BufferedReader in;

	// constructor
	PrincipalClient(Socket sSocket, PrintWriter out, BufferedReader in) {
		this.name = "Principal";
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
		
		Msg("requesting to run okToStart method");
		outGoingRequest = name + ":o:null";
		out.println(outGoingRequest);

		Msg("requesting to run startCheck method");
		outGoingRequest = name + ":c:null";
		out.println(outGoingRequest);

		for (int i = 1; i < 4; i++) {
			Msg("requesting to run startSession method");
			outGoingRequest = name + ":s:" + i;
			out.println(outGoingRequest);

			//if incoming shows school is still in session
			try {
				incoming = in.readLine();
			} catch (IOException e1) {
				Msg("read response fron schoolStatus Exception.");
				e1.printStackTrace();
			}
			if (incoming.equals("t")) {
				Msg("received that school is still in session");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Msg("sleep interrupted");
				}
				Msg("requesting to run endSession method");
				outGoingRequest = name + ":e:null";
				out.println(outGoingRequest);

				Msg("requesting to run notifyAllStudents method");
				outGoingRequest = name + ":n:" + (i + 1);
				out.println(outGoingRequest);
			} else {
				Msg("School ended due to covid, requesting to run notifyAllStudents"
						+ "method to send all students home");
				outGoingRequest = name + ":n:4";
				out.println(outGoingRequest);
			}
		}
		Msg("requesting to run endSchool method");
		outGoingRequest = name + ":f:null";
		out.println(outGoingRequest);
		
		Msg("requesting to terminate connection");
		out.println("terminate");
		Msg("Thread terminated.");
	}

	//main method
	public static void main(String[] args) {
		//checks if there are two arguments from command line for hostname and port number
		if (args.length != 2) {
			System.err.println("PrincipalClient missing <host name> and <port number> from command line");
			System.exit(1);
		}
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);

		//creates a socket connection for the PrincipalClient and start the PrincipalClient threads
		try {
			Socket sSocket = new Socket(hostName, portNumber);
			PrintWriter out = new PrintWriter(sSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(sSocket.getInputStream()));
			PrincipalClient Principal = new PrincipalClient(sSocket, out, in);
			Principal.start();

		} catch (UnknownHostException e) {
			System.out.println("PrincipalClient sSocket creation Exception: ");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("PrincipalClient input output creation Exception: ");
			e.printStackTrace();
		}

	}

}
