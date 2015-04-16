import java.util.LinkedList;

public class Kom2 {
	public static void main(String[] args) throws InterruptedException {

		
		Zarzadca z = new Zarzadca();
		//jedno nowe zadanie
		z.addTask(1,new Zadanie()); 
		//sekunda przerwy
		Thread.sleep(1000);
		//10 nowych zadañ
		z.addTask(10,new Zadanie());
		
		z.zamknij();
	}
	

	
}

class Zadanie implements Runnable { // Zadanie wymieniane w w¹tkach
	public static Object lock = new Object();
	static int licznik = 0;
	public void run() {
		synchronized(lock){
		licznik++;
		System.out.println("Zadanie numer " + licznik + " Wykonano");
		}
	}
}


class Watek extends Thread {
	Zarzadca zarzadca;
	public Watek(Zarzadca zarzadca){
		this.zarzadca=zarzadca;
		//this.setDaemon(true); --- za wczeœnie zamyka³o w¹tki
	}
	
	public void run() {
		while (zarzadca.isRunning) {
			zarzadca.getNextTask().run();
		}
	}
}


class Zarzadca{
	public boolean isRunning;
	LinkedList<Watek> watki;
	LinkedList<Runnable> zadania;
	public Zarzadca(){
		isRunning=true;
		watki = new LinkedList<Watek>();
		zadania = new LinkedList<Runnable>();
		int liczbaWatkow = Runtime.getRuntime().availableProcessors();
		for(int i=0;i<liczbaWatkow;i++){
			watki.add(new Watek(this));
			watki.getLast().start();
		}
	}
	public void addTask(int ilosc,Runnable task){
		synchronized(zadania){
			for(int i=0;i<ilosc;i++)zadania.add(task);
			zadania.notify();
		}
	}
	public Runnable getNextTask(){
		while(true){
		synchronized(zadania){
		if(!zadania.isEmpty()){
		
			Runnable temp = zadania.getLast();
			zadania.removeLast();
			return temp;
		
		}else{
			try {
				while(!zadania.isEmpty()){
				zadania.wait();
				}
			}
			catch (InterruptedException e) {}
		}
		}
		}
	}
	public void zamknij(){
		while(!zadania.isEmpty()){} 
		isRunning=false;
		synchronized(zadania){
			zadania.notifyAll();
		}
		for(Watek w:watki){
			try {
				w.join();
			}
			catch (InterruptedException e) {}
		}
	}

}