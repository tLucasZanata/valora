package br.com.valora.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    // CORRIGIDO: campo renomeado de 'codigoInterno' para 'codigo'
    // para bater com o que o frontend envia/exibe
    private String codigo;

    private String descricao;
    private String observacoes;
    private Double valorHora;
    private Boolean isento = false;

    // CORRIGIDO: substituído 'funcionarioId' (Integer simples) por uma lista
    // de Funcionario com relacionamento ManyToMany.
    // Isso permite armazenar múltiplos funcionários aptos por serviço
    // e retorná-los com nome para exibição na tabela.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "servico_funcionario",
            joinColumns = @JoinColumn(name = "servico_id"),
            inverseJoinColumns = @JoinColumn(name = "funcionario_id")
    )
    private List<Funcionario> funcionarios = new ArrayList<>();

    public Servico() {}

    // Getters e Setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Double getValorHora() { return valorHora; }
    public void setValorHora(Double valorHora) { this.valorHora = valorHora; }

    public Boolean getIsento() { return isento != null && isento; }
    public void setIsento(Boolean isento) { this.isento = isento; }

    public List<Funcionario> getFuncionarios() { return funcionarios; }
    public void setFuncionarios(List<Funcionario> funcionarios) { this.funcionarios = funcionarios; }

    @Override
    public String toString() {
        return "Servico {" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", codigo='" + codigo + '\'' +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}