-- drop database if exists rinha;
-- create database rinha;
-- \c rinha

drop schema if exists rinha CASCADE;
create schema rinha;

create unlogged table rinha.cliente
(
    id     smallint primary key,
    limite int not null,
    saldo  int not null
);

create unlogged table rinha.transacao
(
    id_cliente   smallint           not null,
    valor        int                not null,
    tipo         char               not null,
    descricao    varchar(10)        not null,
    realizada_em timestamp(6)       not null default current_timestamp(6)
);

create index idx_id_clienterealizada_em
    on rinha.transacao (id_cliente, realizada_em desc);

drop PROCEDURE if exists rinha.processa_transacao;

CREATE PROCEDURE rinha.processa_transacao(IN in_id_cliente int,
                                          IN in_valor int,
                                          IN in_descricao varchar(10),
                                          IN in_tipo char,
                                          OUT out_saldo json)
AS
$$
DECLARE
    _valor int;
BEGIN
    if in_tipo = 'd' then
        _valor := in_valor * -1;
    else
        _valor := in_valor;
    end if;
    with atualizar AS (
        update
            rinha.cliente c
                set saldo = c.saldo + _valor
                where c.id = in_id_cliente
                    and (in_tipo = 'c' or (c.saldo + _valor) >= (c.limite * -1))
                returning json_build_object('saldo', saldo, 'limite', limite) object)
    select object
    into out_saldo
    from atualizar;

    if out_saldo is not null then
        insert into rinha.transacao(valor, descricao, id_cliente, tipo)
        values (in_valor, in_descricao, in_id_cliente, in_tipo);
    end if;
END;
$$ LANGUAGE plpgsql;

drop procedure if exists rinha.retorna_extrato;
create procedure rinha.retorna_extrato(IN in_id_cliente int, OUT out_extrato json)
AS
$$
DECLARE
    _saldo_json      json;
    _transacoes_json json;
    _data_extrato    timestamp(6);
BEGIN
    _data_extrato := current_timestamp(6);
    select json_build_object('total', c.saldo, 'limite', c.limite, 'data_extrato',
                             to_char(_data_extrato, 'yyyy-mm-dd"T"HH24:MI:SS.MS'))
    into _saldo_json
    from rinha.cliente c
    where c.id = in_id_cliente;

    select json_agg(x.object)
    into _transacoes_json
    from (select json_build_object('valor',
                                   t.valor,
                                   'tipo',
                                   t.tipo,
                                   'descricao',
                                   t.descricao,
                                   'realizada_em',
                                   to_char(t.realizada_em, 'yyyy-mm-dd"T"HH24:MI:SS.MS')) object

          from rinha.transacao t
          where t.id_cliente = in_id_cliente
          order by t.realizada_em desc
          limit 10) x;

    if (_transacoes_json is null) then
        _transacoes_json := json_build_array();
    end if;
    out_extrato := json_build_object('saldo', _saldo_json,
                                     'ultimas_transacoes', _transacoes_json);
END;

$$ LANGUAGE plpgsql;

insert into rinha.cliente (id, limite, saldo)
values (1, 100000, 0),
       (2, 80000, 0),
       (3, 1000000, 0),
       (4, 10000000, 0),
       (5, 500000, 0);
