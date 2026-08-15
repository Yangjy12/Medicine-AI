-- Replace global client message idempotency with conversation-scoped idempotency.
-- Run against the application database after pulling this change on an existing ECS deployment.

SET @db_name = DATABASE();

SET @drop_old_index_sql = (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE chat_message DROP INDEX uk_chat_msg_sender_client',
        'SELECT ''uk_chat_msg_sender_client not found'' AS info'
    )
    FROM information_schema.statistics
    WHERE table_schema = @db_name
      AND table_name = 'chat_message'
      AND index_name = 'uk_chat_msg_sender_client'
);

PREPARE drop_old_index_stmt FROM @drop_old_index_sql;
EXECUTE drop_old_index_stmt;
DEALLOCATE PREPARE drop_old_index_stmt;

SET @create_new_index_sql = (
    SELECT IF(
        (
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = @db_name
              AND table_name = 'chat_message'
        ) = 0,
        'SELECT ''chat_message not found, skip new index'' AS info',
        IF(
            COUNT(*) = 0,
            'ALTER TABLE chat_message ADD UNIQUE KEY uk_chat_msg_conv_sender_client (conversation_id, sender_id, client_msg_id)',
            'SELECT ''uk_chat_msg_conv_sender_client already exists'' AS info'
        )
    )
    FROM information_schema.statistics
    WHERE table_schema = @db_name
      AND table_name = 'chat_message'
      AND index_name = 'uk_chat_msg_conv_sender_client'
);

PREPARE create_new_index_stmt FROM @create_new_index_sql;
EXECUTE create_new_index_stmt;
DEALLOCATE PREPARE create_new_index_stmt;
