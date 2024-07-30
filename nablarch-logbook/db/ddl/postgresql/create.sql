CREATE TABLE SAMPLE_USER (
  USER_ID SERIAL NOT NULL,
  KANJI_NAME VARCHAR(50) NOT NULL,
  KANA_NAME VARCHAR(50) NOT NULL
);
COMMENT ON table SAMPLE_USER is '�T���v�����[�U���';
COMMENT ON column SAMPLE_USER.USER_ID is '���[�U���ID';
COMMENT ON column SAMPLE_USER.KANJI_NAME is '��������';
COMMENT ON column SAMPLE_USER.KANA_NAME is '���Ȏ���';
CREATE TABLE CODE_NAME (
  CODE_ID CHAR(8) NOT NULL,
  CODE_VALUE VARCHAR(2) NOT NULL,
  LANG CHAR(2) NOT NULL,
  SORT_ORDER SMALLINT NOT NULL,
  CODE_NAME VARCHAR(50) NOT NULL,
  SHORT_NAME VARCHAR(50),
  OPTION01 VARCHAR(50),
  OPTION02 VARCHAR(50),
  OPTION03 VARCHAR(50),
  OPTION04 VARCHAR(50),
  OPTION05 VARCHAR(50),
  OPTION06 VARCHAR(50),
  OPTION07 VARCHAR(50),
  OPTION08 VARCHAR(50),
  OPTION09 VARCHAR(50),
  OPTION10 VARCHAR(50),
  PRIMARY KEY (CODE_ID, CODE_VALUE, LANG)
);
COMMENT ON table CODE_NAME is '�R�[�h����';
COMMENT ON column CODE_NAME.CODE_ID is '�R�[�hID';
COMMENT ON column CODE_NAME.CODE_VALUE is '�R�[�h�l';
COMMENT ON column CODE_NAME.LANG is '����';
COMMENT ON column CODE_NAME.SORT_ORDER is '�\�[�g��';
COMMENT ON column CODE_NAME.CODE_NAME is '����';
COMMENT ON column CODE_NAME.SHORT_NAME is '�R�[�h����';
COMMENT ON column CODE_NAME.OPTION01 is '�I�v�V��������01';
COMMENT ON column CODE_NAME.OPTION02 is '�I�v�V��������02';
COMMENT ON column CODE_NAME.OPTION03 is '�I�v�V��������03';
COMMENT ON column CODE_NAME.OPTION04 is '�I�v�V��������04';
COMMENT ON column CODE_NAME.OPTION05 is '�I�v�V��������05';
COMMENT ON column CODE_NAME.OPTION06 is '�I�v�V��������06';
COMMENT ON column CODE_NAME.OPTION07 is '�I�v�V��������07';
COMMENT ON column CODE_NAME.OPTION08 is '�I�v�V��������08';
COMMENT ON column CODE_NAME.OPTION09 is '�I�v�V��������09';
COMMENT ON column CODE_NAME.OPTION10 is '�I�v�V��������10';
CREATE TABLE CODE_PATTERN (
  CODE_ID CHAR(8) NOT NULL,
  CODE_VALUE VARCHAR(2) NOT NULL,
  PATTERN01 CHAR(1) NOT NULL,
  PATTERN02 CHAR(1),
  PATTERN03 CHAR(1),
  PATTERN04 CHAR(1),
  PATTERN05 CHAR(1),
  PATTERN06 CHAR(1),
  PATTERN07 CHAR(1),
  PATTERN08 CHAR(1),
  PATTERN09 CHAR(1),
  PATTERN10 CHAR(1),
  PATTERN11 CHAR(1),
  PATTERN12 CHAR(1),
  PATTERN13 CHAR(1),
  PATTERN14 CHAR(1),
  PATTERN15 CHAR(1),
  PATTERN16 CHAR(1),
  PATTERN17 CHAR(1),
  PATTERN18 CHAR(1),
  PATTERN19 CHAR(1),
  PATTERN20 CHAR(1),
  PRIMARY KEY (CODE_ID, CODE_VALUE)
);
COMMENT ON table CODE_PATTERN is '�R�[�h�p�^�[��';
COMMENT ON column CODE_PATTERN.CODE_ID is '�R�[�hID';
COMMENT ON column CODE_PATTERN.CODE_VALUE is '�R�[�h�l';
COMMENT ON column CODE_PATTERN.PATTERN01 is '�p�^�[��01';
COMMENT ON column CODE_PATTERN.PATTERN02 is '�p�^�[��02';
COMMENT ON column CODE_PATTERN.PATTERN03 is '�p�^�[��03';
COMMENT ON column CODE_PATTERN.PATTERN04 is '�p�^�[��04';
COMMENT ON column CODE_PATTERN.PATTERN05 is '�p�^�[��05';
COMMENT ON column CODE_PATTERN.PATTERN06 is '�p�^�[��06';
COMMENT ON column CODE_PATTERN.PATTERN07 is '�p�^�[��07';
COMMENT ON column CODE_PATTERN.PATTERN08 is '�p�^�[��08';
COMMENT ON column CODE_PATTERN.PATTERN09 is '�p�^�[��09';
COMMENT ON column CODE_PATTERN.PATTERN10 is '�p�^�[��10';
COMMENT ON column CODE_PATTERN.PATTERN11 is '�p�^�[��11';
COMMENT ON column CODE_PATTERN.PATTERN12 is '�p�^�[��12';
COMMENT ON column CODE_PATTERN.PATTERN13 is '�p�^�[��13';
COMMENT ON column CODE_PATTERN.PATTERN14 is '�p�^�[��14';
COMMENT ON column CODE_PATTERN.PATTERN15 is '�p�^�[��15';
COMMENT ON column CODE_PATTERN.PATTERN16 is '�p�^�[��16';
COMMENT ON column CODE_PATTERN.PATTERN17 is '�p�^�[��17';
COMMENT ON column CODE_PATTERN.PATTERN18 is '�p�^�[��18';
COMMENT ON column CODE_PATTERN.PATTERN19 is '�p�^�[��19';
COMMENT ON column CODE_PATTERN.PATTERN20 is '�p�^�[��20';
ALTER TABLE SAMPLE_USER ADD CONSTRAINT PK_SAMPLE_USER PRIMARY KEY
(
  USER_ID
);
ALTER TABLE CODE_NAME
ADD 
FOREIGN KEY (
  CODE_ID,
  CODE_VALUE
) REFERENCES CODE_PATTERN (
  CODE_ID,
  CODE_VALUE
);
