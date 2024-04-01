package app;

public class Usuario {
	protected String nome;
	protected boolean admin;
	
	public String getNome() {
		return nome;
	}
	public boolean getAdmin() {
		return admin;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
}
