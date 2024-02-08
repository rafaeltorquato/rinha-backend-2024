drop procedure if exists rinha.processa_transacao;
DELIMITER |
create procedure rinha.processa_transacao(
    IN in_id_cliente int,
    IN in_valor int,
    IN in_descricao varchar(10),
    OUT out_limite int,
    OUT out_saldo int)
BEGIN
    declare _saldo int;
    declare _limite int;
    declare _data_hora datetime(6);

    select cliente.saldo, cliente.limite
    into _saldo, _limite
    from rinha.cliente
    where id = in_id_cliente for
    update;

    set _data_hora = now(6);
    set _saldo = _saldo + in_valor;
    if _saldo <= (_limite * -1) then
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Saldo invalido';
    else
        insert into rinha.transacao(valor, descricao, realizada_em, id_cliente)
        values (in_valor, in_descricao, _data_hora, in_id_cliente);

        update rinha.cliente set saldo = _saldo where id = in_id_cliente;

        set out_saldo = _saldo;
        set out_limite = _limite;
    end if;
END |

delimiter ;