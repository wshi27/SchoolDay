import java.util.Random;

//abstract parent class for all principal, nurse, instructors and students
public abstract class Person {
	public String name;
	public Random rand = new Random();
	
	public void setName(String name ) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void msg(String m) {
		System.out.println("[" + (System.currentTimeMillis() - School.time) + "] " + getName() + ": " + m);
	}

}
