drop database if exists rinha;
create database rinha;
# TODO SELECT * FROM tblname PROCEDURE ANALYSE();
create table rinha.cliente
(
    id     smallint primary key,
    limite int not null,
    saldo  int not null
) ENGINE = INNODB;

create table rinha.transacao
(
    id_cliente   smallint    not null,
    valor        int         not null,
    tipo         char        not null,
    descricao    varchar(10) not null,
    realizada_em datetime(6) not null default now(6)
) ENGINE = INNODB;

create index idx_realizada_em
    on rinha.transacao (id_cliente, realizada_em desc);

drop procedure if exists rinha.processa_transacao;
DELIMITER |
create procedure rinha.processa_transacao(
    IN in_id_cliente int,
    IN in_valor int,
    IN in_descricao varchar(10),
    IN in_tipo char,
    OUT out_saldo int,
    OUT out_limite int)
BEGIN
    set @_valor = if(in_tipo = 'd', in_valor * -1, in_valor);

    SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;

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
        set out_saldo = @_saldo;
        set out_limite = @_limite;
    end if;

    COMMIT;
END |

delimiter ;


drop procedure if exists rinha.retorna_extrato;
DELIMITER |
create procedure rinha.retorna_extrato(
    IN in_id_cliente int)
BEGIN
    SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;

    START TRANSACTION READ ONLY;
    set @_data_extrato = now(6);

    select c.saldo,
           c.limite,
           @_data_extrato as data_extrato
    from rinha.cliente c
    where c.id = in_id_cliente;

    select t.valor,
           t.tipo,
           t.descricao,
           t.realizada_em
    from rinha.transacao t
    where t.id_cliente = in_id_cliente
      and t.realizada_em <= @_data_extrato
    order by t.realizada_em desc
    limit 10;

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
