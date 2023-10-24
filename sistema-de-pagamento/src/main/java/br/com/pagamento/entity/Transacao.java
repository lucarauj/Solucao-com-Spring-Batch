package br.com.pagamento.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public record Transacao (
        @Id
        Long id,
        Integer tipo,
        Date data,
        BigDecimal valor,
        Long cpf,
        String cartao,
        Time hora,
        @Column("DONO_LOJA")
        String donoDaLoja,
        @Column("NOME_LOJA")
        String nomeDaLoja

) {
    public Transacao withValor(BigDecimal valor) {
        return new Transacao(
                id(),
                tipo(),
                data(),
                valor,
                cpf(),
                cartao(),
                hora(),
                donoDaLoja(),
                nomeDaLoja()
        );
    }

    public Transacao withData(String data) throws ParseException {
        var dateFormat = new SimpleDateFormat("yyyyMMdd");
        var date = dateFormat.parse(data);

        return new Transacao(
                id(),
                tipo(),
                new Date(date.getTime()),
                valor(),
                cpf(),
                cartao(),
                hora(),
                donoDaLoja(),
                nomeDaLoja()
        );
    }

    public Transacao withHora(String hora) throws ParseException {
        var dateFormat = new SimpleDateFormat("HHmmss");
        var date = dateFormat.parse(hora);

        return new Transacao(
                id(),
                tipo(),
                data(),
                valor(),
                cpf(),
                cartao(),
                new Time(date.getTime()),
                donoDaLoja(),
                nomeDaLoja()
        );
    }
}
