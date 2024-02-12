drop database if exists rinha;
create database rinha;
SET GLOBAL TRANSACTION ISOLATION LEVEL READ COMMITTED;

# TODO SELECT * FROM tblname PROCEDURE ANALYSE();
create table rinha.cliente
(
    id     smallint unsigned primary key,
    limite int not null,
    saldo  int not null
) ENGINE = INNODB;

create table rinha.transacao
(
    id_cliente   smallint unsigned not null,
    valor        int unsigned      not null,
    tipo         char              not null,
    descricao    varchar(10)       not null,
    realizada_em datetime(6)       not null default now(6)
) ENGINE = INNODB;

create index idx_id_clienterealizada_em
    on rinha.transacao (id_cliente, realizada_em desc);

drop procedure if exists rinha.processa_transacao;
DELIMITER |
create procedure rinha.processa_transacao(
    IN in_id_cliente int,
    IN in_valor int,
    IN in_descricao varchar(10),
    IN in_tipo char,
    OUT out_saldo json)
BEGIN
    set @_valor = if(in_tipo = 'd', in_valor * -1, in_valor);
    START TRANSACTION;
    update
        rinha.cliente c
    set c.saldo  = @_saldo := c.saldo + @_valor,
        c.limite = @_limite := c.limite
    where c.id = in_id_cliente
      and (c.saldo + @_valor) >= (c.limite * -1);

    if @_saldo is not null then
        insert into rinha.transacao(valor, descricao, id_cliente, tipo)
        values (in_valor, in_descricao, in_id_cliente, in_tipo);
        set out_saldo = JSON_OBJECT('saldo', @_saldo, 'limite', @_limite);
    end if;
    COMMIT;
END |

delimiter ;


drop procedure if exists rinha.retorna_extrato;
DELIMITER |
create procedure rinha.retorna_extrato(
    IN in_id_cliente int,
    OUT out_extrato json)
BEGIN
    declare saldo_json json;
    declare transacoes_json json;

    set @data_extrato = now(6);

    START TRANSACTION READ ONLY;
    select JSON_OBJECT('total', c.saldo, 'limite', c.limite, 'data_extrato',
                       DATE_FORMAT(@data_extrato, '%Y-%m-%dT%H:%i:%s.%fZ'))
    into saldo_json
    from rinha.cliente c
    where c.id = in_id_cliente;

    select JSON_ARRAYAGG(x.object)
    into transacoes_json
    from (select JSON_OBJECT('valor',
                             t.valor,
                             'tipo',
                             t.tipo,
                             'descricao',
                             t.descricao,
                             'realizada_em',
                             DATE_FORMAT(t.realizada_em, '%Y-%m-%dT%H:%i:%s.%fZ')) object

          from rinha.transacao t
          where t.id_cliente = in_id_cliente
            and t.realizada_em <= @data_extrato
          order by t.realizada_em desc
          limit 10) x;

    if (transacoes_json is null) then
        set transacoes_json = JSON_ARRAY();
    end if;

    set out_extrato = JSON_OBJECT('saldo', saldo_json, 'ultimas_transacoes', transacoes_json);
    COMMIT;
END |
delimiter ;

insert into rinha.cliente (id, limite, saldo)
values (1, 100000, 0),
       (2, 80000, 0),
       (3, 1000000, 0),
       (4, 10000000, 0),
       (5, 500000, 0);


# Cache
