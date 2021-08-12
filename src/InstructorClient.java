import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class InstructorClient extends Thread {
	String name, incoming, outGoingRequest;
	Socket sSocket;
	PrintWriter out;
	BufferedReader in;

	// constructor
	InstructorClient(int i, Socket sSocket, PrintWriter out, BufferedReader in) {
		this.name = "Instructor" + i;
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
		Random rand = new Random();

		for (int i = 1; i < 4; i++) {
			// request to start session
			Msg("request to run startSession method");
			outGoingRequest = name + ":s:" + i;
			out.println(outGoingRequest);

			// if incoming shows school is still in session,
			try {
				incoming = in.readLine();
			} catch (IOException e1) {
				Msg("read response fron schoolStatus Exception.");
				e1.printStackTrace();
			}
			if (incoming.equals("t")) {
				Msg("School status is open, request to run teachSession method");
				outGoingRequest = name + ":t:" + i;
				out.println(outGoingRequest);

				// rest before next session
				if (i < 2) {
					Msg("resting before the next session");
					int sleep = rand.nextInt(50);
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						Msg("sleep interrupted");
					}
				}
			}

			else {
				Msg("School ended due to Covid, request to run setNumStuInMyClass method");
				outGoingRequest = name + ":n:3";
				out.println(outGoingRequest);
				
				Msg("request to run releaseStu method to sent all students home");
				outGoingRequest = name + ":r:4";
				out.println(outGoingRequest);

				break;
			}
		}

		Msg("request to terminate connection");
		out.println("terminate");
		Msg("thread terminated");
	}

	//main method
	public static void main(String[] args) {
		//checks if there are two arguments from command line for hostname and port number
		if (args.length != 2) {
			System.err.println("InstructorClient missing <host name> and <port number> from command line");
			System.exit(1);
		}
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);

		//creates two socket connection for each of the two instances of InstructorClient and start the
		//InstructorClient threads
		try {
			for (int i = 1; i <= 2; i++) {
				Socket sSocket = new Socket(hostName, portNumber);
				PrintWriter out = new PrintWriter(sSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(sSocket.getInputStream()));
				InstructorClient instru = new InstructorClient(i, sSocket, out, in);
				instru.start();
			}

		} catch (UnknownHostException e) {
			System.out.println("InstructorClient sSocket creation Exception: ");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("InstructorClient input output creation Exception: ");
			e.printStackTrace();
		}

	}
}
