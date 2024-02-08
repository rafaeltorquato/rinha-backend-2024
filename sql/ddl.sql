create database if not exists rinha;

create table rinha.cliente
(
    id     smallint auto_increment primary key,
    limite int not null,
    saldo  int not null
);

create table rinha.transacao
(
    id           int auto_increment primary key,
    id_cliente   smallint    not null,
    valor        int         not null,
    descricao    varchar(10) not null,
    realizada_em datetime(6) not null,
    constraint foreign key (id_cliente) references rinha.cliente (id)
);

create index idx_transacao_realizada_em
    on rinha.transacao (realizada_em desc);
