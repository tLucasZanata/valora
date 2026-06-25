package br.com.valora.Service;

import br.com.valora.model.RegistroServico;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface RegistroServicoService {
    List<RegistroServico> findAll();
    Optional<RegistroServico> findById(Integer id);
    RegistroServico save(RegistroServico registroServico);
    RegistroServico update(Integer id, RegistroServico registroServicoDetails);
    void delete(Integer id);
    RegistroServico alterarStatus(Integer id, String novoStatus);
    RegistroServico confirmarEstoque(Integer id);
}