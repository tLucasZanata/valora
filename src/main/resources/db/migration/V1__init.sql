-- Usuários do sistema com controle de acesso (RBAC)
-- role: ADMIN (acesso total) ou OPERADOR (acesso restrito)
CREATE TABLE usuario (
    id    SERIAL PRIMARY KEY,
    nome  VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100),
    senha VARCHAR(100) NOT NULL,
    role  VARCHAR(20)  NOT NULL DEFAULT 'OPERADOR'
              CHECK (role IN ('ADMIN', 'OPERADOR'))
);

CREATE TABLE funcionario (
    id         SERIAL PRIMARY KEY,
    nome       VARCHAR(100)   NOT NULL,
    cargo      VARCHAR(50),
    contato    VARCHAR(50),
    valor_hora NUMERIC(10,2) NOT NULL,
    email      VARCHAR(100)
);


CREATE TABLE cliente (
    id                  SERIAL PRIMARY KEY,
    nome                VARCHAR(100) NOT NULL,
    cpf_cnpj            VARCHAR(20) UNIQUE,
    email               VARCHAR(100),
    contato             VARCHAR(50),
    endereco            VARCHAR(150),
    cad_pro             VARCHAR(50),
    endereco_adicional  VARCHAR(150)
);


CREATE TABLE servico (
    id          SERIAL PRIMARY KEY,
    codigo      VARCHAR(50),
    nome        VARCHAR(100)  NOT NULL,
    descricao   TEXT,
    observacoes TEXT,
    valor_hora  NUMERIC(10,2),
    isento      BOOLEAN NOT NULL DEFAULT FALSE
);


CREATE TABLE servico_funcionario (
    servico_id     INT NOT NULL REFERENCES servico(id)     ON DELETE CASCADE,
    funcionario_id INT NOT NULL REFERENCES funcionario(id) ON DELETE CASCADE,
    PRIMARY KEY (servico_id, funcionario_id)
);


CREATE TABLE produto (
    id          SERIAL PRIMARY KEY,
    codigo      VARCHAR(20)   NOT NULL UNIQUE,  -- ex: PRD-00001, gerado pelo sistema
    nome        VARCHAR(150)  NOT NULL,
    descricao   TEXT,
    valor_venda NUMERIC(10,2) NOT NULL CHECK (valor_venda >= 0),
    quantidade  INT           NOT NULL DEFAULT 0 CHECK (quantidade >= 0)
);


-- valor_km: custo cobrado por quilometro rodado, especifico de cada veiculo
CREATE TABLE veiculo (
    id       SERIAL PRIMARY KEY,
    modelo   VARCHAR(100)  NOT NULL,
    placa    VARCHAR(10)   NOT NULL UNIQUE,
    valor_km NUMERIC(10,2) NOT NULL DEFAULT 0.00 CHECK (valor_km >= 0)
);


-- status: ciclo de vida do lançamento — ABERTO → EM_ANDAMENTO → FECHADO
-- veiculo_id e km_percorridos: vinculam o veiculo ao lancamento e
-- registram a quilometragem, cujo valor (km x veiculo.valor_km) compoe o total cobrado
CREATE TABLE registro_servico (
    id                SERIAL PRIMARY KEY,
    funcionario_id    INT  REFERENCES funcionario(id) ON DELETE CASCADE,
    cliente_id        INT  REFERENCES cliente(id)     ON DELETE CASCADE,
    servico_id        INT  REFERENCES servico(id)     ON DELETE CASCADE,
    veiculo_id        INT  REFERENCES veiculo(id)     ON DELETE SET NULL,
    data_servico      DATE          NOT NULL DEFAULT CURRENT_DATE,
    hora_inicio       TIME          NOT NULL,
    hora_fim          TIME          NOT NULL,
    km_percorridos    NUMERIC(10,2) CHECK (km_percorridos >= 0),
    observacao        TEXT,
    status            VARCHAR(20)   NOT NULL DEFAULT 'ABERTO'
                          CHECK (status IN ('ABERTO', 'EM_ANDAMENTO', 'FECHADO')),
    horas_trabalhadas NUMERIC(10,4) GENERATED ALWAYS AS (
        EXTRACT(EPOCH FROM (hora_fim - hora_inicio)) / 3600
    ) STORED
);


CREATE TABLE registro_km (
    id             SERIAL PRIMARY KEY,
    veiculo_id     INT           NOT NULL REFERENCES veiculo(id)     ON DELETE CASCADE,
    funcionario_id INT           NOT NULL REFERENCES funcionario(id) ON DELETE CASCADE,
    data_registro  DATE          NOT NULL DEFAULT CURRENT_DATE,
    km_percorridos NUMERIC(10,2) NOT NULL CHECK (km_percorridos > 0),
    observacao     TEXT
);


CREATE TABLE produto_servico (
    id                  SERIAL PRIMARY KEY,
    registro_servico_id INT NOT NULL REFERENCES registro_servico(id) ON DELETE CASCADE,
    produto_id          INT NOT NULL REFERENCES produto(id)          ON DELETE RESTRICT,
    quantidade          INT NOT NULL DEFAULT 1 CHECK (quantidade > 0)
);

-- Auditoria: registra todas as ações realizadas no sistema
-- acao: CRIAR, EDITAR, EXCLUIR, ALTERAR_STATUS, LOGIN, LOGIN_FALHOU
-- detalhes: JSON com campos antes/depois da alteração
CREATE TABLE audit_log (
    id          BIGSERIAL    PRIMARY KEY,
    usuario     VARCHAR(100) NOT NULL,
    acao        VARCHAR(30)  NOT NULL,
    entidade    VARCHAR(50)  NOT NULL,
    entidade_id VARCHAR(50),
    detalhes    TEXT,
    data_hora   TIMESTAMP    NOT NULL DEFAULT NOW()
);


-- View de relatorio: valor_total inclui horas do funcionario + taxa do servico
-- + quilometragem do veiculo + soma dos produtos utilizados
CREATE VIEW vw_relatorio_horas_valores AS
SELECT
    rs.id                                                           AS registro_id,
    rs.status                                                       AS status,
    f.nome                                                          AS funcionario,
    c.nome                                                          AS cliente,
    s.nome                                                          AS servico,
    rs.data_servico,
    rs.hora_inicio,
    rs.hora_fim,
    rs.horas_trabalhadas,
    rs.observacao,
    f.valor_hora                                                    AS valor_hora_funcionario,
    s.valor_hora                                                    AS valor_hora_servico,
    v.modelo                                                        AS veiculo_modelo,
    v.placa                                                         AS veiculo_placa,
    v.valor_km                                                      AS valor_km_veiculo,
    rs.km_percorridos,
    COALESCE(rs.km_percorridos * v.valor_km, 0)                     AS valor_km_total,
    COALESCE(
        (SELECT SUM(p.valor_venda * ps.quantidade)
           FROM produto_servico ps
           JOIN produto p ON ps.produto_id = p.id
          WHERE ps.registro_servico_id = rs.id),
        0
    )                                                               AS valor_produtos,
    ROUND(
        (rs.horas_trabalhadas * f.valor_hora)
        + COALESCE(CASE WHEN NOT s.isento THEN s.valor_hora ELSE 0 END, 0)
        + COALESCE(rs.km_percorridos * v.valor_km, 0)
        + COALESCE(
            (SELECT SUM(p.valor_venda * ps.quantidade)
               FROM produto_servico ps
               JOIN produto p ON ps.produto_id = p.id
              WHERE ps.registro_servico_id = rs.id),
            0
          ),
        2
    )                                                               AS valor_total
FROM registro_servico rs
JOIN funcionario  f ON rs.funcionario_id = f.id
JOIN cliente      c ON rs.cliente_id     = c.id
JOIN servico      s ON rs.servico_id     = s.id
LEFT JOIN veiculo v ON rs.veiculo_id     = v.id
ORDER BY rs.data_servico DESC;


CREATE INDEX idx_produto_servico_registro  ON produto_servico(registro_servico_id);
CREATE INDEX idx_registro_km_veiculo       ON registro_km(veiculo_id);
CREATE INDEX idx_registro_km_funcionario   ON registro_km(funcionario_id);
CREATE INDEX idx_registro_km_data          ON registro_km(data_registro);
CREATE INDEX idx_registro_servico_veiculo  ON registro_servico(veiculo_id);
CREATE INDEX idx_registro_servico_status   ON registro_servico(status);
CREATE INDEX idx_usuario_nome              ON usuario(nome);
CREATE INDEX idx_audit_usuario             ON audit_log(usuario);
CREATE INDEX idx_audit_acao                ON audit_log(acao);
CREATE INDEX idx_audit_entidade            ON audit_log(entidade);
CREATE INDEX idx_audit_data_hora           ON audit_log(data_hora DESC);

-- Adiciona campo para armazenar a URL da imagem do produto, hospedada na nuvem (Cloudinary)
ALTER TABLE produto
    ADD COLUMN IF NOT EXISTS imagem_url VARCHAR(500);