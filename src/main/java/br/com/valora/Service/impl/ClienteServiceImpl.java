package br.com.valora.Service.impl;

import br.com.valora.model.Cliente;
import br.com.valora.repository.ClienteRepository;
import br.com.valora.Service.ClienteService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    @Override
    public Optional<Cliente> findById(Integer id) {
        return clienteRepository.findById(id);
    }

    @Override
    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente update(Integer id, Cliente clienteDetails) {
        // Busca o cliente existente pelo ID
        return clienteRepository.findById(id).map(clienteExistente -> {


            clienteExistente.setNome(clienteDetails.getNome());
            clienteExistente.setCpfCnpj(clienteDetails.getCpfCnpj());
            clienteExistente.setEmail(clienteDetails.getEmail());
            clienteExistente.setContato(clienteDetails.getContato());
            clienteExistente.setEndereco(clienteDetails.getEndereco());
            clienteExistente.setCadPro(clienteDetails.getCadPro());
            clienteExistente.setEnderecoAdicional(clienteDetails.getEnderecoAdicional());

            return clienteRepository.save(clienteExistente);

        }).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        clienteRepository.deleteById(id);
    }
}
