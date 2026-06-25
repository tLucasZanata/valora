package br.com.valora.Service.impl;

import br.com.valora.model.Veiculo;
import br.com.valora.repository.VeiculoRepository;
import br.com.valora.Service.VeiculoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class VeiculoServiceImpl implements VeiculoService {

    private final VeiculoRepository veiculoRepository;

    public VeiculoServiceImpl(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    public List<Veiculo> findAll() {
        return veiculoRepository.findAll();
    }

    @Override
    public Optional<Veiculo> findById(Integer id) {
        return veiculoRepository.findById(id);
    }

    @Override
    public Veiculo save(Veiculo veiculo) {
        if (veiculo.getPlaca() != null) {
            veiculo.setPlaca(veiculo.getPlaca().toUpperCase());
        }
        if (veiculo.getValorKm() == null) {
            veiculo.setValorKm(BigDecimal.ZERO);
        }
        return veiculoRepository.save(veiculo);
    }

    @Override
    public Veiculo update(Integer id, Veiculo veiculoDetails) {
        return veiculoRepository.findById(id).map(existente -> {
            existente.setModelo(veiculoDetails.getModelo());
            existente.setPlaca(veiculoDetails.getPlaca().toUpperCase());
            existente.setValorKm(
                    veiculoDetails.getValorKm() != null ? veiculoDetails.getValorKm() : BigDecimal.ZERO
            );
            return veiculoRepository.save(existente);
        }).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        veiculoRepository.deleteById(id);
    }
}
