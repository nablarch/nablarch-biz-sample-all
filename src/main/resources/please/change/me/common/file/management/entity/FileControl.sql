--------------------------------------------------------------------------------
-- ファイルを取得するSQL
--------------------------------------------------------------------------------
SELECT_FILE_CONTROL =
SELECT
    FILE_CONTROL_ID,
    FILE_OBJECT,
    SAKUJO_SGN
FROM
    FILE_CONTROL
WHERE
    FILE_CONTROL_ID = ?
    AND SAKUJO_SGN='0'


--------------------------------------------------------------------------------
-- ファイルを論理削除するSQL
--------------------------------------------------------------------------------
DELETE_FILE_CONTROL = 
UPDATE FILE_CONTROL 
SET
    SAKUJO_SGN='1'
WHERE
    FILE_CONTROL_ID = ?
    AND SAKUJO_SGN='0'
