package br.com.valora.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "veiculo")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String modelo;

    private String placa;

    // Valor cobrado por quilômetro rodado (específico de cada veículo)
    @Column(name = "valor_km", nullable = false)
    private BigDecimal valorKm = BigDecimal.ZERO;

    public Veiculo() {}

    public Veiculo(String modelo, String placa, BigDecimal valorKm) {
        this.modelo   = modelo;
        this.placa    = placa;
        this.valorKm  = valorKm != null ? valorKm : BigDecimal.ZERO;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public BigDecimal getValorKm() { return valorKm; }
    public void setValorKm(BigDecimal valorKm) {
        this.valorKm = valorKm != null ? valorKm : BigDecimal.ZERO;
    }
}