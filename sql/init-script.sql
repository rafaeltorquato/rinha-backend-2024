drop database if exists rinha;
create database if not exists rinha;

create table rinha.cliente
(
    id     smallint primary key,
    limite int not null,
    saldo  int not null
) ENGINE = INNODB;

create table rinha.transacao
(
    id           serial primary key,
    id_cliente   smallint    not null,
    valor        int         not null,
    descricao    varchar(10) not null,
    realizada_em datetime(6) not null default now(6)
) ENGINE = INNODB;

create index idx_transacao_realizada_em
    on rinha.transacao (realizada_em desc);



drop procedure if exists rinha.processa_transacao;
DELIMITER |
create procedure rinha.processa_transacao(
    IN in_id_cliente int,
    IN in_valor int,
    IN in_descricao varchar(10),
    OUT out_limite int,
    OUT out_saldo int)
BEGIN
    update
        rinha.cliente c
    set c.saldo  = @_saldo := c.saldo + in_valor,
        c.limite = @_limite := c.limite
    where c.id = in_id_cliente
      and (c.saldo + in_valor) >= (c.limite * -1);

    if @_saldo is not null then
        insert into rinha.transacao(valor, descricao, id_cliente)
        values (in_valor, in_descricao, in_id_cliente);
        set out_saldo = @_saldo;
        set out_limite = @_limite;
    end if;
END |

delimiter ;


/*
Especificação:

1	100000      0
2	80000	    0
3	1000000	    0
4	10000000	0
5	500000	    0
*/
insert into rinha.cliente (id, limite, saldo)
values (1, 100000, 0),
       (2, 80000, 0),
       (3, 1000000, 0),
       (4, 10000000, 0),
       (5, 500000, 0);
