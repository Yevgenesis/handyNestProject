# Хранение и удаление файлов

Retention выполняется в два этапа: запись сначала получает `deleted_at`, затем отдельный worker удаляет `bucket/storage_key` и записывает `storage_deleted_at`.

## Защищённые данные

- evidence активного dispute не удаляется;
- файлы deal history, verification audit и legal retention нельзя удалять до истечения политики хранения;
- private verification documents никогда не перемещаются в public bucket.

## Диагностика

Проверка очереди удаления:

```sql
SELECT public_id, bucket, storage_key, storage_deletion_attempts, storage_deletion_last_error
FROM marketplace_attachment
WHERE deleted_at IS NOT NULL AND storage_deleted_at IS NULL
ORDER BY deleted_at;
```

При ошибках проверьте credentials/IAM, наличие bucket, network/DNS и object lock. После исправления worker повторит удаление. Нельзя вручную выставлять `storage_deleted_at`, не проверив отсутствие объекта.

Production alerts должны контролировать размер очереди, возраст старейшей записи, количество ошибок и доступность storage endpoint.
