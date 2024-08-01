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
