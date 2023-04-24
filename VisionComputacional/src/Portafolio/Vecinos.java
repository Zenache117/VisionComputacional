package Portafolio;

public class Vecinos {

	private int i;
	private int j;
	private int valor;
	private int vecindario = -1;
	private boolean borde = false;
	
	public int getValor() {
		return valor;
	}
	public void setValor(int valor) {
		this.valor = valor;
	}
	public int getI() {
		return i;
	}
	public void setI(int i) {
		this.i = i;
	}
	public int getJ() {
		return j;
	}
	public void setJ(int j) {
		this.j = j;
	}
	public int getVecindario() {
		return vecindario;
	}
	public void setVecindario(int vecindario) {
		this.vecindario = vecindario;
	}
	public boolean isBorde() {
		return borde;
	}
	public void setBorde(boolean borde) {
		this.borde = borde;
	}
	
	
	
}
