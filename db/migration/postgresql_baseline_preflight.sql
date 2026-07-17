-- 既存PostgreSQLをFlywayでベースライン化する前に実行する。
-- INVALID_COUNTが全て0であることを確認し、最後のスキーマ一覧をV1と照合する。
SELECT 'MUTTERS without USERS' AS CHECK_NAME, COUNT(*) AS INVALID_COUNT
FROM MUTTERS m LEFT JOIN USERS u ON u.ID = m.USER_ID
WHERE u.ID IS NULL;

SELECT 'MUTTER_LIKES without MUTTERS' AS CHECK_NAME, COUNT(*) AS INVALID_COUNT
FROM MUTTER_LIKES ml LEFT JOIN MUTTERS m ON m.ID = ml.MUTTER_ID
WHERE m.ID IS NULL;

SELECT 'MUTTER_LIKES without USERS' AS CHECK_NAME, COUNT(*) AS INVALID_COUNT
FROM MUTTER_LIKES ml LEFT JOIN USERS u ON u.ID = ml.USER_ID
WHERE u.ID IS NULL;

SELECT 'FOLLOWS without FOLLOWER' AS CHECK_NAME, COUNT(*) AS INVALID_COUNT
FROM FOLLOWS f LEFT JOIN USERS u ON u.ID = f.FOLLOWER_ID
WHERE u.ID IS NULL;

SELECT 'FOLLOWS without FOLLOWEE' AS CHECK_NAME, COUNT(*) AS INVALID_COUNT
FROM FOLLOWS f LEFT JOIN USERS u ON u.ID = f.FOLLOWEE_ID
WHERE u.ID IS NULL;

SELECT 'MUTTER_LIKES null CREATED_AT' AS CHECK_NAME, COUNT(*) AS INVALID_COUNT
FROM MUTTER_LIKES WHERE CREATED_AT IS NULL;

SELECT 'FOLLOWS null CREATED_AT' AS CHECK_NAME, COUNT(*) AS INVALID_COUNT
FROM FOLLOWS WHERE CREATED_AT IS NULL;

SELECT table_name, column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name IN ('users', 'mutters', 'mutter_likes', 'follows')
ORDER BY table_name, ordinal_position;

SELECT tc.table_name, tc.constraint_type, tc.constraint_name
FROM information_schema.table_constraints tc
WHERE tc.table_schema = 'public'
  AND tc.table_name IN ('users', 'mutters', 'mutter_likes', 'follows')
ORDER BY tc.table_name, tc.constraint_type, tc.constraint_name;
