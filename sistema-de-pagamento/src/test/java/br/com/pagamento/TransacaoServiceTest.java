package br.com.pagamento;

import br.com.pagamento.entity.Transacao;
import br.com.pagamento.repository.TransacaoRepository;
import br.com.pagamento.service.TransacaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransacaoServiceTest {

    @InjectMocks
    private TransacaoService transacaoService;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Test
    public void testlistTotaisByNomeDaLoja() {

        final String lojaA= "Loja A", lojaB= "Loja B";

        var transacao1 = new Transacao(
                2L,
                1,
                new Date(System.currentTimeMillis()),
                BigDecimal.valueOf(75.00),
                987654321L,
                "9876-5432-1098-7654",
                new Time(System.currentTimeMillis()),
                "Dono da loja A",
                lojaA);

        var transacao2 = new Transacao(
                3L,
                1,
                new Date(System.currentTimeMillis()),
                BigDecimal.valueOf(50.00),
                111222333L,
                "1111-2222-3333-4444",
                new Time(System.currentTimeMillis()),
                "Dono da loja B",
                lojaB);

        var transacao3 = new Transacao(
                1L,
                1,
                new Date(System.currentTimeMillis()),
                BigDecimal.valueOf(100),
                123456789L,
                "1234-5678-9012-3456",
                new Time(System.currentTimeMillis()),
                "Dono da loja A",
                lojaA);

        var mockTransacoes = List.of(transacao1, transacao2, transacao3);

        when(transacaoRepository.findAllByOrderByNomeDaLojaAscIdDesc())
                .thenReturn(mockTransacoes);
        var reports = transacaoService.listTransacoesTotaisPorNomeDaLoja();

        assertEquals(2, reports.size());

        reports.forEach(report -> {
            if(report.nomeDaLoja().equals(lojaA)) {
                assertEquals(2, report.transacoes().size());
                assertEquals(BigDecimal.valueOf(175.0), report.total());
                assertTrue(report.transacoes().contains(transacao1));
                assertTrue(report.transacoes().contains(transacao3));
            } else if(report.nomeDaLoja().equals(lojaB)) {
                assertEquals(1, report.transacoes().size());
                assertEquals(BigDecimal.valueOf(50.0), report.total());
                assertTrue(report.transacoes().contains(transacao2));
            }
        });
    }
}
