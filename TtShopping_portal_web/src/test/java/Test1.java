
public class Test1 {
	
	private int i=0;
	
	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public void add(int i){
		++i;
		this.setI(i);
	}
	
	public static void main(String[] args) {
		Test1 t = new Test1();
		t.add(t.getI());
		System.out.println(t.i);
	}
}
