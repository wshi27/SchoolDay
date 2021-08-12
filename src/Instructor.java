import java.util.ArrayList;

//this class represent the two instructors
public class Instructor extends Person implements Runnable {

	// ArrayList to represent the classroom
	public ArrayList<Person> classRoom = new ArrayList<>();

	// int variables to keep track of number of students currently in class, and
	// number of students that needs to be dismissed from class
	public int numStuInMyClass = 0, numReleaseMyClass = 0;

	// boolean variables to check if each instructors are in their classroom
	public boolean instruInClass = true;

	// constructor
	public Instructor(int id) {
		setName("Instructor" + id);
	}

	// method for students to try and attend the respective classes
	public synchronized boolean attendClass(Student s) {
		//if number of students currently in class is less than 4, add student to class
		if (setNumStuInMyClass(0) < 4) {
			classRoom.add(s);
			//increase numStuInMyClass count, and add the respective class to student's attendance
			if (this.name.contains("1")) {
				s.msg(" attending ELA. ElaRoom = " + setNumStuInMyClass(1));
				s.attendance.append("ELA ");
			} else {
				s.msg(" attending MATH. Mathroom = " + setNumStuInMyClass(1));
				s.attendance.append("MATH ");
			}
			return true;
		}
		return false;
	}


	// method for student to check how many spots left in class, 
	//for instructor to remove/check number of students from previous class,
	//and for any other person to inc/dec number of students in class
	public synchronized int setNumStuInMyClass(int action) {
		if (action == 1)
			numStuInMyClass++;
		else if (action == 2) {
			numStuInMyClass--;
			return numStuInMyClass;
		} else if (action == 3) {
			numReleaseMyClass = numStuInMyClass;
			return numReleaseMyClass;
		} else if (action == 4)
			return numStuInMyClass;

		return numStuInMyClass - numReleaseMyClass;
	}


	// method to update/check if instructor is in classroom
	public synchronized boolean instruInClass(boolean updateStatus) {
		if (updateStatus)
			instruInClass = !instruInClass;

		return instruInClass;
	}

	// method for instructor to release the students from their class and tell
	// students where to go next when session ends
	public synchronized void releaseStu(int session) {
		if (name.contains("1"))
			msg("sending all students in ELA class home");
		else
			msg("sending all students in MATH class home");
		Student s;
		while (numReleaseMyClass-- > 0) {
			StringBuilder mg = new StringBuilder();
			s = (Student) classRoom.remove(0);
			synchronized (s) {
				if (session == 4) {
					s.instruction = "Home";
					mg.append(s.name + " sent home");
				}

				else {
					s.instruction = "Class2";
					mg.append(s.name + " sent to session " + session);
				}
				if (name.contains("1")) {
					mg.append(". ElaRoom = " + setNumStuInMyClass(2));
				} else {
					mg.append(". Mathroom = " + setNumStuInMyClass(2));
				}

				msg(mg.toString());
				s.notify();
			}
		}
	}

	// method for instructor to add themselves to their own classes, and notify
	// principle if principle is waiting for them to attend their class
	public synchronized void teachClass(int i) {
		classRoom.add(this);

		if (instruInClass(true) && Principal.principalWaiting) {
			synchronized (School.p) {
				Principal.principalWaiting = false;
				School.p.notify();
			}
		}
		
		if (name.contains("2")) 
			msg("teaching math");
		 else
			msg("teaching ela");
		
		waitSessionEnd();
		releaseStu(i + 1);
	}

	// method for instructors to check if session already started when they arrived.
	// If yes, instructor notify the principle that they arrived and go straight to class, 
	//if not, instructors wait for principle to notify them
	public synchronized void SessionStart(int i) {
		// change status to arrived and check the following
		synchronized (this) {
			boolean wait = false;
			synchronized (School.p) {
				if (i == 1) {
					if (name.contains("1")) {
						msg("instruInEla = " + instruInClass(true));
					} else
						msg("instruInMath = " + instruInClass(true));
				}
				// if session has already started, or if school ended due to covid and all
				// students are already in class go to class
				if (Principal.changeSessionStarted(false)
						|| (!School.schoolStatus() && School.incAndcheckStuInClass(false, this))) {
					msg("finds out either session had started, or school ended b/c > 3 students found positive with covid. \n"
							+ "Proceeds to teach/release students.");
					// if principal is waiting, notify that I just arrived
					if (Principal.principalWaiting == true) {
						msg("notified principle I just arrived");
						School.p.notify();
					}
				} else
					wait = true;
			}
			if (wait) {
				msg("waiting for session to start");
				while (true) {
					try {
						this.wait();
						msg("notified");
						break;
					} catch (InterruptedException e) {
						if (Principal.changeSessionStarted(false))
							break;
						else
							continue;
					}
				}
			}
		}
	}

	// method for instructor to wait until notified by principle that session has
	// ended
	private void waitSessionEnd() {
		synchronized (this) {
			while (true) {
				try {
					this.wait();
					msg("notified");
					break;
				} catch (InterruptedException e) {
					if (!Principal.changeSessionStarted(false))
						break;
					else
						continue;
				}
			}
		}

	}

	@Override
	public void run() {
		msg("arrived");

		for (int i = 1; i < 4; i++) {
			SessionStart(i);

			// if school hasn't ended go and teach class
			if (School.schoolStatus()) {
				teachClass(i);
				// rest before next session
				if (i < 2) {
					msg("resting before the next session");
					int sleep = rand.nextInt(50);
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						msg("sleep interrupted");
					}
				}
			}
			// else school ended due to covid. Notify all students to go home
			else {
				setNumStuInMyClass(3);
				releaseStu(4);
				break;
			}
		}
		msg(" Thread terminated");
	}
}
