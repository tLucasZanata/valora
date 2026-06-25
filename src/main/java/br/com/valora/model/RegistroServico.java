package br.com.valora.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "registro_servico")
public class RegistroServico {

    // =========================
    // ID
    // =========================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // IDS RELACIONADOS
    // =========================
    @Column(name = "funcionario_id")
    private Long funcionarioId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "servico_id")
    private Long servicoId;

    // =========================
    // VEÍCULO (opcional)
    // =========================
    @Column(name = "veiculo_id")
    private Long veiculoId;

    @Column(name = "km_percorridos")
    private BigDecimal kmPercorridos;

    // =========================
    // DATA
    // =========================
    @Column(name = "data_servico")
    private LocalDate dataServico;

    // =========================
    // HORÁRIOS
    // =========================
    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    // =========================
    // HORAS TRABALHADAS
    // =========================
    // Coluna GENERATED ALWAYS no PostgreSQL — somente leitura
    @Column(name = "horas_trabalhadas", insertable = false, updatable = false)
    private Double horasTrabalhadas;

    // =========================
    // OBSERVAÇÃO
    // =========================
    @Column(name = "observacao")
    private String observacao;

    // =========================
    // STATUS
    // =========================
    @Column(name = "status", nullable = false)
    private String status = "ABERTO";

    // =========================
    // VALOR TOTAL
    // =========================
    // Calculado na view vw_relatorio_horas_valores, não existe como coluna física
    @Transient
    private BigDecimal valorTotal;

    // =========================
    // CONSTRUTOR VAZIO
    // =========================
    public RegistroServico() {
    }

    // =========================
    // GETTERS E SETTERS
    // =========================

    public Long getId() { return id; }

    public Long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }

    public Long getVeiculoId() { return veiculoId; }
    public void setVeiculoId(Long veiculoId) { this.veiculoId = veiculoId; }

    public BigDecimal getKmPercorridos() { return kmPercorridos; }
    public void setKmPercorridos(BigDecimal kmPercorridos) { this.kmPercorridos = kmPercorridos; }

    public LocalDate getDataServico() { return dataServico; }
    public void setDataServico(LocalDate dataServico) { this.dataServico = dataServico; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public Double getHorasTrabalhadas() { return horasTrabalhadas; }
    public void setHorasTrabalhadas(Double horasTrabalhadas) { this.horasTrabalhadas = horasTrabalhadas; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    // =========================
    // FORMATAÇÃO DAS HORAS
    // =========================
    @Transient
    public String getHorasFormatadas() {
        if (horasTrabalhadas == null) return "0h 00m";
        int horas   = horasTrabalhadas.intValue();
        int minutos = (int) Math.round((horasTrabalhadas - horas) * 60);
        return String.format("%dh %02dm", horas, minutos);
    }
}