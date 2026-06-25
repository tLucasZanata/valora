package br.com.valora.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nome;
    private String cargo;
    private String contato;
    private double valorHora;
    private String email;


    public Funcionario() {}

    public Funcionario(String nome, String cargo, String contato, double valorHora, String email) {
        this.nome = nome;
        this.cargo = cargo;
        this.contato = contato;
        this.valorHora = valorHora;
        this.email = email;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getContato() { return contato; }
    public void setContato(String contato) { this.contato = contato; }

    public double getValorHora() { return valorHora; }
    public void setValorHora(double valorHora) { this.valorHora = valorHora; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}