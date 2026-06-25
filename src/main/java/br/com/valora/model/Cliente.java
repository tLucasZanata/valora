package br.com.valora.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonProperty; // Importe esta anotação!

@Entity
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nome;

    // CORREÇÃO: Mapeia "cpf_cnpj" (do JSON) para o atributo Java
    @JsonProperty("cpf_cnpj")
    private String cpfCnpj;

    private String email;
    private String contato;
    private String endereco;

    // CORREÇÃO: Mapeia "cad_pro" (do JSON) para o atributo Java
    @JsonProperty("cad_pro")
    private String cadPro;

    // CORREÇÃO: Mapeia "endereco_adicional" (do JSON) para o atributo Java
    @JsonProperty("endereco_adicional")
    private String enderecoAdicional;

    public Cliente() {}

    // Construtor completo
    public Cliente(String nome, String cpfCnpj, String email, String contato, String endereco, String cadPro, String enderecoAdicional) {
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.contato = contato;
        this.endereco = endereco;
        this.cadPro = cadPro;
        this.enderecoAdicional = enderecoAdicional;
    }

    // --- Getters e Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContato() { return contato; }
    public void setContato(String contato) { this.contato = contato; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCadPro() { return cadPro; }
    public void setCadPro(String cadPro) { this.cadPro = cadPro; }

    public String getEnderecoAdicional() { return enderecoAdicional; }
    public void setEnderecoAdicional(String enderecoAdicional) { this.enderecoAdicional = enderecoAdicional; }
}