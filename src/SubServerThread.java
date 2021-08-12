import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SubServerThread extends Thread {
	Socket clientSocket;
	String incomingRequest, outgoingInstruction;
	BufferedReader in;
	PrintWriter out;

	// constructor
	SubServerThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	private void Msg(String message) {
		System.out.println(message);
	}

	//student helper method to answer requests from student threads
	private void studentHelper(Student s, char methodInitial, String parameter) {

		switch (methodInitial) {
		case 'f':
			s.filloutCovidForm();
			break;

		case 'g':
			synchronized (s) {
				if (s.gotoSchool())
					s.waiting(s.instruction);
			}
			out.println(s.instruction);
			break;

		case 'd':
			s.decideWheretoGo();
			out.println(s.instruction);
			break;
		case 'a':
			if (s.attendance.length() != 10) {
				s.msg(s.attendance.toString());
			}
			break;
		}
		// wait for the next instruction and calls student helper with new parameters
		try {
			incomingRequest = in.readLine();
		} catch (IOException e) {
			Msg(s.name + ": new request read Exception.");
			e.printStackTrace();
		}
		if (!incomingRequest.equals("terminate")) {
			String[] request = incomingRequest.split(":");
			studentHelper(s, request[2].charAt(0), request[3]);
		}
	}

	//principal helper method to answer requests from the principal thread
	private void principalHelper(char methodInitial, String parameter) {
		switch (methodInitial) {
		case 'o':
			School.p.okToStart();
			break;
		case 'c':
			School.p.startCheck();
			School.p.msg("Finished checking all students who arrived before school started. InSchool = "
					+ School.getNumStuInSchool());
			break;
		case 's':
			School.p.startSession(Integer.parseInt(parameter));
			if (School.schoolStatus())
				out.println("t");
			else
				out.println("f");
			break;
		case 'e':
			School.p.endSession();
			break;
		case 'n':
			if (Integer.parseInt(parameter) == 4)
				Msg("sending all students in PHY-ED home");
			School.p.notifyAllStudents(Integer.parseInt(parameter));
			break;
		case 'f':
			School.endSchool();
		}

		// wait for the next instruction and calls principal helper with new parameters
		try {
			incomingRequest = in.readLine();
		} catch (IOException e) {
			Msg(School.p.name + ": new request read Exception.");
			e.printStackTrace();
		}
		if (!incomingRequest.equals("terminate")) {
			String[] request = incomingRequest.split(":");
			principalHelper(request[1].charAt(0), request[2]);
		}
	}

	//nurse helper method to answer requests from the nurse thread
	private void nurseHelper(char methodInitial, String parameter) {
		switch (methodInitial) {
		case 'a':
			School.n.arrived();
			break;
		case 's':
			School.n.startWork();
			out.println(Nurse.numPositive);
			break;
		case 'e':
			School.n.endSchool();
			break;
		case 'p':
			School.n.wakeUpPrincipal();
			break;
		}
		// wait for the next instruction and calls nurse helper with new parameters
		try {
			incomingRequest = in.readLine();
		} catch (IOException e) {
			Msg(School.n.name + ": new request read Exception.");
			e.printStackTrace();
		}
		if (!incomingRequest.equals("terminate")) {
			String[] request = incomingRequest.split(":");
			nurseHelper(request[1].charAt(0), request[2]);
		}

	}

	//instructor helper method to answer requests from instructor threads
	private void instructorHelper(Instructor i, char methodInitial, String parameter) {
		switch (methodInitial) {
		case 's':
			i.SessionStart(Integer.parseInt(parameter));
			if (School.schoolStatus()) {
				Msg("sending to: " + i.name + "schoolStatus: t");
				out.println("t");
			} else
				out.println("f");
			break;
		case 't':
			i.teachClass(Integer.parseInt(parameter));
			break;
		case 'n':
			i.setNumStuInMyClass(Integer.parseInt(parameter));
			break;
		case 'r':
			i.releaseStu(Integer.parseInt(parameter));
			break;

		}
		// wait for the next instruction and calls instructor helper with new parameters
		try {
			incomingRequest = in.readLine();
		} catch (IOException e) {
			incomingRequest = "terminate";
		}
		Msg(i.name + ": " + incomingRequest);
		if (!incomingRequest.equals("terminate")) {
			String[] request = incomingRequest.split(":");
			instructorHelper(i, request[1].charAt(0), request[2]);
		}

	}

	public void run() {
		try {
			//reads the first request from client and figure out which helper method to use
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			incomingRequest = in.readLine();

			String[] request = incomingRequest.split(":");

			switch (request[0]) {
			case "Student":
				Student s = new Student(Integer.parseInt(request[1]));
				Msg("established connection with " + s.name + ", in it's helper thread");
				studentHelper(s, request[2].charAt(0), request[3]);
				Msg(request[0] + ": closing clientSocket.");
				break;
			case "Principal":
				Msg("established connection with " + School.p.name + ", in it's helper thread");
				principalHelper(request[1].charAt(0), request[2]);
				Msg(request[0] + ": closing clientSocket.");
				break;
			case "Nurse":
				Msg("established connection with " + School.n.name + ", in it's helper thread");
				nurseHelper(request[1].charAt(0), request[2]);
				Msg(request[0] + ": closing clientSocket.");
				break;
			case "Instructor1":
				Msg("established connection with " + School.ElaInstructor.name + ", in it's helper thread");
				instructorHelper(School.ElaInstructor, request[1].charAt(0), request[2]);
				Msg(request[0] + ": closing clientSocket.");
				break;
			case "Instructor2":
				Msg("established connection with " + School.MathInstructor.name + ", in it's helper thread");
				instructorHelper(School.MathInstructor, request[1].charAt(0), request[2]);
				
				Msg(request[0] + ": closing clientSocket.");
				break;
			}

		} catch (IOException e) {
			Msg("Read first line Exception");
			e.printStackTrace();
		}

		try {
			clientSocket.close();
		} catch (IOException e) {
			Msg("clientSocket close Exception");
			e.printStackTrace();
		}
	}
}
