//this class represent all the student threads
public class Student extends Person implements Runnable,  Comparable<Student>{
	//keeps the arrival time of when student arrive school
	long arrivalTime;
	
	//keeps record of whether student completed their covid form or not
	public boolean covidForm;
	
	//the original instruction for student threads is to go to school
	String instruction = "School";
	
	//int variable for student to keep track of which class they need to attend next
	int needToAttend = 0;
	
	//keeps records of all the classes attended by each student through out the day
	StringBuilder attendance = new StringBuilder();
	

	//constructor method
	public Student(int id) {
		setName("Student" + id);
		attendance.append("attended: ");
	}
	
	//used for automatic sorting of the order of students according to their arrival time 
	//in the school yard
	@Override
	public int compareTo(Student st) {
		if(arrivalTime < st.getArrivalTime())
			return -1;
		else
			return 1;
		
	}
	
	//checks if instruction is to go home
	public boolean goHome() {
		synchronized(this) {
			return (instruction == "Home");
		}
	}
	
	//getter method for student arrival time
	public long getArrivalTime() {
		return arrivalTime;
	}
	
	//use the random number method to determine if student fillout their covid form or not
	public void filloutCovidForm() {
		if(rand.nextInt(19) < (20 * 0.15)) {
			covidForm = false;
		}
		else {
			covidForm = true;
		}	
	}
	
	//method for everytime when student need to wait on whatever instruction
	public void waiting(String oldInstr) {
		while(true) {
			try {
				this.wait(); 
				break;
			}
			catch(InterruptedException e) {
				msg("THREAD INTERRUPTED!!!");
				if(!instruction.equals(oldInstr)) 
					break;
				else
					continue;
			}
		}
	}
	
	//method for student to try and go to school, if school already started, it will go home
	public boolean gotoSchool() {
		if(School.arriveAtSchool(this, covidForm)) {
			return true;
		}
		else {
			instruction = "Home";
			msg("arrived late to school, going home.");
			return false;
		}
	}

	//method for student to try and go to the appropriate classes
	public void gotoClass() {
		if(needToAttend == 0) {
			if(!School.ElaInstructor.attendClass(this)) {
				if(!School.MathInstructor.attendClass(this))
					School.p.attendPE(this);
				else
					needToAttend = 1;
			}
			else
				needToAttend = 2;
		}

		else if(needToAttend == 1) {
			if(!School.ElaInstructor.attendClass(this)) {
				School.p.attendPE(this);		
			}
			else 
				needToAttend = 3;
		}	
		else if(needToAttend == 2) {
			if(!School.MathInstructor.attendClass(this)) {
				School.p.attendPE(this);
			}
			else 
				needToAttend = 3;
		}		
		else
			School.p.attendPE(this);
		
		//after each student entered their classroom, they inc the number of students in class, 
		//and check if all students are in class. if yes notify the principal. 
		if(School.incAndcheckStuInClass(true, this)) {			
			synchronized(School.p) {
				if(!Principal.changeSessionStarted(false)) {
					School.p.notify();
					msg("All students in class, notified principle");
				}
			}	
		}
	}
	
	//method for student to wait in nurseWaitRoom. They notify the nurse after they get
	//a partner and if nurse is waiting
	public void gotoNurse() {	
		if(Nurse.waitAtNurseRoom(this, true) >= 2 || (Nurse.numStudentLeft() == 1)) {
			synchronized(School.n) {
				if(Nurse.updateNurseWaiting(false)) {
					Nurse.updateNurseWaiting(true);
					msg("notifying Nurse");
					School.n.notify();
				}
			}
		}	
	}
	
	//method for students to check the next instruction and determine where to go
	public void decideWheretoGo() {
		switch(instruction) {
		case "Nurse":
			synchronized(this) {
				gotoNurse();
				waiting(instruction);
			}
			break;
			
		case "Class1":
			synchronized(this) {
				gotoClass();
				waiting(instruction);
			}
			break;
		
		case "Class2":
			//students rest between classes
			int sleep = rand.nextInt(50);
			try {
				Thread.sleep(sleep);
				} catch (InterruptedException e) {
					msg("sleep interrupted");
				}
			
			synchronized(this) {
				gotoClass();
				waiting(instruction);
			}	
			break;
		}
	}	 
	
	@Override
	public void run() {
		//students rests before going to school
		int sleep = rand.nextInt(100);
		try {
			Thread.sleep(sleep);
			} catch (InterruptedException e) {
				msg("sleep interrupted");
			}
		
		
		filloutCovidForm();
		
		synchronized(this) {
			if(gotoSchool())
				waiting(instruction);
		}

		while(!goHome()) {
			decideWheretoGo();
		}	
		
		
		if(attendance.length() != 10) { 
			msg(attendance.toString()); 
		}
		
		msg(" Thread terminated");
	}
}
	
