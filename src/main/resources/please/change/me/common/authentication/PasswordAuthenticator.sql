--------------------------------------------------------------------------------
-- システムアカウント情報を取得するSQL
--------------------------------------------------------------------------------
FIND_SYSTEM_ACCOUNT_AND_LOCK =
SELECT
  USER_ID,
  PASSWORD,
  USER_ID_LOCKED,
  PASSWORD_EXPIRATION_DATE,
  FAILED_COUNT
FROM
  SYSTEM_ACCOUNT
WHERE
  USER_ID = ?
  AND EFFECTIVE_DATE_FROM <= ?
  AND EFFECTIVE_DATE_TO >= ?
FOR UPDATE

--------------------------------------------------------------------------------
-- 最終ログイン日時を更新するSQL
--------------------------------------------------------------------------------
UPDATE_LAST_LOGIN_DATETIME =
UPDATE
  SYSTEM_ACCOUNT
SET
  LAST_LOGIN_DATE_TIME = ?
WHERE
  USER_ID = ?

--------------------------------------------------------------------------------
-- ログイン失敗回数を更新するSQL
--------------------------------------------------------------------------------
UPDATE_FAILED_COUNT =
UPDATE
  SYSTEM_ACCOUNT
SET
  FAILED_COUNT = ?
WHERE
  USER_ID = ?

--------------------------------------------------------------------------------
-- ログイン失敗回数とユーザIDのロックを更新するSQL
--------------------------------------------------------------------------------
UPDATE_FAILED_COUNT_AND_USER_LOCK =
UPDATE
  SYSTEM_ACCOUNT
SET
  FAILED_COUNT = ?,
  USER_ID_LOCKED = '1'
WHERE
  USER_ID = ?


