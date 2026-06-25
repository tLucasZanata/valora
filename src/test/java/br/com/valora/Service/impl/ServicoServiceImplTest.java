package br.com.valora.Service.impl;

import br.com.valora.model.Funcionario;
import br.com.valora.model.Servico;
import br.com.valora.repository.FuncionarioRepository;
import br.com.valora.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ServicoServiceImpl.
 *
 * O ponto mais importante deste Service é o método privado
 * resolverFuncionarios(): o frontend envia apenas os IDs dos funcionários
 * (ex: [{"id": 1}, {"id": 2}]), e o service precisa buscar as entidades
 * completas no banco antes de persistir o relacionamento ManyToMany.
 * Como o método é privado, ele é testado indiretamente através de
 * save() e update(), que são os métodos públicos que o utilizam.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServicoServiceImpl - Testes Unitários")
class ServicoServiceImplTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private ServicoServiceImpl service;

    private Funcionario funcionario1;
    private Funcionario funcionario2;
    private Servico servicoExistente;

    @BeforeEach
    void setUp() {
        funcionario1 = new Funcionario("Laura Mendes", "Veterinária", "(44) 99888-7766", 45.0, "laura@valora.com");
        funcionario1.setId(1);

        funcionario2 = new Funcionario("Carlos Pereira", "Atendente", "(44) 97777-1111", 25.0, "carlos@valora.com");
        funcionario2.setId(2);

        servicoExistente = new Servico();
        servicoExistente.setId(10);
        servicoExistente.setNome("Banho e Tosa");
        servicoExistente.setCodigo("SRV-001");
        servicoExistente.setValorHora(40.0);
        servicoExistente.setIsento(false);
    }

    /** Cria um Funcionario "raso", como o frontend envia: só com o ID preenchido. */
    private Funcionario funcionarioComApenasId(int id) {
        Funcionario f = new Funcionario();
        f.setId(id);
        return f;
    }

    // ════════════════════════════════════════════════════════════════════
    // save() — resolução de funcionários
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("save() - resolução de funcionários")
    class SaveResolverFuncionarios {

        @Test
        @DisplayName("Deve resolver os IDs de funcionários para as entidades completas")
        void deveResolverFuncionariosPorId() {
            Servico novo = new Servico();
            novo.setNome("Consulta Veterinária");
            novo.setFuncionarios(List.of(funcionarioComApenasId(1), funcionarioComApenasId(2)));

            when(funcionarioRepository.findById(1)).thenReturn(Optional.of(funcionario1));
            when(funcionarioRepository.findById(2)).thenReturn(Optional.of(funcionario2));
            when(servicoRepository.save(any(Servico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Servico salvo = service.save(novo);

            assertThat(salvo.getFuncionarios()).hasSize(2);
            assertThat(salvo.getFuncionarios()).extracting(Funcionario::getNome)
                    .containsExactlyInAnyOrder("Laura Mendes", "Carlos Pereira");
        }

        @Test
        @DisplayName("Deve ignorar silenciosamente IDs de funcionários que não existem")
        void deveIgnorarFuncionariosInexistentes() {
            Servico novo = new Servico();
            novo.setNome("Tosa Simples");
            novo.setFuncionarios(List.of(funcionarioComApenasId(1), funcionarioComApenasId(999)));

            when(funcionarioRepository.findById(1)).thenReturn(Optional.of(funcionario1));
            when(funcionarioRepository.findById(999)).thenReturn(Optional.empty());
            when(servicoRepository.save(any(Servico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Servico salvo = service.save(novo);

            // Apenas o funcionário 1 (existente) deve aparecer na lista final
            assertThat(salvo.getFuncionarios()).hasSize(1);
            assertThat(salvo.getFuncionarios().get(0).getNome()).isEqualTo("Laura Mendes");
        }

        @Test
        @DisplayName("Deve salvar com lista de funcionários vazia quando nenhum é enviado")
        void deveSalvarComListaVaziaQuandoSemFuncionarios() {
            Servico novo = new Servico();
            novo.setNome("Serviço sem funcionário definido");
            novo.setFuncionarios(new ArrayList<>());

            when(servicoRepository.save(any(Servico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Servico salvo = service.save(novo);

            assertThat(salvo.getFuncionarios()).isEmpty();
            verify(funcionarioRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Deve salvar com lista vazia quando a lista de funcionários é nula")
        void deveSalvarComListaVaziaQuandoFuncionariosNulo() {
            Servico novo = new Servico();
            novo.setNome("Serviço com funcionarios null");
            novo.setFuncionarios(null);

            when(servicoRepository.save(any(Servico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Servico salvo = service.save(novo);

            assertThat(salvo.getFuncionarios()).isNotNull().isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // update()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Deve atualizar todos os campos básicos do serviço")
        void deveAtualizarCamposBasicos() {
            when(servicoRepository.findById(10)).thenReturn(Optional.of(servicoExistente));
            when(servicoRepository.save(any(Servico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Servico detalhes = new Servico();
            detalhes.setNome("Banho e Tosa Premium");
            detalhes.setCodigo("SRV-001-B");
            detalhes.setDescricao("Inclui hidratação");
            detalhes.setObservacoes("Agendamento prévio necessário");
            detalhes.setValorHora(60.0);
            detalhes.setIsento(false);
            detalhes.setFuncionarios(new ArrayList<>());

            Servico atualizado = service.update(10, detalhes);

            assertThat(atualizado.getNome()).isEqualTo("Banho e Tosa Premium");
            assertThat(atualizado.getCodigo()).isEqualTo("SRV-001-B");
            assertThat(atualizado.getDescricao()).isEqualTo("Inclui hidratação");
            assertThat(atualizado.getValorHora()).isEqualTo(60.0);
        }

        @Test
        @DisplayName("Deve re-resolver a lista de funcionários ao atualizar")
        void deveReResolverFuncionariosAoAtualizar() {
            when(servicoRepository.findById(10)).thenReturn(Optional.of(servicoExistente));
            when(funcionarioRepository.findById(2)).thenReturn(Optional.of(funcionario2));
            when(servicoRepository.save(any(Servico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Servico detalhes = new Servico();
            detalhes.setNome("Banho e Tosa");
            detalhes.setFuncionarios(List.of(funcionarioComApenasId(2)));

            Servico atualizado = service.update(10, detalhes);

            assertThat(atualizado.getFuncionarios()).hasSize(1);
            assertThat(atualizado.getFuncionarios().get(0).getNome()).isEqualTo("Carlos Pereira");
        }

        @Test
        @DisplayName("Deve marcar o serviço como isento quando isento=true é enviado")
        void deveMarcarComoIsento() {
            when(servicoRepository.findById(10)).thenReturn(Optional.of(servicoExistente));
            when(servicoRepository.save(any(Servico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Servico detalhes = new Servico();
            detalhes.setIsento(true);
            detalhes.setFuncionarios(new ArrayList<>());

            Servico atualizado = service.update(10, detalhes);

            assertThat(atualizado.getIsento()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar null quando o serviço não existe")
        void deveRetornarNullQuandoNaoExiste() {
            when(servicoRepository.findById(999)).thenReturn(Optional.empty());

            Servico resultado = service.update(999, new Servico());

            assertThat(resultado).isNull();
            verify(servicoRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // findAll() / findById() / delete()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Consultas e exclusão")
    class ConsultasEExclusao {

        @Test
        @DisplayName("findAll deve retornar todos os serviços cadastrados")
        void findAllDeveRetornarTodos() {
            Servico outro = new Servico();
            outro.setNome("Vacinação");
            when(servicoRepository.findAll()).thenReturn(List.of(servicoExistente, outro));

            List<Servico> resultado = service.findAll();

            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("findById deve retornar o serviço quando existe")
        void findByIdDeveRetornarServicoExistente() {
            when(servicoRepository.findById(10)).thenReturn(Optional.of(servicoExistente));

            Optional<Servico> resultado = service.findById(10);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNome()).isEqualTo("Banho e Tosa");
        }

        @Test
        @DisplayName("findById deve retornar Optional vazio quando não existe")
        void findByIdDeveRetornarVazioQuandoNaoExiste() {
            when(servicoRepository.findById(999)).thenReturn(Optional.empty());

            Optional<Servico> resultado = service.findById(999);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("delete deve chamar deleteById no repositório com o id correto")
        void deleteDeveChamarRepositorioComIdCorreto() {
            service.delete(10);

            verify(servicoRepository, times(1)).deleteById(10);
        }
    }
}