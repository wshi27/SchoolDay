import java.util.ArrayList;

//this class represent the nurse
public class Nurse extends Person implements Runnable {
	// ArrayList to represent the nurse waiting room
	private static ArrayList<Student> nurseWaitRoom = new ArrayList<>();

	// int variables to keep track of the number of students that need testing and
	// how many have
	// been tested
	public static int NumTesting = 0, NumTested = 0, numStuSent = 0, numPositive = 0;

	// variable to keep to check if nurse is waiting
	private static boolean nurseWaiting = false;

	// Student variable to hold the pair of students that nurse will test next
	private static Student p1, p2;

	// constructor method
	public Nurse(int id) {
		setName("Nurse" + id);
	}

	// method to update/check if nurse is waiting
	public static boolean updateNurseWaiting(boolean update) {
		if (update)
			nurseWaiting = !nurseWaiting;

		return nurseWaiting;
	}

	// method to get two students from nurse wait room if number of students is >= 2
	private static synchronized boolean get2Students() {
		if (nurseWaitRoom.size() >= 2) {
			p1 = nurseWaitRoom.remove(0);
			p2 = nurseWaitRoom.remove(0);
			numTestingMinus2();
			return true;
		}
		return false;
	}

	// method to increment/check the number of students that completed testing
	public synchronized static int incNumTested(boolean inc) {
		if (inc)
			NumTested++;
		return NumTested;
	}

	// method to increment/check the number of students that needs testing
	public synchronized static int incNumTesting(boolean inc) {
		if (inc)
			NumTesting++;
		return NumTesting;
	}

	// method to decrement the number of students that still needs testing
	private synchronized static void numTestingMinus2() {
		if (NumTesting == 1)
			NumTesting--;
		else
			NumTesting -= 2;
	}

	// method for nurse to get the testing result of each student that was sent for
	// testing
	private String getResult(Student s1) {
		String result;
		int positive = rand.nextInt(numStuSent - 1);

		if (positive < (NumTesting * 0.03)) {
			s1.instruction = "Home";
			result = (s1.getName() + " = positive;");
			numPositive++;
			if (numPositive >= 3) {
				msg("At lease 3 students tested positive.");
			}
		} else {
			s1.instruction = "Class1";
			result = (s1.getName() + " = negative;");
			if (numPositive >= 3) {
				msg("At lease 3 students tested positive.");
			}
		}
		incNumTested(true);
		return result;
	}

	// method to print the test results and notify students to go to their next
	// destination
	private void performTest(Student s1, Student s2) {
		int o = numPositive;
		StringBuilder mg = new StringBuilder();
		mg.append(getResult(s1) + " " + getResult(s2) + " NurseRoom = " + getNurseRoomSize() + "; NeedTesting = "
				+ incNumTesting(false) + "; NumTested = " + incNumTested(false));

		if (numPositive < 3 && numPositive == o) {
			mg.append("; InSchool = " + School.getNumStuInSchool() + "; NumPositive = " + numPositive);
			msg(mg.toString());
			synchronized (s1) {
				s1.notify();
			}
			synchronized (s2) {
				s2.notify();
			}
		} else if (numPositive < 3 && numPositive - 0 == 1) {
			mg.append("; InSchool = " + School.decNumStuInSchool(s2) + "; NumPositive = " + numPositive);
			msg(mg.toString());
			synchronized (s1) {
				s1.notify();
			}
			synchronized (s2) {
				s2.notify();
			}
		} else if (numPositive < 3 && numPositive - o == 2) {
			School.decNumStuInSchool(s1);
			mg.append("; InSchool = " + School.decNumStuInSchool(s2) + "; NumPositive = " + numPositive);
			msg(mg.toString());
			synchronized (s1) {
				s1.notify();
			}
			synchronized (s2) {
				s2.notify();
			}
		} else {
			School.decNumStuInSchool(s1);
			mg.append("; InSchool = " + School.decNumStuInSchool(s2) + "; NumPositive = " + numPositive);
			msg(mg.toString());

			synchronized (s1) {
				msg("sending " + s1.getName() + " home.");
				s1.instruction = "Home";
				s1.notify();
			}
			synchronized (s2) {
				msg("sending " + s2.getName() + " home.");
				s2.instruction = "Home";
				s2.notify();
			}
		}
	}

	// method for students to add themselves to the nurse wait room
	public static synchronized int waitAtNurseRoom(Student s, boolean add) {
		if (add) {
			nurseWaitRoom.add(s);
			s.msg("waiting at NurseRoom. NurseRoom = " + nurseWaitRoom.size() + "; NeedTesting = "
					+ incNumTesting(false) + "; NumTested = " + incNumTested(false));
		}
		return nurseWaitRoom.size();
	}

	// method to get the number of students inside nurse wait room
	public static synchronized int getNurseRoomSize() {
		return nurseWaitRoom.size();
	}

	// method to check the number of student left that still needs testing
	public static int numStudentLeft() {
		return (numStuSent - incNumTested(false));
	}

	public void arrived() {
		// prints the status of nurse wait room and number of students needs testing
		// when first arrived
		msg(" arrived. NurseRoom = " + getNurseRoomSize() + "; NeedTesting = " + incNumTesting(false) + "; NumTested = "
				+ incNumTested(false));
		numStuSent = incNumTesting(false);
	}

	public void startWork() {
		// while loop for nurse to make sure all students sent here are getting tested
		while (numStudentLeft() != 0 && numPositive < 3) {
			// testing students in pairs
			if (get2Students()) {
				msg("Testing " + p1.getName() + " and " + p2.getName() + ". NurseRoom = " + getNurseRoomSize()
						+ "; NeedTesting = " + incNumTesting(false) + "; NumTested = " + incNumTested(false) + ".");
				performTest(p1, p2);
				continue;
			}
			// if only one student left, nurse will test the single student separately
			else if (numStudentLeft() == 1 && getNurseRoomSize() == 1) {
				p1 = nurseWaitRoom.remove(0);
				numTestingMinus2();
				msg(getResult(p1) + " NurseRoom = " + getNurseRoomSize() + "; NeedTesting = " + incNumTesting(false)
						+ "; NumTested = " + incNumTested(false));
				synchronized (p1) {
					p1.notify();
				}
				break;
			} else {
				// if number of students need testing is not zero, and nurse wait room is empty,
				// nurse waits for additional students to arrive
				msg("waiting for more students to arrive");
				synchronized (this) {
					updateNurseWaiting(true);
					while (true) {
						try {
							this.wait();
							msg("notified");
							break;
						} catch (InterruptedException e) {
							if (!updateNurseWaiting(false))
								break;
							else
								continue;
						}
					}
				}
			}
		}
	}

	//method to end school when at least 3 students found to be covid positive
	public void endSchool() {
		//Set school status to ended and wait
		// for rest of need testing student to arrive in nurseWaitRoom
		School.endSchool();
		while (numStudentLeft() != getNurseRoomSize()) {
			msg("wait for rest of the students to arrive");
			synchronized (this) {
				updateNurseWaiting(true);
				while (true) {
					try {
						this.wait();
						msg("notified");
						break;
					} catch (InterruptedException e) {
						if (!updateNurseWaiting(false))
							break;
						else
							continue;
					}
				}
			}
		}
		// once all students have arrive, notify each one of them to go home
		while (getNurseRoomSize() != 0) {
			Student s = nurseWaitRoom.remove(0);
			synchronized (s) {
				msg("sent " + s.getName() + " home. InSchool = " + School.decNumStuInSchool(s));
				s.instruction = "Home";
				s.notify();
			}
		}
		msg("All students in Nurse Room had been notified to go home.");
	}

	//method to wake up principal if needed
	public void wakeUpPrincipal() {
		// if the last student tested positive, or school has ended, and other students
		// are already in
		// their respective classes, notify the principal to start session/send everyone
		// home
		if (School.incAndcheckStuInClass(false, this)) {
			synchronized (School.p) {
				if (!Principal.changeSessionStarted(false)) {
					School.p.notify();
					msg("All students in class, notified principle");
				}
			}
		}
	}

	@Override
	public void run() {
		arrived();
		startWork();

		if (numPositive >= 3) {
			endSchool();
		}

		wakeUpPrincipal();
		
		msg("Thread terminated");
	}
}
