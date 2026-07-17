-- 明示的なIDをCSVから投入した後、自動採番を既存の最大IDへ追従させる。
SELECT setval(pg_get_serial_sequence('users', 'id'),
              COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM users;
SELECT setval(pg_get_serial_sequence('mutters', 'id'),
              COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM mutters;
SELECT setval(pg_get_serial_sequence('mutter_likes', 'id'),
              COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM mutter_likes;
SELECT setval(pg_get_serial_sequence('follows', 'id'),
              COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM follows;

SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users
UNION ALL SELECT 'mutters', COUNT(*) FROM mutters
UNION ALL SELECT 'mutter_likes', COUNT(*) FROM mutter_likes
UNION ALL SELECT 'follows', COUNT(*) FROM follows;
