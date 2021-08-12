
import java.util.*;

//this class represents the school
public class School {
	//common program start time for everyone
	public static long time = System.currentTimeMillis();
	
	
	//principle, nurse and instructors objects
	public static Principal p = new Principal(1);
	public static Nurse n = new Nurse(1);
	public static Instructor ElaInstructor = new Instructor(1), MathInstructor = new Instructor(2);
	
	
	//int variables to keep track of number of students in school and in class
	public static int NumStuInSchool = 0, NumStuInClass = 0;
	
	//arrayList representing the school yard
	public static PriorityQueue<Student> schoolYard = new PriorityQueue<>();
	
	//boolean variable to represent if school started or ended
	static boolean started = false, ended = false;

	//method for principal to declare that school has started
	public static synchronized int startSchool() {
		started = true;
		return schoolYard.size();
	}
	
	//method for principal to declare that school has ended
	public static synchronized void endSchool() {
		System.out.println("school ended");
		started = false;
		ended = true;
	}
	
	//method for students to add themselves to schoolYard ArrayList and set their arrival time
	public static synchronized boolean arriveAtSchool(Student s, boolean covidForm) {
		if(!started && !ended) {
			s.arrivalTime = System.currentTimeMillis();
			schoolYard.add(s);
			s.msg("arrived school. covidForm = " + covidForm + "; SchoolYard = " + 
					schoolYard.size());
			return true;
		}
		return false;
	}
	
	//method to get the school status
	public static synchronized boolean schoolStatus() {
		return started;
	}
	
	//method for principal to set the number of students in school after the sort through all 
	//the students
	public static synchronized int setNumStuInSchool(int num) {
		NumStuInSchool = num;
		return NumStuInSchool;
	}
	
	//method to increase number of students in class everytime when a student enters
	//a class
	public static synchronized boolean incAndcheckStuInClass(boolean inc, Person p) {
		if(inc)
			NumStuInClass++;
		p.msg("NumStuInClass = " + NumStuInClass + "; NumStuInSchool = " + NumStuInSchool);
		
		return (NumStuInSchool == NumStuInClass);
	}
	
	//method to reset the number of students in class to 0 after each session ends
	public static synchronized void resetStuInClass() {
		NumStuInClass = 0;
	}
	
	//method to decrease number of students in school everytime when principal/nurse sends
	//a student home
	public static synchronized int decNumStuInSchool(Person p) {
		return --NumStuInSchool;
	}
	
	//method to get the number of students in shcool
	public static synchronized int getNumStuInSchool() {
		return NumStuInSchool;
	}
	
	//method to check the number of students that arrived school before school starts
	public static synchronized int NumStudentArrived() {
		return schoolYard.size();
	}
	
	//method to retrieve the next student in in school yard
	public static synchronized Student getNextStudent() {
		if(!schoolYard.isEmpty()) {
			return schoolYard.remove();
		}
		return null;
	}
}
