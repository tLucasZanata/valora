package br.com.valora.Service.impl;

import br.com.valora.Service.impl.RegistroServicoServiceImpl;
import br.com.valora.model.Produto;
import br.com.valora.model.ProdutoServico;
import br.com.valora.model.RegistroServico;
import br.com.valora.repository.ProdutoRepository;
import br.com.valora.repository.ProdutoServicoRepository;
import br.com.valora.repository.RegistroServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RegistroServicoServiceImpl.
 *
 * Estratégia: os repositórios (RegistroServicoRepository, ProdutoRepository,
 * ProdutoServicoRepository) são "mockados" — ou seja, são objetos falsos que
 * simulam o comportamento do banco de dados sem precisar de um banco real.
 * Isso torna os testes rápidos e isolados, focando apenas na regra de negócio
 * implementada na classe de serviço.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegistroServicoServiceImpl - Testes Unitários")
class RegistroServicoServiceImplTest {

    @Mock
    private RegistroServicoRepository registroServicoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoServicoRepository produtoServicoRepository;

    @InjectMocks
    private RegistroServicoServiceImpl service;

    private RegistroServico registroAberto;
    private RegistroServico registroFechado;
    private Produto produtoComEstoque;
    private ProdutoServico itemProduto;

    @BeforeEach
    void setUp() {
        registroAberto = new RegistroServico();
        registroAberto.setStatus("ABERTO");

        registroFechado = new RegistroServico();
        registroFechado.setStatus("FECHADO");

        produtoComEstoque = new Produto("PRD-00001", "Prato", "Prato comedouro", 26.90, 100);
        produtoComEstoque.setId(1);

        itemProduto = new ProdutoServico(10, 1, 5); // 5 unidades do produto 1 no registro 10
    }

    // ════════════════════════════════════════════════════════════════════
    // save()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("Deve salvar novo lançamento sempre com status ABERTO")
        void deveSalvarComStatusAberto() {
            RegistroServico novo = new RegistroServico();
            novo.setStatus(null); // simula que o frontend não enviou status

            when(registroServicoRepository.save(any(RegistroServico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RegistroServico salvo = service.save(novo);

            assertThat(salvo.getStatus()).isEqualTo("ABERTO");
            verify(registroServicoRepository, times(1)).save(novo);
        }

        @Test
        @DisplayName("Deve forçar status ABERTO mesmo se outro status for enviado")
        void deveForcarStatusAbertoIgnorandoEntrada() {
            RegistroServico novo = new RegistroServico();
            novo.setStatus("FECHADO"); // tentativa indevida de já criar fechado

            when(registroServicoRepository.save(any(RegistroServico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RegistroServico salvo = service.save(novo);

            assertThat(salvo.getStatus()).isEqualTo("ABERTO");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // confirmarEstoque()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("confirmarEstoque()")
    class ConfirmarEstoque {

        @Test
        @DisplayName("Deve descontar a quantidade exata do produto utilizado")
        void deveDescontarEstoqueCorretamente() {
            when(registroServicoRepository.findById(10)).thenReturn(Optional.of(registroAberto));
            when(produtoServicoRepository.findByRegistroServicoId(10)).thenReturn(List.of(itemProduto));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produtoComEstoque));

            service.confirmarEstoque(10);

            // Estoque inicial era 100, item pede 5 → deve sobrar 95
            verify(produtoRepository).save(argThat(p -> p.getQuantidade() == 95));
        }

        @Test
        @DisplayName("Deve lançar 409 quando o estoque é insuficiente")
        void deveLancarErroQuandoEstoqueInsuficiente() {
            Produto produtoComPouco = new Produto("PRD-00002", "Ração", "Ração premium", 50.0, 3);
            produtoComPouco.setId(1);
            ProdutoServico itemQuerendoMais = new ProdutoServico(10, 1, 5); // pede 5, só tem 3

            when(registroServicoRepository.findById(10)).thenReturn(Optional.of(registroAberto));
            when(produtoServicoRepository.findByRegistroServicoId(10)).thenReturn(List.of(itemQuerendoMais));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produtoComPouco));

            assertThatThrownBy(() -> service.confirmarEstoque(10))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Estoque insuficiente");

            // Garante que NENHUM produto foi salvo (operação tudo-ou-nada)
            verify(produtoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar 404 quando o lançamento não existe")
        void deveLancar404QuandoLancamentoNaoExiste() {
            when(registroServicoRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.confirmarEstoque(999))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("não encontrado");
        }

        @Test
        @DisplayName("Não deve alterar estoque se a lista de produtos estiver vazia")
        void naoDeveAlterarEstoqueSemProdutos() {
            when(registroServicoRepository.findById(10)).thenReturn(Optional.of(registroAberto));
            when(produtoServicoRepository.findByRegistroServicoId(10)).thenReturn(List.of());

            service.confirmarEstoque(10);

            verify(produtoRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // update()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Deve bloquear edição de lançamento FECHADO")
        void deveBloquearEdicaoDeFechado() {
            when(registroServicoRepository.findById(10)).thenReturn(Optional.of(registroFechado));

            RegistroServico detalhes = new RegistroServico();

            assertThatThrownBy(() -> service.update(10, detalhes))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("FECHADO");

            // Nada deve ser salvo nem o estoque devolvido
            verify(registroServicoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve devolver estoque antigo antes de salvar as alterações")
        void deveDevolverEstoqueAntesDeAtualizar() {
            when(registroServicoRepository.findById(10)).thenReturn(Optional.of(registroAberto));
            when(produtoServicoRepository.findByRegistroServicoId(10)).thenReturn(List.of(itemProduto));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produtoComEstoque));
            when(registroServicoRepository.save(any(RegistroServico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RegistroServico detalhes = new RegistroServico();
            detalhes.setObservacao("Atualizado");

            service.update(10, detalhes);

            // Estoque tinha 100, devolveu 5 → deve estar com 105
            verify(produtoRepository).save(argThat(p -> p.getQuantidade() == 105));
        }

        @Test
        @DisplayName("Deve retornar null quando o lançamento não existe")
        void deveRetornarNullQuandoNaoExiste() {
            when(registroServicoRepository.findById(999)).thenReturn(Optional.empty());

            RegistroServico resultado = service.update(999, new RegistroServico());

            assertThat(resultado).isNull();
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // delete()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Deve bloquear exclusão de lançamento FECHADO")
        void deveBloquearExclusaoDeFechado() {
            when(registroServicoRepository.findById(10)).thenReturn(Optional.of(registroFechado));

            assertThatThrownBy(() -> service.delete(10))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("FECHADO");

            verify(registroServicoRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve devolver estoque ao excluir lançamento ABERTO")
        void deveDevolverEstoqueAoExcluir() {
            when(registroServicoRepository.findById(10)).thenReturn(Optional.of(registroAberto));
            when(produtoServicoRepository.findByRegistroServicoId(10)).thenReturn(List.of(itemProduto));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produtoComEstoque));

            service.delete(10);

            verify(produtoRepository).save(argThat(p -> p.getQuantidade() == 105));
            verify(registroServicoRepository).deleteById(10);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // alterarStatus()
    // ════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("alterarStatus()")
    class AlterarStatus {

        @Test
        @DisplayName("Deve rejeitar status inválido")
        void deveRejeitarStatusInvalido() {
            assertThatThrownBy(() -> service.alterarStatus(10, "CANCELADO"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("inválido");

            verify(registroServicoRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Deve devolver estoque ao reabrir um lançamento FECHADO")
        void deveDevolverEstoqueAoReabrir() {
            when(registroServicoRepository.findById(10)).thenReturn(Optional.of(registroFechado));
            when(produtoServicoRepository.findByRegistroServicoId(10)).thenReturn(List.of(itemProduto));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produtoComEstoque));
            when(registroServicoRepository.save(any(RegistroServico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RegistroServico resultado = service.alterarStatus(10, "ABERTO");

            verify(produtoRepository).save(argThat(p -> p.getQuantidade() == 105));
            assertThat(resultado.getStatus()).isEqualTo("ABERTO");
        }

        @Test
        @DisplayName("Não deve tocar no estoque ao mudar de EM_ANDAMENTO para FECHADO")
        void naoDeveAlterarEstoqueDeEmAndamentoParaFechado() {
            RegistroServico registroEmAndamento = new RegistroServico();
            registroEmAndamento.setStatus("EM_ANDAMENTO");

            when(registroServicoRepository.findById(10)).thenReturn(Optional.of(registroEmAndamento));
            when(registroServicoRepository.save(any(RegistroServico.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            RegistroServico resultado = service.alterarStatus(10, "FECHADO");

            verify(produtoRepository, never()).save(any());
            verify(produtoServicoRepository, never()).findByRegistroServicoId(any());
            assertThat(resultado.getStatus()).isEqualTo("FECHADO");
        }

        @Test
        @DisplayName("Deve lançar 404 ao alterar status de lançamento inexistente")
        void deveLancar404QuandoNaoExiste() {
            when(registroServicoRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.alterarStatus(999, "FECHADO"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("não encontrado");
        }
    }
}
