--------------------------------------------------------------------------------
-- CAPTCHA情報を取得するSQL
--------------------------------------------------------------------------------
SELECT_CAPTCHA_MANAGE =
SELECT
    CAPTCHA_KEY,
    CAPTCHA_TEXT,
    GENERATE_DATE_TIME
FROM
    CAPTCHA_MANAGE
WHERE
    CAPTCHA_KEY = ?

--------------------------------------------------------------------------------
-- CAPTCHA管理情報を作成するSQL
--------------------------------------------------------------------------------
INSERT_CAPTCHA_MANAGE =
INSERT INTO CAPTCHA_MANAGE(
    CAPTCHA_KEY,
    GENERATE_DATE_TIME
)
VALUES(
    ?,
    ?
)

--------------------------------------------------------------------------------
-- CAPTCHA情報を登録するSQL
--------------------------------------------------------------------------------
UPDATE_CAPTCHA_MANAGE =
UPDATE 
    CAPTCHA_MANAGE
SET
    CAPTCHA_TEXT = ?,
    GENERATE_DATE_TIME = ?
WHERE
    CAPTCHA_KEY = ?

