package br.com.valora.model;

import jakarta.persistence.*;

@Entity
@Table(name = "produto_servico")
public class ProdutoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "registro_servico_id")
    private Integer registroServicoId;

    @Column(name = "produto_id")
    private Integer produtoId;

    private Integer quantidade;

    public ProdutoServico() {}

    public ProdutoServico(Integer registroServicoId, Integer produtoId, Integer quantidade) {
        this.registroServicoId = registroServicoId;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRegistroServicoId() { return registroServicoId; }
    public void setRegistroServicoId(Integer registroServicoId) { this.registroServicoId = registroServicoId; }

    public Integer getProdutoId() { return produtoId; }
    public void setProdutoId(Integer produtoId) { this.produtoId = produtoId; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}