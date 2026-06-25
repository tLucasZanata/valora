package br.com.valora.Service.impl;

import br.com.valora.Service.FuncionarioServiceImpl;
import br.com.valora.model.Funcionario;
import br.com.valora.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para FuncionarioServiceImpl.
 *
 * CRUD simples sobre o FuncionarioRepository (mockado). O foco principal
 * é garantir que update() copia corretamente todos os campos, incluindo
 * o valorHora (usado depois nos cálculos de lançamento de serviço).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FuncionarioServiceImpl - Testes Unitários")
class FuncionarioServiceImplTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private FuncionarioServiceImpl service;

    private Funcionario funcionarioExistente;

    @BeforeEach
    void setUp() {
        funcionarioExistente = new Funcionario("Laura Mendes", "Veterinária", "(44) 99888-7766", 45.0, "laura@valora.com");
        funcionarioExistente.setId(2);
    }

    // ════════════════════════════════════════════════════════════════════
    // save()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("Deve salvar um novo funcionário delegando diretamente ao repositório")
        void deveSalvarNovoFuncionario() {
            Funcionario novo = new Funcionario("Carlos Pereira", "Atendente", "(44) 97777-1111", 25.0, "carlos@valora.com");

            when(funcionarioRepository.save(novo)).thenReturn(novo);

            Funcionario salvo = service.save(novo);

            assertThat(salvo.getNome()).isEqualTo("Carlos Pereira");
            assertThat(salvo.getValorHora()).isEqualTo(25.0);
            verify(funcionarioRepository, times(1)).save(novo);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // update()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Deve atualizar nome, cargo, contato, valorHora e email")
        void deveAtualizarTodosOsCampos() {
            when(funcionarioRepository.findById(2)).thenReturn(Optional.of(funcionarioExistente));
            when(funcionarioRepository.save(any(Funcionario.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Funcionario detalhes = new Funcionario("Laura Mendes Silva", "Veterinária Sênior",
                    "(44) 91111-0000", 65.0, "laura.silva@valora.com");

            Funcionario atualizado = service.update(2, detalhes);

            assertThat(atualizado.getNome()).isEqualTo("Laura Mendes Silva");
            assertThat(atualizado.getCargo()).isEqualTo("Veterinária Sênior");
            assertThat(atualizado.getContato()).isEqualTo("(44) 91111-0000");
            assertThat(atualizado.getValorHora()).isEqualTo(65.0);
            assertThat(atualizado.getEmail()).isEqualTo("laura.silva@valora.com");
        }

        @Test
        @DisplayName("Deve manter o mesmo ID do funcionário original após a atualização")
        void deveManterIdOriginal() {
            when(funcionarioRepository.findById(2)).thenReturn(Optional.of(funcionarioExistente));
            when(funcionarioRepository.save(any(Funcionario.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Funcionario detalhes = new Funcionario("Outro Nome", "Outro Cargo", "Outro Contato", 30.0, "outro@valora.com");

            Funcionario atualizado = service.update(2, detalhes);

            assertThat(atualizado.getId()).isEqualTo(2);
        }

        @Test
        @DisplayName("Deve atualizar corretamente quando o novo valorHora é maior que o anterior")
        void deveAtualizarValorHoraParaMaior() {
            when(funcionarioRepository.findById(2)).thenReturn(Optional.of(funcionarioExistente));
            when(funcionarioRepository.save(any(Funcionario.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Funcionario detalhes = new Funcionario("Laura Mendes", "Veterinária", "(44) 99888-7766", 120.0, "laura@valora.com");

            Funcionario atualizado = service.update(2, detalhes);

            assertThat(atualizado.getValorHora()).isEqualTo(120.0);
        }

        @Test
        @DisplayName("Deve retornar null quando o funcionário não existe")
        void deveRetornarNullQuandoNaoExiste() {
            when(funcionarioRepository.findById(999)).thenReturn(Optional.empty());

            Funcionario resultado = service.update(999, new Funcionario());

            assertThat(resultado).isNull();
            verify(funcionarioRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // findAll() / findById() / delete()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Consultas e exclusão")
    class ConsultasEExclusao {

        @Test
        @DisplayName("findAll deve retornar todos os funcionários cadastrados")
        void findAllDeveRetornarTodos() {
            Funcionario outro = new Funcionario("Pedro Alves", "Atendente", "(44) 90000-0000", 22.0, "pedro@valora.com");
            when(funcionarioRepository.findAll()).thenReturn(List.of(funcionarioExistente, outro));

            List<Funcionario> resultado = service.findAll();

            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("findById deve retornar o funcionário quando existe")
        void findByIdDeveRetornarFuncionarioExistente() {
            when(funcionarioRepository.findById(2)).thenReturn(Optional.of(funcionarioExistente));

            Optional<Funcionario> resultado = service.findById(2);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNome()).isEqualTo("Laura Mendes");
        }

        @Test
        @DisplayName("findById deve retornar Optional vazio quando não existe")
        void findByIdDeveRetornarVazioQuandoNaoExiste() {
            when(funcionarioRepository.findById(999)).thenReturn(Optional.empty());

            Optional<Funcionario> resultado = service.findById(999);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("delete deve chamar deleteById no repositório com o id correto")
        void deleteDeveChamarRepositorioComIdCorreto() {
            service.delete(2);

            verify(funcionarioRepository, times(1)).deleteById(2);
        }
    }
}
