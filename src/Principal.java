
import java.util.*;

//this class represents the principal
public class Principal extends Person implements Runnable {
	// ArrayList to represent the backYard where principal holds the PHY-ED class
	public static ArrayList<Student> backYard = new ArrayList<>();

	// variable to represent when sessions start and end
	private static boolean sessionStarted = false;

	// variables used to keep track of 1/3 random students chosen for covid test
	private int covidTest, everyThree = 0;

	// boolean to keep track if principal is waiting or not
	public static boolean principalWaiting = false;

	// principal class constructor method
	public Principal(int id) {
		setName("Principal" + id);
	}

	// method for students to attend the PHY-ED class
	public synchronized void attendPE(Student s) {
		backYard.add(s);
		s.attendance.append("PHY-ED ");
		s.msg(" attending PHY-ED. PHY-ED = " + backYard.size());
	}

	// method for principal to change session status and for everyone else to check
	// the
	// session status
	public synchronized static boolean changeSessionStarted(boolean change) {
		if (change) {
			if (sessionStarted == true) {
				sessionStarted = false;
				System.out.println("sessionStarted = false");
			} else {
				sessionStarted = true;
				System.out.println("sessionStarted = true");
			}
		}
		return sessionStarted;
	}

	// method for principal to check the covid form status for each student, if
	// covid form is
	// incomplete, student get sent home. Rest of the students get sent to nurse
	// when chosen
	// for covid test, or sent to class otherwise
	public void checkIndividual(Student s, int covidTest) {
		if (s.covidForm) {
			everyThree++;
			if (covidTest == everyThree) {
				s.instruction = "Nurse";
				msg(s.getName() + " selected for testing. nurseRoom = " + Nurse.waitAtNurseRoom(s, false)
						+ "; NeedTesting = " + (Nurse.incNumTesting(true)) + "; NumTested = "
						+ Nurse.incNumTested(false));
			} else {
				s.instruction = "Class1";
				msg(s.getName() + " sent to session 1");
			}
		} else {
			msg(s.getName() + " incomplete Covid form, sent home. InSchool = " + School.decNumStuInSchool(this));
			s.instruction = "Home";
			;
		}
	}

	// method to calculate the random number for covid test and to call the
	// individual students
	// from school yard.
	public void startCheck() {
		msg("SchoolYard = " + School.setNumStuInSchool(School.startSchool()) + "; Started School");
		covidTest = rand.nextInt(2) + 1;
		while (true) {
			Student s = School.getNextStudent();
			if (s != null) {
				synchronized (s) {
					checkIndividual(s, covidTest);
					s.notify();
				}
				if (everyThree == 3) {
					everyThree = 0;
					covidTest = rand.nextInt(2) + 1;
				}
			} else
				break;
		}
	}

	// principle will wait to be called by the last student who entered a
	// appropriate class.
	public void startSession(int i) {
		// wait for everyone to be in class
		synchronized (this) {
			while (true) {
				try {
					msg("waiting for all students to get in class");
					this.wait();
					msg("notified");
					break;
				} catch (InterruptedException e) {
					msg("Waiting all students to get to class interrupted");
					if (School.incAndcheckStuInClass(false, this))
						break;
					else
						continue;
				}
			}
		}

		// if nurse has found at least 3 students sick with covid, school ends
		if (School.schoolStatus()) {
			// start the session
			changeSessionStarted(true);
		}

		// notify ELA instructors to start class
		while (true) {
			synchronized (this) {
				boolean wait = false;
				//if ELA instructor is in the class already break
				if ((!School.ElaInstructor.classRoom.isEmpty()
						&& School.ElaInstructor.classRoom.get(School.ElaInstructor.classRoom.size() - 1) instanceof Instructor))
					break;
				//else if the ELA instructor has arrived but waiting, notify the instructor to start class
				else if (!School.ElaInstructor.instruInClass(false)) {
					synchronized (School.ElaInstructor) {
						School.ElaInstructor.notify();
						msg("notified Ela Instructor to start.");
						break;
					}
				} else
					wait = true;
				//else the ELA instructor is not in school yet, wait
				if (wait) {
					while (true) {
						msg("waiting for ELA instructors to get ready");
						principalWaiting = true;
						try {
							this.wait();
							msg("notified");
							break;
						} catch (InterruptedException e) {
							msg("Waiting ELA instructors to arrive school interrupted");
							break;
						}
					}
				}
			}
			//if notified, check again if the ELA instructor is already in the class or if arrived but waiting.
			//If waiting, notify. If none of the above, continue to wait
			if ((!School.ElaInstructor.classRoom.isEmpty()
					&& School.ElaInstructor.classRoom.get(School.ElaInstructor.classRoom.size() - 1) instanceof Instructor)) {
				principalWaiting = false;
				break;
			} else if (!School.ElaInstructor.instruInClass(false)) {
				synchronized (School.ElaInstructor) {
					School.ElaInstructor.notify();
					msg("notified Ela Instructor to start.");
					principalWaiting = false;
					break;
				}
			} else {
				msg("ELA instructor is not the one that notified me");
				continue;
			}

		}
		
		//notify the MATH instructor to start class
		synchronized (this) {
			boolean wait = false;
			//if the MATH instructor is not in class and but says he has arrived, then he/she is waiting
			//notify
			if (!School.MathInstructor.instruInClass && (School.MathInstructor.classRoom.isEmpty()
					|| !(School.MathInstructor.classRoom.get(School.MathInstructor.classRoom.size() - 1) instanceof Instructor))) {
				synchronized (School.MathInstructor) {
					School.MathInstructor.notify();
					msg("notified MATH Instructor to start class");
				}
			} 
			//else if the MATH instructor has not yet arrived wait
			else if (School.MathInstructor.instruInClass(false) && (School.MathInstructor.classRoom.isEmpty()
					|| !(School.MathInstructor.classRoom.get(School.MathInstructor.classRoom.size() - 1) instanceof Instructor)))
				wait = true;

			if (wait) {
				while (true) {
					msg("waiting for MATH instructors to get ready");
					principalWaiting = true;
					try {
						this.wait();
						msg("notified");
						break;
					} catch (InterruptedException e) {
						msg("Waiting MATH instructors to arrive school interrupted");
						break;
					}
				}
			}
		}
	}

	// method for principal to end session
	public void endSession() {
		changeSessionStarted(true);
		School.resetStuInClass();

		// if instructors is inside class notify both instructors to end class
		if (School.ElaInstructor.instruInClass(false) && School.MathInstructor.instruInClass(false)) {
			School.ElaInstructor.instruInClass(true);
			School.MathInstructor.instruInClass(true);
			Instructor ela = (Instructor) School.ElaInstructor.classRoom.remove(School.ElaInstructor.setNumStuInMyClass(3));
			Instructor math = (Instructor) School.MathInstructor.classRoom.remove(School.MathInstructor.setNumStuInMyClass(3));
			msg("notified ELA Instructor to end seesion");
			msg("notified MATH Instructor to end session");

			synchronized (ela) {
				ela.notify();
			}
			synchronized (math) {
				math.notify();
			}
		} else {
			msg("waiting for instructors to get to their classes");
			synchronized (this) {
				principalWaiting = true;
				while (true) {
					try {
						msg("waiting");
						this.wait();
						msg("notified");
						if (!School.ElaInstructor.instruInClass(false) || !School.MathInstructor.instruInClass(false)) {
							this.wait();
							msg("notified");
						}
						break;
					} catch (InterruptedException e) {
						if (School.ElaInstructor.instruInClass(false) && School.MathInstructor.instruInClass(false))
							break;
						else
							continue;
					}
				}
			}
		}

	}

	// notify all students attending PHY-ED that session has ended
	public synchronized void notifyAllStudents(int session) {
		Student s;
		while (!backYard.isEmpty()) {
			StringBuilder mg = new StringBuilder();
			s = (Student) backYard.remove(0);
			synchronized (s) {
				if (session == 4) {
					s.instruction = "Home";
					mg.append(s.getName() + " sent home");
				} else {
					s.instruction = "Class2";
					mg.append(s.getName() + " sent to class " + session);
				}

				mg.append(". PHY-ED = " + backYard.size());

				msg(mg.toString());
				s.notify();
			}
		}
	}
	
	public void okToStart() {
		// Principal waits until most(90% or above) students arrive in school before
				// starting school
				while (true) {
					if (School.NumStudentArrived() < (20 * 0.90)) {
						msg("# of Student in SchoolYard < 18(90%), continues to wait.");
						try {
							Thread.sleep(30);
						} catch (InterruptedException e) {
							msg("wait for # students in SchoolYard interrupted");
						}
					} else
						break;
				}
	}

	@Override
	public void run() {
		//check # of arrived students before starting school
		okToStart();
		

		// starts the checking process
		startCheck();

		msg("Finished checking all students who arrived before school started. InSchool = "
				+ School.getNumStuInSchool());

		// for loop for principal to keep count of the number of sessions
		for (int i = 1; i < 4; i++) {
			startSession(i);
			//if school has not ended, conduct a normal day
			if (School.schoolStatus()) {
				msg("Conducting PHY-ED and waiting for session to end");

				// principal sleeps to signify the time while class is in session
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					msg("sleep interrupted");
				}
				endSession();
				notifyAllStudents(i + 1);
			} 
			//else school ended due to covid, send all students in PHY-ED class home
			else {
				msg("sending all students in PHY-ED home");
				notifyAllStudents(4);
				break;
			}
		}

		School.endSchool();
		msg(" Thread terminated");
	}
}
